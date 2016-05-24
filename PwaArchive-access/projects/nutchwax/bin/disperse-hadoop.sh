#/usr/bin
#
# Disperses hadoop around the cluster.
#
# $Id: disperse-hadoop.sh 1332 2006-11-20 21:42:35Z stack-sf $
#
if [ "${HADOOP_CONF_DIR}" = "" ]
then
    echo "Define HADOOP_CONF_DIR"
    exit 1
fi
source "${HADOOP_CONF_DIR}/hadoop-env.sh"
if [ "${HADOOP_HOME}" = "" ]
then
    echo "Define HADOOP_HOME in hadoop-env.sh"
    exit 2
fi
if [ ! -d ${HADOOP_HOME} ]
then 
    echo "$HADOOP_HOME does not exist"
    exit 3
fi
for i in `cat ${HADOOP_CONF_DIR}/slaves `
do 
    rsync -av  "${HADOOP_HOME}/" $i:${HADOOP_HOME}
done
