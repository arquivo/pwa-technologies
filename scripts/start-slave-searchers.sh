#!/bin/bash
# Starts the hadoop search servers
#
# Portuguese web archive | http://arquivo.pt
# Simao Fontes

if [ "${COLLECTIONS_DIR}" = "" ]
then
	export COLLECTIONS_DIR=/opt/searcher/collections
fi

if [ "${HADOOP_FOR_NUTCH_SERVERS_PID}" = "" ]
then
	export HADOOP_FOR_NUTCH_SERVERS_PID=/opt/searcher/run/hadoopserver.pid
fi

if [ -f ${COLLECTIONS_DIR}/SearchAll/search-servers.txt ]; then
	rm ${COLLECTIONS_DIR}/SearchAll/search-servers.txt
fi

cat "${COLLECTIONS_DIR}/search-servers.txt" | while read line
do
    echo "Starting ${line}"
    
   
    server=`echo $line|awk '{print $1}'`
    port=`echo $line|awk '{print $2}'`
    HADOOP_HOME=`echo $line|awk '{print $3}'`
    HADOOP_FOR_NUTCH_SERVERS_HOME=`echo $line|awk '{print $3}'`
    HADOOP_FOR_NUTCH_SERVERS_CONF_DIR="$HADOOP_FOR_NUTCH_SERVERS_HOME/conf"
    NUTCHWAX_LOG_DIR="/opt/searcher/logs"
    NUTCHWAX_INDEXES_DIR=`echo $line|awk '{print $4}'`
    
    . "${HADOOP_FOR_NUTCH_SERVERS_CONF_DIR}"/hadoop-env.sh
    nohup bash ${HADOOP_FOR_NUTCH_SERVERS_HOME}/bin/hadoop --config ${HADOOP_FOR_NUTCH_SERVERS_CONF_DIR} jar \
        ${HADOOP_FOR_NUTCH_SERVERS_HOME}/nutchwax-job-0.11.0-SNAPSHOT.jar class \
        'org.archive.access.nutch.NutchwaxDistributedSearch$Server' $port \
        ${NUTCHWAX_INDEXES_DIR} &> ${NUTCHWAX_LOG_DIR}/slave-searcher-$port.log & echo -n "$! " >> ${HADOOP_FOR_NUTCH_SERVERS_PID}

    echo $server $port >> ${COLLECTIONS_DIR}/SearchAll/search-servers.txt

#    if [ "${HADOOP_FOR_NUTCH_SERVERS_PID}" = "" ]
#       ps -fe | grep 'NutchwaxDistributedSearch' | head -n1 | cut -d" " -f 6 >> ${HADOOP_FOR_NUTCH_SERVERS_PID}
    #${HADOOP_FOR_NUTCH_SERVERS_HOME}/start-slave-searcher.sh ${port} &
done



