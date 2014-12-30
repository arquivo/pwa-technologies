#!/bin/sh

#receives three arguments: location of dedupdigest, location of heritrix jobs directory, location of where to store the index 
#if anything goes wrong, it should warn me by email

#verifies number of arguments
if [ $# -ne 3 ]; then
  echo USAGE: $0 DEDUPDIGEST_PATH HERITRIX_JOBS_DIRECTORY INDEX_OUTPUT_DIRECTORY
  exit 127
fi
DEDUPPATH=$1
HERJOBSDIR=$2
INDEXDIR=$3

export JAVA_HOME=/usr/java/default/jre
export JAVA_OPTS="-d64 -Xmx2g"

#verifies if it has a slash at the end, if it doesn't, add it
case "$HERJOBSDIR" in
*/)
  #has slash
  ;;
*)
  #doesn't have a slash, add it
  HERJOBSDIR="$HERJOBSDIR/" 
  ;;
esac

#find the crawl to be made
JOB=`ls -ltr $HERJOBSDIR | tail -1 | sed 's/  */ /g' | cut -d ' ' -f9`

#this is being done for FAWP crawls so it only has a crawl.log file (no need to append craw.log.0001, etc)
CRAWLLOG="$HERJOBSDIR$JOB/logs/crawl.log"

#run the dedupdigest script, which takes 2 arguments: the craw.log to process and the index output directory
$DEDUPPATH -w -m".*" -s -t $CRAWLLOG $INDEXDIR > /dev/null 
