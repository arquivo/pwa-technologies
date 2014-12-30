#!/bin/sh

#verifies number of arguments
if [ $# -ne 1 ]; then
  echo Usage: $0 CRAWL_LOG
  exit 127
fi

CRAWL_LOG=$1
ERROR_FILE=`echo $CRAWL_LOG.error`

#URI varchar(2000)
URI_LIM=2000

#DISCOVERYPATH varchar(20)
DISCOVERYPATH_LIM=20

#REFERRER varchar(2000)
REFERRER_LIM=2000

#MIMETYPE varchar(50)
MIMETYPE_LIM=50

#THREADID varchar(5)
THREADID_LIM=5

#DIGEST varchar(60)
DIGEST_LIM=60

#SOURCETAG varchar(100)
SOURCETAG_LIM=100

#ANNOTATIONS varchar(1000)
ANNOTATIONS_LIM=1000

#FQDN varchar(1000))
FQDN_LIM=1000


#read file and assign values to variables
#NR holds current line number given by awk
awk '{ TIMESTAMP=$1; FETCHCODE=$2; SIZE=$3; URI=$4; DISCOVERYPATH=$5; REFERRER=$6; MIMETYPE=$7; THREADID=$8; FETCHTIMESTAMP=$9; DIGEST=$10; SOURCETAG=$11; 
ANNOTATIONS=$12; print TIMESTAMP , FETCHCODE , SIZE , URI , DISCOVERYPATH , REFERRER , MIMETYPE , THREADID , FETCHTIMESTAMP , DIGEST , SOURCETAG , ANNOTATIONS , NR }' $CRAWL_LOG | \
while read TIMESTAMP FETCHCODE SIZE URI DISCOVERYPATH REFERRER MIMETYPE THREADID FETCHTIMESTAMP DIGEST SOURCETAG ANNOTATIONS NR
do

  AUX=`echo $URI | sed 's/^dns:/http:\/\//g' | cut -d '/' -f1,2,3`
  FQDN=`echo $AUX | cut -d ':' -f1,2`
  PORT=`echo $AUX | cut -d ':' -f3`

#URI
  if [ ${#URI} -gt $URI_LIM ]; then
    echo "URI length on line $NR longer than limit: ${#URI} longer than $URI_LIM chars" >> $ERROR_FILE
    echo $URI >> $ERROR_FILE
    URI=`echo ${URI:0:$URI_LIM}`
  fi

#DISCOVERYPATH varchar(20)
  if [ ${#DISCOVERYPATH} -gt $DISCOVERYPATH_LIM ]; then
    echo "DISCOVERYPATH length on line $NR longer than limit: ${#DISCOVERYPATH} longer than $DISCOVERYPATH_LIM chars" >> $ERROR_FILE
    echo $DISCOVERYPATH >> $ERROR_FILE
    DISCOVERYPATH=`echo ${DISCOVERYPATH:0:$DISCOVERYPATH_LIM}`
  fi

#REFERRER varchar(2000)
  if [ ${#REFERRER} -gt $REFERRER_LIM ]; then
    echo "REFERRER length on line $NR longer than limit: ${#REFERRER} longer than $REFERRER_LIM chars" >> $ERROR_FILE
    echo $REFERRER >> $ERROR_FILE
    REFERRER=`echo ${REFERRER:0:$REFERRER_LIM}`
  fi

#MIMETYPE varchar(50)
  if [ ${#MIMETYPE} -gt $MIMETYPE_LIM ]; then
    echo "MIMETYPE length on line $NR longer than limit: ${#MIMETYPE} longer than $MIMETYPE_LIM chars" >> $ERROR_FILE
    echo $MIMETYPE >> $ERROR_FILE
    MIMETYPE=`echo ${MIMETYPE:0:$MIMETYPE_LIM}`
  fi

#THREADID varchar(5)
  if [ ${#THREADID} -gt $THREADID_LIM ]; then
    echo "THREADID length on line $NR longer than limit: ${#THREADID} longer than $THREADID_LIM chars" >> $ERROR_FILE
    echo $THREADID >> $ERROR_FILE
    THREADID=`echo ${THREADID:0:$THREADID_LIM}`
  fi

#DIGEST varchar(60)
  if [ ${#DIGEST} -gt $DIGEST_LIM ]; then
    echo "DIGEST length on line $NR longer than limit: ${#DIGEST} longer than $DIGEST_LIM chars" >> $ERROR_FILE
    echo $DIGEST >> $ERROR_FILE
    DIGEST=`echo ${DIGEST:0:$DIGEST_LIM}`
  fi

#SOURCETAG varchar(100)
  if [ ${#SOURCETAG} -gt $SOURCETAG_LIM ]; then
    echo "SOURCETAG length on line $NR longer than limit: ${#SOURCETAG} longer than $SOURCETAG_LIM chars" >> $ERROR_FILE
    echo $SOURCETAG >> $ERROR_FILE
    SOURCETAG=`echo ${SOURCETAG:0:$SOURCETAG_LIM}`
  fi

#ANNOTATIONS varchar(1000)
  if [ ${#ANNOTATIONS} -gt $ANNOTATIONS_LIM ]; then
    echo "ANNOTATIONS length on line $NR longer than limit: ${#ANNOTATIONS} longer than $ANNOTATIONS_LIM chars" >> $ERROR_FILE
    echo $ANNOTATIONS >> $ERROR_FILE
    ANNOTATIONS=`echo ${ANNOTATIONS:0:$ANNOTATIONS_LIM}`
  fi

#FQDN varchar(1000))
  if [ ${#FQDN} -gt $FQDN_LIM ]; then
    echo "FQDN length on line $NR longer than limit: ${#FQDN} longer than $FQDN_LIM chars" >> $ERROR_FILE
    echo $FQDN >> $ERROR_FILE
    FQDN=`echo ${FQDN:0:$FQDN_LIM}`
  fi

##no need to cut if second timestamp is "-" since cut returns the whole string if delimiter is not found
  FETCH_TIME="-"
  FETCH_DURATION="-"

  echo $FETCHTIMESTAMP | awk '{ split($0, a, "+"); print a[1] , a[2] }' | while read FETCH_TIME FETCH_DURATION
  do
  if [ $FETCH_DURATION ]; then
#    echo "fetch time foi $FETCH_TIME"
#    echo "fetch duration foi $FETCH_DURATION"
    FETCH_TIME=$FETCH_TIME
    FETCH_DURATION=$FETCH_DURATION
  else
#    echo "fetch foi nula ->$FETCH_DURATION<-"
    FETCH_TIME="-"
    FETCH_DURATION="-"
  fi


##convert timestamp to ISO8601 format
  string=$FETCH_TIME
  if [ $string != '-' ]; then
    FETCH_TIME=`echo ${string:0:4}-${string:4:2}-${string:6:2}T${string:8:2}:${string:10:2}:${string:12:2}.${string:14:3}Z`
  fi


#PORT
  if [ ! $PORT ]; then
#    echo "tem porto"
#  else 
#    echo "nao tem porto"
    PORT="80"
  fi

##convert to CSV
echo "$TIMESTAMP $FETCHCODE $SIZE $URI $DISCOVERYPATH $REFERRER $MIMETYPE $THREADID $FETCH_TIME $FETCH_DURATION $DIGEST $SOURCETAG $ANNOTATIONS $FQDN $PORT" | \
sed 's/  */","/g' | sed -e 's/^/"/g; s/$/"/g' | sed 's/,"-"/,-/g'

  done
done
