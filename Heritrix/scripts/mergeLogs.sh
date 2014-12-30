#!/bin/sh

INFO="This script merges the crawl logs into a file from a given crawl"
EXAMPLE="Example: mergeLogs.sh /awpsdata/AWP15 AWP15 ForaPT"

if [ $# -ne 3 ]
then
    echo "$INFO"
    echo "USAGE: $0 LOGDIR CRAWLNAME PART"
    echo "$EXAMPLE"
    exit 3
fi

#process the arguments
LOGDIR=$1
CRAWLNAME=$2
PART=$3

#find the files and merge them into a file
find $LOGDIR -name "crawl.log.*" | sort | xargs cat > $CRAWLNAME.$PART.crawl.log
find $LOGDIR -name "crawl.log" | xargs cat >> $CRAWLNAME.$PART.crawl.log

