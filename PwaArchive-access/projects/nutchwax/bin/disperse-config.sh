#/usr/bin
#
# Disperses HADOOP_CONF_DIR content across cluster.
#
# $Id: disperse-config.sh 1332 2006-11-20 21:42:35Z stack-sf $
if [ "${HADOOP_CONF_DIR}" = "" ]
then
    echo "Define HADOOP_CONF_DIR"
    exit 1
fi
source "${HADOOP_CONF_DIR}/hadoop-env.sh"
for i in `cat ${HADOOP_CONF_DIR}/slaves `
do 
    echo $i
    rsync -av --delete "${HADOOP_CONF_DIR}/" $i:${HADOOP_CONF_DIR}
done
