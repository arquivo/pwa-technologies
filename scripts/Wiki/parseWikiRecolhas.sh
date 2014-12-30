#!/bin/sh

#what this script does:
INFO="This script parses a file saved from the wiki page Recolhas and outputs info about the collection, showing the one expected and the one found"

#if this script stops working, check the page version of http://wiki.priv.fccn.pt/Recolhas
#on the wiki history for the date 26-08-2014 because it expects the page in that format

#check arguments
if [ $# -ne 1 ]
then
    echo "$INFO"
    echo "USAGE:" $0 "InputFile" 
    exit 3
fi


#parse arguments
WIKIPAGE=$1

COLLECTIONBULLETSTXT="collectionlocationbullets.txt"
INDEXBULLETSTXT="indexlocationbullets.txt"
ERRORFILE="parseWikiRecolhas.errors.txt"

#function that indents bullets according to the number of ul elements found and cleans tags
indentBullets(){
    INDENT=0
    BULLETS=$*

    echo $BULLETS | sed 's/<ul>/\n<ul>/g' | sed 's/<li>/\n<li>/g' | sed '/^$/d' | sed 's/<\/ul>/\n<\/ul>/g' |
    while read LINE
    do
        #closing of ul means one less level of indentation
        if [[ "$LINE" =~ \<\/ul\>.* ]]
        then
          ((INDENT--))
        fi

        #opening of ul means one more level of indentation
        if [[ "$LINE" =~ \<ul\>.* ]]
        then
          ((INDENT++))
        fi

        #print line with corresponding level of indentation
        LINE=`echo $LINE | sed 's/ *</</g' | sed 's/> */>/g' | sed 's/<\/\?ul>//g' | sed 's/<\/\?li>//g' | sed 's/^$//g' | grep -v "^$"`
        if [ -n "$LINE" ]
        then
          INDENTMARGIN=$(($INDENT * 2))
          printf '%0.s  ' $(seq 1 $INDENTMARGIN)
          echo $LINE 
        fi
    done
}


# Verify if last command was correct
verifycorrect(){
    ERROR=$1
    if [[ $ERROR != 0 ]]
    then
      case "$ERROR" in
        "4") ERRORDESC="Remote file or directory not found" ;;
        "255") ERRORDESC="Name or service not known" ;;
        "*") ERRORDESC="I don't know this error yet, please add it" ;;
      esac
      echo "ERROR: $2: $ERRORDESC"
      exit $ERROR
    fi
}

#split numbers in groups of three digits
splitNumberThreeDigits(){
    NUMBER=$1
    echo $NUMBER | sed -r ':a;s/([0-9])([0-9]{3}([^0-9]|$))/\1 \2/;ta'
}

