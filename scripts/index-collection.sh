#!/bin/bash
#
# Script to index collections for PWA
#
# Simao Fontes | simao.fontes@fccn.pt
#
# File managed by puppet

# Load common functions for arquivo scripts
. /opt/searcher/scripts/functions
RETVAL=$?
if [[ $RETVAL != 0 ]]; then
    echo "Error: Not able to start functions file."
    exit $RETVAL
fi

# Verify the user has configuration file
check_file_exists ~/.indexing
. ~/.indexing

# applications used
hadoop="${HADOOP_HOME}/bin/hadoop"
get_time="/usr/bin/time -a -o report"

# Global Variables for functions
COLLECTION_FOLDER=${COLLECTION_FOLDER:-"/data/collection_data"}
disk_machine=${disk_machine:-"p14.arquivo.pt"}
HASLOG=true
PATH_ROOT="null"
## Verify if your root and if you have sudo
#if [[ $(id -u) == 0 ]]; then
#    correct 1 "This script cannot be executed as root."
#fi
#sudo -v
#correct $? "This user cannot execute sudo in this machine."

# Verify deploy of application
${SCRIPTS_DIR}/deploy.sh -l
if [ $? -gt 0 ]; then
    debug "Deploying new application"
    # Deploy new if not latest
    sudo ${SCRIPTS_DIR}/deploy.sh -a
    # Deploy broker ability
    sudo ${SCRIPTS_DIR}/deploy.sh -b
fi

if [ ! -e ${SCRIPTS_DIR}/lib/pwalucene-1.0.0-SNAPSHOT.jar ]; then
    cd ${SCRIPTS_DIR}
    unzip ${SCRIPTS_DIR}/nutchwax-job-0.11.0-SNAPSHOT.jar lib/pwalucene-1.0.0-SNAPSHOT.jar
fi

# Print usage of script
usage(){
    echo "Usage: $(basename $0) [options] <machines and folders>"
    echo "Mandatory arguments to long options are mandatory for short options too."
    echo "      -c  Collection name, AWP1"
    echo "      -d  Enable debug for this script"
    echo "      -t  Collection Type, AWP or FAWP"
    echo "      -l  No log is need"
    echo "      -?  Display this help and exit"
}

# Select collection type for indexation
collection_type(){
    case $(echo "$1"| awk '{print tolower($0)}') in
      "awp")
        debug "Collection type: awp (normal)"
        COLLECTION_TYPE="normal";;
      "fawp")
        debug "Collection type: fawp (multiple)"
        COLLECTION_TYPE="multiple";;
      *)
        debug "Collection type: (default) normal"
        COLLECTION_TYPE="normal";;
    esac
}

# Read file with checkpoint for state, in case the script fails or process returns errors
read_file(){
PATH_ROOT=`pwd`
    CHECKPOINT_FILE="checkpoint"
    if [ -e "$CHECKPOINT_FILE" ]; then
        debug "File checkpoint exists!"
        RETVAL=$(tail -n1 ${CHECKPOINT_FILE})
    else
        debug "File checkpoint doesn't exist!"
        RETVAL=0
    fi
    case $RETVAL in
        "import")           RET="1";;
        "update")           RET="2";;
        "invert")           RET="3";;
        "index")            RET="4";;
        "merge")            RET="5";;
        "copy_outputs")     RET="6";;
        "sort_indexes")     RET="7";;
        "index_prunning")   RET="8";;
        "copy_stopwords")   RET="12";;
        "create_blacklist") RET="12";; # note that the RET is equal for create_blacklist and copy_to_diks in order to avoid the reques_disk and copy_to_disk  operation
        "request_disk")     RET="11";;
        "copy_to_disk")     RET="12";;
        "create_md5sum")    RET="14";;
        "close_disk")       RET="14";;
        "finished")         echo "##### Collection Finished #####"; exit 0;;
    esac

    debug "Checkpoint file returned: $RETVAL corresponds to: $RET"
    return $RET
}



