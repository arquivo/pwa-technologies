#!/bin/sh
#
# Run a df across the cluster.
#
# $Id: df-slaves.sh 1342 2006-12-07 22:27:03Z uid143487 $
#
if [ "${HADOOP_CONF_DIR}" = "" ]
then
    echo "Set HADOOP_CONF_DIR"
    exit 1
fi
if [ ! -d ${HADOOP_LOG_DIR} ]
then
    echo Set HADOOP_LOG_DIR
    exit 1
fi
for i in `cat ${HADOOP_CONF_DIR}/slaves `
do 
    ssh $i 'hostname; df -h'
done