#remotely connect to a machine and get data about a collection
remoteConnect(){
    LOCATIONLINE=$1
    TYPE=$2

    #check if it's a location
    if [[ $LOCATIONLINE == *:/*  ]]
    then
  
        #strip garbage
        LOCATIONLINE=`echo $LOCATIONLINE | sed 's/ .*//g' `

        #retrieve machine
        MACHINE=`echo $LOCATIONLINE | sed 's/:.*//g' `

        #retrieve path
        REMOTEPATH=`echo $LOCATIONLINE | sed 's/.*://g' `

        #connect to machine, testing if it can ssh to it and if remote dir exists
        #if it does, get the number of files found 
        COMMAND="find $REMOTEPATH | wc -l"
        COMMANDEXT="if [ -e $REMOTEPATH ] ; then $COMMAND ; else exit 4 ; fi" 
        NUMFILES=`ssh $MACHINE $COMMAND < /dev/null 2>> $ERRORFILE`
        verifycorrect $? "Could not ssh to server $MACHINE and execute command '$COMMAND'"
        NUMFILES=`splitNumberThreeDigits $NUMFILES`

        #then get the diskspace occupied
        COMMAND="du -chs $REMOTEPATH | tail -1 | sed 's/\t.*//g'"
        COMMANDEXT="if [ -e $REMOTEPATH ] ; then $COMMAND ; else exit 4 ; fi"
        DISKSPACE=`ssh $MACHINE $COMMANDEXT < /dev/null 2>> $ERRORFILE`
        verifycorrect $? "Could not ssh to server $MACHINE and execute command '$COMMAND'"

        #and then, if it's a collection location, print the number of arcs too
        if [ "$TYPE" == "collection" ]
        then
            COMMAND="find $REMOTEPATH | grep \"arc.gz$\" | wc -l"
            COMMANDEXT="if [ -e $REMOTEPATH ] ; then $COMMAND ; else exit 4 ; fi"
            NUMARCS=`ssh $MACHINE $COMMANDEXT < /dev/null 2>> $ERRORFILE`
            verifycorrect $? "Could not ssh to server $MACHINE and execute command '$COMMAND'"
            NUMARCS=`splitNumberThreeDigits $NUMARCS`    

            echo "| $NUMFILES Files | $NUMARCS Arcs | $DISKSPACE Disk Space"
        else
            echo "| $NUMFILES Files | $DISKSPACE Disk Space"
        fi
    fi
}

#if it's a collection call the remoteConnect as a collection
remoteConnectCollection(){
    remoteConnect $1 collection
}

#if it's an index call the remoteConnect as an index
remoteConnectIndex(){
    remoteConnect $1 index
}


#parse file, retrieve table and select collumns with the collection name, collection location, 
# index location, number of documents, number of arcs and diskspace
cat $WIKIPAGE | sed -n '/^<table align=/,/^<table align=/p' | sed 's/\t/ /g' | sed 's/<\/\?strike>//g' | sed 's/<td style="background-color: red">/<td>/g' \
              | sed ':a;N;$!ba;s/\n/ /g' | sed 's/<tr>/\n<tr>/g' | sed 's/<\/td>/\t<\/td>/g' | sed 's/<\/\?b>//g' | cut -f2,5,6,7,8,9 -s |
    while read LINE
    do
        #assign each tab separated field to each variable
        COLLECTIONNAME=`echo $LINE | sed 's/<\/td><td>/\t/g' | sed 's/^\t//g' | awk -F$'\t' '{ print $1 }'`
        COLLECTIONLOCATION=`echo $LINE | sed 's/<\/td><td>/\t/g' | sed 's/^\t//g' | awk -F$'\t' '{ print $2 }'`
        INDEXLOCATION=`echo $LINE | sed 's/<\/td><td>/\t/g' | sed 's/^\t//g' | awk -F$'\t' '{ print $3 }'`
        NUMDOCS=`echo $LINE | sed 's/<\/td><td>/\t/g' | sed 's/^\t//g' | awk -F$'\t' '{ print $4 }'`
        NUMARCS=`echo $LINE | sed 's/<\/td><td>/\t/g' | sed 's/^\t//g' | awk -F$'\t' '{ print $5 }'`
        DISKSPACE=`echo $LINE | sed 's/<\/td><td>/\t/g' | sed 's/^\t//g' | awk -F$'\t' '{ print $6 }'`

        #reformat output for locations
        COLLECTIONLOCATION=`indentBullets $COLLECTIONLOCATION` 
        echo "$COLLECTIONLOCATION" > $COLLECTIONBULLETSTXT
        INDEXLOCATION=`indentBullets $INDEXLOCATION`
        echo "$INDEXLOCATION" > $INDEXBULLETSTXT

        #print info
        echo "========START $COLLECTIONNAME========" 
        echo "Collection:" $COLLECTIONNAME
        echo "Number of documents:" $NUMDOCS
        echo "Number of arc files:" $NUMARCS
        echo "Disk space occupied by the collection (TB):" $DISKSPACE
        echo "Collection location:"

        #remote connect to machine if it is machine, to get data for the collection in that location
        #change IFS to preserve whitespaces but unset it to enable ssh
        IFS='%'   
        while read LOCATION 
        do
            unset IFS
            COLLECTIONDATA=`remoteConnectCollection $LOCATION`
            IFS='%'   
            echo "$LOCATION $COLLECTIONDATA"
        done < $COLLECTIONBULLETSTXT
        unset IFS

        echo "Index location:" 
        #remote connect to machine if it is machine, to get data for the indexes in that location
        #change IFS to preserve whitespaces but unset it to enable ssh
        IFS='%'   
        while read LOCATION 
        do
            unset IFS
            INDEXDATA=`remoteConnectIndex $LOCATION`
            IFS='%'   
            echo "$LOCATION $INDEXDATA"
        done < $INDEXBULLETSTXT
        unset IFS

        echo "=========END $COLLECTIONNAME=========" 
        echo ""
    done

rm $COLLECTIONBULLETSTXT
rm $INDEXBULLETSTXT
