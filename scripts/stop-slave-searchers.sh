#!/bin/bash
# Script to stop all search-servers.
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

cat "${COLLECTIONS_DIR}/search-servers.txt" | while read line
do
    NUTCHWAX_INDEXES_DIR=`echo $line|awk '{print $4}'`
    NUTCHWAX_INDEXES_DIR=$(echo $NUTCHWAX_INDEXES_DIR | sed 's/\//\\\//g')
    ps -C java -fwwwH | sed -ne '/'"$NUTCHWAX_INDEXES_DIR"'/p' | awk '{print $2}' | xargs kill -9
    rm -f ${HADOOP_FOR_NUTCH_SERVERS_PID}
done