# Start the indexing process
create_arcs_file(){
    debug "Creating arcs file for $COLLECTION_NAME. In: $ARCS_FILE, $*"
    inputs="inputs_$COLLECTION_NAME"
    
    while (($#))
    do
        read SERVER FOLDER <<<$(IFS=":"; echo $1)
        debug "Read from input Server: ${SERVER} and folder: ${FOLDER}"
        PORT_FOR_TOMCAT=8080
        CATALINA_HOME="/opt/searcher/apache-tomcat-8.0.30"
        CATALINA_LINK="$CATALINA_HOME/webapps/browser/files/$COLLECTION_NAME"
        debug "Creating links in tomcat for $SERVER"
  echo $CATALINA_LINK
        ssh $SERVER  "if [ ! -e $CATALINA_LINK ]; then ln -s $FOLDER "$CATALINA_LINK"; fi"
        correct $? "Could not ssh to server $SERVER and create link from \n$FOLDER --> $CATALINA_LINK"
        ssh $SERVER "find $FOLDER -name '*arc.gz*' -printf 'http://${SERVER}:${PORT_FOR_TOMCAT}/browser/files/${COLLECTION_NAME}/%P\n'" >> $ARCS_FILE
        tail -n1 $ARCS_FILE | xargs HEAD -d
        correct $? "Could not connect to server $SERVER to get arc file"
        shift
    done
    
    $hadoop dfs -mkdir $inputs
    correct $? "Creating folder in hdfs"
    $hadoop dfs -put $ARCS_FILE $inputs
echo "arcs_file_inputs : $ARCS_FILE $inputs"
    correct $? "Putting arcs file in hdfs"
}

# Set a property for xml java
set_property(){
    echo "<property>" >> $3
    echo "<name>$1</name>" >> $3
    echo "<value>$2</value>" >> $3
    echo "</property>" >> $3
}

# Prepare FAWP indexation
fawp_prepare(){
    echo "Preparing FAWP database."
    dbname="indexing"
    username="indexer"
    secret="1nd3x3r"
    if [ ! -e conf_multiple ]; then
        cp -r $HADOOP_HOME/conf conf_multiple
        num_remove=$(sed -n '/<\/configuration>/=' conf_multiple/hadoop-site.xml)
        head -n $(expr $num_remove - 1) conf_multiple/hadoop-site.xml > temp
        set_property collection.type multiple temp
        set_property database.conection "//p43.arquivo.pt/$dbname" temp
        set_property database.username $username temp
        set_property database.password $secret temp
        echo "</configuration>" >> temp
        mv temp conf_multiple/hadoop-site.xml
    fi
    CONFIG="--config $(pwd)/conf_multiple"
    if [ -e DBprepared ]; then 
        debug "FAWP database is allready prepared."
        return 0; fi
    HERITRIX_HOME=${HERITRIX_HOME:-"${SCRIPTS_DIR}/heritrix-1.12.1"}
    WAYBACK_HOME=${WAYBACK_HOME:-"${SCRIPTS_DIR}/wayback_lib"}
    DEPLOY_DIR=${DEPLOY_DIR:-"/opt/searcher/deploy"}
    csv_file="stats-$COLLECTION_NAME.csv"
    csv_file_final="stats-normalized-$COLLECTION_NAME.csv"
    export PGPASSWORD="$secret"
    psql="/usr/bin/psql $dbname -U $username"
    if [ ! -e ${HERITRIX_HOME} ]; then
        debug "Deploying heritrix"
        cd ${SCRIPTS_DIR}
        wget https://pwa-technologies.googlecode.com/files/heritrix-1.12.1.zip
        unzip heritrix-1.12.1.zip
        rm heritrix-1.12.1.zip
        cd -
    fi
    if [ ! -e ${SCRIPTS_DIR}/wayback_lib ]; then
        unzip -j ${DEPLOY_DIR}/current/wayback-1.2.1.war 'WEB-INF/lib/*' -d ${SCRIPTS_DIR}/wayback_lib
    fi
    if [ ! -e $csv_file ]; then
        debug "Creating csv file"
        while read line; do
            export HERITRIX_HOME
            export WAYBACK_HOME
            ${HERITRIX_HOME}/src/scripts/arcreader -p $line 2>/dev/null | awk '{if (NR!=1) {print}}' | awk '{printf("\42%s-%s-%s %s:%s:%s\42", substr($1,0,4), substr($1,5,2), substr($1,7,2), substr($1,9,2), substr($1,11,2), substr($1,13,2))}; {print ",\42"gensub(/\"/,"\"\"","g",$3)"\42,\42"$4"\42,"$5","$8",\42"$9"\42"}' >> $csv_file
        done < $ARCS_FILE
    fi
    echo -e "$step\n$(date)" >> "time"
    $psql -f ${SCRIPTS_DIR}/createDB.sql
    if [ ! -e $csv_file_final ]; then
        $get_time -f "*Time to execute hadoop $step, %E seconds" ${HADOOP_HOME}/bin/hadoop jar ${SCRIPTS_DIR}/nutchwax-job-0.11.0-SNAPSHOT.jar class org.apache.access.nutch.utils.UrlNormalizer $csv_file $csv_file_final 6 1
        correct $? "There was an error in creating normalized csv"
    fi
    echo $psql -c "\copy files FROM '$csv_file_final' DELIMITER ',' NULL AS '-' CSV"
    $get_time -f "*Time to execute import to psql, %E seconds" $psql -c "\copy files FROM '$csv_file_final' DELIMITER ',' NULL AS '-' CSV"
    correct $? "There was an error importing csv to psql"
#    $get_time -f "*Time to execute create index in psql, %E seconds" $psql -c "CREATE INDEX type_index ON files(type); CREATE INDEX status_index ON files(status); CREATE INDEX url_index ON files USING hash(url);"
    $get_time -f "*Time to execute create index in psql, %E seconds" $psql -c "CREATE INDEX type_index ON files(type); CREATE INDEX status_index ON files(status);"
    correct $? "creating indexes in psql"
    date >> "time"
    echo "" > DBprepared
}

# Exec Hadoop procedure define by nutch
hadoop_exec(){
    step="$1"
    echo "Starting nutch $step phase."
    echo -e "$step\n$(date)" >> "time"
    debug "${HADOOP_HOME}/bin/hadoop ${CONFIG} jar ${SCRIPTS_DIR}/nutchwax-job-0.11.0-SNAPSHOT.jar $step $inputs $outputs $2"
    echo "${HADOOP_HOME}/bin/hadoop ${CONFIG} jar ${SCRIPTS_DIR}/nutchwax-job-0.11.0-SNAPSHOT.jar $step $inputs $outputs $2"
    $get_time -f "*Time to execute hadoop $step, %E seconds" ${HADOOP_HOME}/bin/hadoop ${CONFIG} jar ${SCRIPTS_DIR}/nutchwax-job-0.11.0-SNAPSHOT.jar $step $inputs $outputs $2
    correct $? "There was an error in $step"
    ##unset inputs
    date >> "time"
    echo "$step" > "${CHECKPOINT_FILE}"
}

# Copy outputs from the hdfs
hadoop_copy_outputs(){
    echo "Copying files from hdfs to $dest_folder"

# Checking if there is available space in /data directory
    hadoop_outputs_size=$(${HADOOP_HOME}/bin/hadoop dfs -dus $outputs | awk '{print $2}')
    let hadoop_outputs_size=hadoop_outputs_size/1000
    local_data_size=$(df /data | tail -n1 | awk '{print $4}')
    if [ "$hadoop_outputs_size" -ge "$local_data_size" ]; then correct 1 "There is not enouth space in $dest_folder"; fi
    echo -e "Copy outputs from HDFS\n$(date)" >>"time"
    $get_time -f "*Time to copy hadoop outputs from HDFS, %E seconds" ${HADOOP_HOME}/bin/hadoop fs -get $outputs $dest_folder
    correct $? "There was an error copying files from HDFS to $dest_folder"
    date >>"time" 
    echo "copy_outputs" > "${CHECKPOINT_FILE}"
}

# Sort the index
sort_indexes(){
    echo -e "IndexSorter\n$(date)" >>"time"
    $get_time -f "*Time to sort index, %E seconds" ${HADOOP_HOME}/bin/hadoop jar ${SCRIPTS_DIR}/nutchwax-job-0.11.0-SNAPSHOT.jar class org.apache.nutch.indexer.IndexSorterArquivoWeb $dest_folder
    correct $? "There was an error inverting index"
    mv $dest_folder/index $dest_folder/index-orig
    correct $? "There was an error moving original index to new directory"
    mv $dest_folder/index-sorted $dest_folder/index
    correct $? "There was an error moving inverted index to index directory"
    date >> "time"
    echo "sort_indexes" > "${CHECKPOINT_FILE}"
}

# Index prunning
index_prunning(){
    INDEX_DIR="${dest_folder}/index"
	INDEX_DIR_OUT="${dest_folder}/index-prunning"
	echo -e "PruningIndexes\n$(date)" >>"time"
	$get_time -f "*Time prunning index, %E seconds" java -Xmx5000m -classpath ${SCRIPTS_DIR}/lib/pwalucene-1.0.0-SNAPSHOT.jar org.apache.lucene.pruning.PruningTool -impl arquivo -in $INDEX_DIR -out $INDEX_DIR_OUT -del pagerank:pPsv,outlinks:pPsv -t 1
	correct $? "There was an error in prunning index process"
	mv $INDEX_DIR "$INDEX_DIR-sorted"
	correct $? "There was an error moving sorted index to new directory" 
	mv $INDEX_DIR_OUT $INDEX_DIR
	correct $? "There was an error moving prunning index to index directory"
	date >> "time"
	echo "index_prunning" > "${CHECKPOINT_FILE}"
}

# Copy stopwords to correct path
copy_stopwords(){
	echo -e "Copy stopwords\n$(date)" >>"time"
	$get_time -f "*Time getting stop words, %E seconds" wget -O "${dest_folder}/index/stopwords.cache" "http://pwa-technologies.googlecode.com/svn/trunk/scripts/stopwords.cache"
	correct $? "There was an error creating stopwords file"
	date >>"time"
	echo "copy_stopwords" > "${CHECKPOINT_FILE}"
}

# Send mail to suporte@aia.fccn.pt , creating a disk in machine p14
request_disk(){
   collection=$(echo $COLLECTION_NAME|tr A-Z a-z)
    subject="Criação de disco $COLLECTION_NAME"
   dest_email="suporte@aia.fccn.pt"
	from="${user_mail}"

# Connect to servers and calculate disk space needed
    size=$(calculate_size_ssh_servers $* | awk '{print $1}' | awk '{s+=$1} END {print s}')
	size=$(($size + $(du -bs ${dest_folder}| tail -n1 | awk '{print $1}')))
	size=$(echo "$size*1.2" | bc)
    size=$(echo $size | awk '{ sum=$1 ; hum[1024**3]="Gb";hum[1024**2]="Mb";hum[1024]="Kb"; for (x=1024**3; x>=1024; x/=1024){ if (sum>=x) { printf "%.0f%s\n",sum/x,hum[x];break } }}')
    email_text="Boa tarde,\n\nAgradecia que fosse criado um disco com capacidade de armazenar ${size} de dados, na máquina ${disk_machine} em /${collection}data.\nA partição deve ter permissões de escrita para grupo tomba.\n\nObrigado pela ajuda,\n${user_signature}"
    debug "From: ${from}, Subject: $subject, messsage: $email_text"
	send_mail "$from" "$dest_email" "$subject" "$email_text"
	correct $? "Could not send email to create disk."
	#echo -e "$email_text" > /tmp/message.txt
	#EMAIL="${from}" mutt -s "$subject" "$dest_email" < /tmp/message.txt
	date >>"time"
	echo "request_disk" > "${CHECKPOINT_FILE}"

# Stop while at this point so it can be reexecuted later.
	STOP_WHILE=true
}

# Execute command in list of servers
calculate_size_ssh_servers(){
	if [ $# -lt 2 ]; then correct $? "Invalid number of arguments for calculate_size_ssh_servers"; fi

	while (($#))
		do
    		read SERVER FOLDER <<<$(IFS=":"; echo $1)
	    	COMMAND="du -bs ${FOLDER}"
	    	debug "Execute command in Server: ${SERVER} ${COMMAND}"
	    	ssh ${SERVER} ${COMMAND} | tail -n1
	    	correct $? "Could not ssh to server $SERVER and execute command ${COMMAND}"
		shift
	done
}

# Get the crawl logs from seperate servers
retrive_crawl_logs(){
    while (($#))
		do
			read SERVER FOLDER <<<$(IFS=":"; echo $1)
			debug "Retriving Heritrix logs of Server: ${SERVER}"
#        crawl_log_file=$(ssh $SERVER "find ${FOLDER} -name 'crawl.log*' -printf '$SERVER:%p\n'")
			if [ ! -e "logs_from_$SERVER" ]; then mkdir "logs_from_$SERVER"; fi

#        for file in ${crawl_log_file}; do
			debug "Retriving Heritrix logs: ${SERVER} file: $file"
#rsync -avz --dry-run --include '*/' --include 'crawl.log*' --exclude '*' $SERVER:${FOLDER} logs_from_$SERVER
			rsync -az --include '*/' --include 'crawl.log*' --exclude '*' $SERVER:${FOLDER} logs_from_$SERVER
			correct $? "Could not copy crawl logs of file: $file from server $SERVER"
#        done
	    shift
    done
}

# Create the blacklist for this collection
create_blacklist(){
    if [ $HASLOG == true ]; then 
	    echo -e "Blacklist\n$(date)" >>"time"
# test if apache and hadoop are installed.
    	CATALINA_HOME="/opt/searcher/apache-tomcat-8.0.30"
	    HADOOP_QS_HOME="/opt/searcher/hadoopForSearchServers"
    	INDEXER_HOME="/data/indexer/outputs"
	    if [ ! -e $CATALINA_HOME ]; then echo "Apache tomcat not in folder: $CATALINA_HOME"; fi
    	if [ ! -e $HADOOP_QS_HOME ]; then echo "Hadoop not in folder: $HADOOP_QS_HOME";fi
	    rm -f $INDEXER_HOME
		ln -s $dest_folder $INDEXER_HOME

# count number of documents and disk space
#    echo "documentos:"; cat logs/crawl.log* | sed 's/  */ /g'| cut -d ' ' -f2,12 | grep "^200" | grep -v "duplicate" | wc -l
#    echo "arcs:"; find | grep "arc.gz" | wc -l
#    echo "espaco em disco:"; du -ch --max-depth=0 | tail -1
		retrive_crawl_logs $*
		if [ ! -e "${COLLECTION_NAME}.errors.list.txt" ]; then 
			debug "Making errors list from logs."
			crawl_logs=$(find logs_from_* -name 'crawl.log*')
			debug "crawl_logs: $crawl_logs"
			if [ ! -z "${crawl_logs}" ]; then
				cat $crawl_logs | sed 's/  */ /g' | cut -d ' ' -f2,4 | egrep -v '^2' | egrep -v '^\-' | egrep -v '^1 ' | egrep -v '^0 ' > "${COLLECTION_NAME}.errors.list.txt"
				correct $? "Error creating errors.list.txt"
			else
				correct "1" "Error no crawl log files found."
			fi
			debug "Error list was created from logs."
		fi

# Configure and start QS
# Count number of documents from collection
#debug "Calculating number of documents from logs."
#num_docs=$(cat logs_from_*/crawl.log* | sed 's/  */ /g'| cut -d ' ' -f2,12 | grep "^200" | grep -v "duplicate" | wc -l)
#num_memory=$(expr $num_docs / 7168)

# Configure search-servers.txt for starting Query Servers
#echo "${HOSTNAME} 20000 ${INDEXER_HOME} ${num_memory} \#${COLLECTION_NAME}" > /opt/searcher/collections/search-servers.txt

# Configure search-servers.txt for the tomcat(broker) application 
		echo "${HOSTNAME} 20000" > /opt/searcher/deploy/configs/search-servers.txt

# Create original blacklist file without any contents
		if [ ! -e "${INDEXER_HOME}/index/blacklist.cache" ]; then 
			debug "touching blacklist file for first time."
			touch ${INDEXER_HOME}/index/blacklist.cache
		fi

# Restart and Verify query server is ready
		sudo /sbin/service hadoopserver restart
		STOP=false
		i=0
		while ! $STOP
		do
	    	if [ $DEBUG ]; then echo "COUNT: $i"; fi
			echo "Verify query server is ready to recive"
			sudo PATH=$PATH:/sbin /opt/searcher/scripts/deploy.sh -u
	    	if [ "$?" -gt 0 ] ; then
				debug "One of the query servers is not started, waiting ..."
				sleep 1m
			else
				debug "Query servers started"
				STOP=true
			fi
			if [ "$i" -eq 30 ]; then
				echo "Query servers took too much time comming up. exiting"
				exit 1
			fi
			let i+=1
		done

    	sudo /sbin/service tomcat stop
		sleep 10
	    sudo /sbin/service tomcat start
		sleep 2
		sudo /sbin/service httpd restart

		HEAD "http://${HOSTNAME}:8080/" > /dev/null
		correct $? "The tomcat server not returning results"
		HEAD "http://${HOSTNAME}/" > /dev/null
		correct $? "The httpd server not returning results"

		ARCS_FILE=arcs_$COLLECTION_NAME.txt
		cat ${ARCS_FILE} | awk -F"/" '{print $NF" "$0}' > tmp_arcs 
		WAYBACK_HOME=${CATALINA_HOME}/webapps/wayback/WEB-INF/lib ${SCRIPTS_DIR}/location-client add-stream http://${HOSTNAME}:8080/arcproxy/locationdb >/dev/null < tmp_arcs
		rm -f tmp_arcs

		HEAD "http://${HOSTNAME}:8080/wayback/wayback/id0index0" > /dev/null
		correct $? "Wayback is not returning results"

		debug "java -classpath ${SCRIPTS_DIR}/lib/pwalucene-1.0.0-SNAPSHOT.jar org.apache.lucene.search.caches.PwaBlacklistCache ${INDEXER_HOME}/index http://${HOSTNAME}:8080/wayback/wayback 30 0 -1 ${COLLECTION_NAME}.errors.list.txt"
		$get_time -f "*Time creating blacklist, %E seconds" java -classpath ${SCRIPTS_DIR}/lib/pwalucene-1.0.0-SNAPSHOT.jar org.apache.lucene.search.caches.PwaBlacklistCache "${INDEXER_HOME}/index" http://${HOSTNAME}:8080/wayback/wayback 30 0 -1 "${COLLECTION_NAME}.errors.list.txt" >lixo1 2>lixo2
		correct $? "There was an error creating the blacklist"

		date >>"time"
    fi
	echo "create_blacklist" > "${CHECKPOINT_FILE}"
}

# Copy all files for this collection to separate disk.
copy_to_disk(){
	debug "Copying files to new partition."
	collection_dir="/$(echo $COLLECTION_NAME|tr A-Z a-z)data/$COLLECTION_NAME"
	ssh $disk_machine "mkdir -p $collection_dir && touch $collection_dir/testrw"
	if [ ! $? -eq 0 ]; then
		from=${user_mail}
	    dest_email=${user_mail}
	    subject="Cannot write ${disk_machine}:$collection_dir/testrw"
		email_text="Cannot write $collection_dir/testrw\nProcedure:\n login to ${disk_machine} ssh ${disk_machine} and chown root:tomba -R ${collection_dir} and chmod 775 -R ${collection_dir}."
		send_mail "$from" "$dest_email" "$subject" "$email_text"
		exit 1
	fi

	start_date=$(date +"%s")

	debug "Creating arcs directory on final destination"
	ssh $disk_machine "mkdir -p $collection_dir/arcs"
	correct $? "Could not create directory: $disk_machine@$collection_dir/$COLLECTION_NAME/arcs"

	debug "Copying outputs folder from this machine to ${disk_machine}:$collection_dir"
	scp -q -r $dest_folder $disk_machine:$collection_dir
	correct $? "Could not copy $dest_folder to $disk_machine:$collection_dir"

	while (($#))
		do
			read SERVER FOLDER <<<$(IFS=":"; echo $1)
			debug "Copying folder ${FOLDER} from Server: ${SERVER} to ${disk_machine}"
			ssh $disk_machine "scp -q -r ${SERVER}:${FOLDER} $collection_dir"
			correct $? "Copying from ${SERVER}:${FOLDER} to $disk_machine:$collection_dir"
		shift
	done
	debug "Changing permissions for directories and arc files in $collection_dir"
	ssh $disk_machine "find $collection_dir -type d -exec chmod +w {} \;"
	correct $? "Changing permissions for folders in $disk_machine:$outputs_dir"
	ssh $disk_machine "find $collection_dir -name '*.arc.gz*' -exec chmod +w {} \;"
	correct $? "Changing permissions for arc files in $disk_machine:$outputs_dir"
	debug "Moving arcs to arcs directory."
	ssh $disk_machine "find $collection_dir -name '*.arc.gz*' -exec mv {} $collection_dir/arcs \;"
	correct $? "Could not move arcs to $disk_machine:$outputs_dir/arcs"
	debug "Organizing arcs into arcs directory."
	ssh $disk_machine "java -classpath $CLASSPATH:${SCRIPTS_DIR}/arquivowebutils-1.0.1.jar pt.arquivoweb.utils.CopyFilesToDirTree $collection_dir/arcs $collection_dir/arcs 100 arc.gz move \;"
	correct $? "Could not organize the $disk_machine:$outputs_dir/arcs"

	end_date=$(date +"%s")
	diff=$(($end_date-$start_date))
	echo "*Time to execute copy of files, $(date -u -d @"$diff" +'%-Mm %-Ss') seconds" >> report
	echo "copy_to_disk" > "${CHECKPOINT_FILE}"
}

# Create md5sum file for the collection
create_md5sum(){
	echo "Creating md5sum for collection: $COLLECTION_NAME"
    read SERVER FOLDER <<<$(IFS=":"; echo $1)
    collection_dir="/$(echo $COLLECTION_NAME|tr A-Z a-z)data/$COLLECTION_NAME"
	cd $dest_folder;find ! -name 'checksums_collection.md5' -type f -exec md5sum {} \; >> checksums_collection.md5
    correct $? "creating md5sum in  $dest_folder"
    # The bellow code was developed to create md5 file for the whole project on p14.arquivo.pt
    #ssh $disk_machine "cd $collection_dir;find ! -name 'checksums_collection.md5' -type f -exec md5sum {} \; >> checksums_collection.md5"
   	read SERVER FOLDER <<<$(IFS=":"; echo $1)
    ssh $SERVER "cd $FOLDER;find ! -name 'checksums_collection.md5' -type f -exec md5sum {} \; >> checksums_collection.md5"
 	correct $? "creating md5sum in machine $SERVER:$FOLDER"
    cd $PATH_ROOT
	echo "create_md5sum" > "${CHECKPOINT_FILE}"
}

# Close partition of new collection
close_disk(){
    echo "Closing the collection: $COLLECTION_NAME"
    collection_dir="/$(echo $COLLECTION_NAME|tr A-Z a-z)data/$COLLECTION_NAME"
    debug ssh $disk_machine "find ${collection_dir} -type f -exec chmod 440 {} \; && find ${collection_dir} -type d -exec chmod 550 {} \;"
    ssh $disk_machine "find ${collection_dir} -type f -exec chmod 440 {} \; && find ${collection_dir} -type d -exec chmod 550 {} \;"
    correct $? "closing collection of $COLLECTION_NAME, in machine: $disk_machine:$collection_dir"
    echo "close_disk" > "${CHECKPOINT_FILE}"
}

# Connect to hadoop slaves and get import logs
get_import_logs(){
    job_name=$(GET 'http://indexer.arquivo.pt:50030/jobhistory.jsp?historyFile=JobHistory.log' | sed -n '/^<h3>Completed Jobs <\/h3>/,/^<h3>Failed Jobs <\/h3>/p' | sed 's/<[./]*tr>/\n/g' | sed 's/<[./]*td>/ /g' | grep import | grep $COLLECTION_NAME | sed 's/<\/a>.*//g'| sed 's/.*>job_//g' | tail -1) 
    logs_dir="/data/hadooptemp/logs/userlogs"
if [ ! -d hadoop_logs ];
then
    mkdir hadoop_logs
fi
    cat $HADOOP_HOME/conf/slaves | while read line
    do
        debug "Getting log file from $line"
        ssh $line "locate /task_$job_name*"
        scp -q -r $line:"$logs_dir/task_$job_name*" hadoop_logs
        correct $? "Copying hadoop import logs from machine $line"
    done
    
   echo Charset errors: >> report_logs
   grep -r 'IllegalCharsetNameException' hadoop_logs/* | awk {'print $6'} | sort -u >> report_logs
   correct $? "Calculating charset errors."
   num_errors=$(grep -r 'IllegalCharsetNameException' hadoop_logs/* | wc -l)
   echo no parser for: >> report_logs
   grep -r 'parser not found for' hadoop_logs/* | awk {'print $15'} | sort -u >> report_logs
   correct $? "Calculating parser not found errors."
   let num_errors=$num_errors+$(grep -r 'parser not found for' hadoop_logs/* | wc -l)
   echo Error while parsing: >> report_logs
   grep -r 'Error parsing' hadoop_logs/* | awk {'print $7'} | sort | uniq -c | sort -n -r >> report_logs
   correct $? "Calculating parsing errors."
   let num_errors=$num_errors+$(grep -r 'Error parsing' hadoop_logs/* | wc -l)
   echo Number of errors: $num_errors >> report_logs
}

# Create report of the indexation of collection:
create_report(){
    echo "Creating report for the indexation"
    debug "Content of report:"
   debug "$(cat report)"
    
    get_import_logs
    
    subject="Indexing of $COLLECTION_NAME terminated."
    dest_email="${alert_finished}"
    from="indexer@asa.fccn.pt"
    email_text="The report generated is:\n$(cat report)\n\nReport logs:\n$(cat report_logs|sed 's/^/ /')\n\nTime information:\n$(cat time|sed 's/^/ /')"
    send_mail "$from" "$dest_email" "$subject" "$email_text"
    echo "finished" > "${CHECKPOINT_FILE}"
}
# Process the arguments and files
process_phase(){
    read_file
    PHASE="$?"
    debug "Folder: $(pwd)"
    ARCS_FILE=arcs_$COLLECTION_NAME.txt
    if [ ! -e $ARCS_FILE ]; then create_arcs_file $*; fi
    if [ $COLLECTION_TYPE = "multiple" ]; then fawp_prepare; fi
    case "$PHASE" in
        "0")  inputs="inputs_$COLLECTION_NAME" hadoop_exec "import" $COLLECTION_NAME
              unset inputs
              ;;
        "1")  hadoop_exec update;;
        "2")  hadoop_exec invert;;
        "3")  hadoop_exec index;;
        "4")  hadoop_exec merge;;
        "5")  hadoop_copy_outputs $*;;
        "6")  sort_indexes $*;;
        "7")  index_prunning $*;;
        "8")  copy_stopwords $*;;
        "9")  create_blacklist $*;;
        "10") request_disk $*;;
        "11") copy_to_disk $*;;
        "12") create_md5sum $*;;
        "13") close_disk $*;;
        "14") create_report $*;;
    esac
}

# process arguments
showopts(){
    while getopts ":l:c:dt:" optname
    do
        case "$optname" in
            "?") usage;;
            "c") COLLECTION_NAME=$OPTARG ;;
            "d") DEBUG=true ;;
            "t") collection_type $OPTARG ;;
            "l") HASLOG=false ;;
            ":") echo -e "No argument value for option $OPTARG\n"; usage;;
            *) # Should not occur
            echo "Unknown error while processing options";;
        esac
    done
    return $OPTIND
}

showopts "$@"
argstart=$?
argvs=${@:$argstart}

if [ -z $COLLECTION_TYPE ]; then echo "Error: collection type is not defined";usage; fi
if [ -z $COLLECTION_NAME ]; then echo "Error: collection name is not defined";usage; fi

if [ "$PWD" != "$COLLECTION_FOLDER" ]; then
    cd $COLLECTION_FOLDER
fi

if [ ! -e $COLLECTION_NAME ]; then
    if [ "$(basename $PWD)" != "$COLLECTION_NAME" ]; then
        mkdir "$COLLECTION_NAME"
        cd "$COLLECTION_NAME"
    fi
else
    cd "$COLLECTION_NAME"
fi

outputs="outputs_$COLLECTION_NAME"
dest_folder="/data/$COLLECTION_NAME/$outputs"

STOP_WHILE=false
while ! $STOP_WHILE; 
do
   process_phase $argvs
   sleep 20m
done
