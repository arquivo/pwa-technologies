#!/bin/sh
#get seeds to new crawl

#verifies number of arguments
if [ $# -ne 1 ]; then
  echo Usage: $0 CRAWL_LOG 
  exit 127
fi

CRAWL_LOG=$1
#OUTPUT_FILE=$2

cat $CRAWL_LOG | sed 's/  */ /g'| cut -d ' ' -f2,4 | grep "200 " | cut -d ' ' -f2 | grep http:// | cut -d '/' -f1,2,3 | sort -u | egrep -v "^http://www\.$" | egrep -v "^http://www$" 
#> $OUTPUT_FILE   


#meaning
#cat $1                     -cats specified file
#sed 's/  */ /g'            -removes multiple spaces
#cut -d ' ' -f2,4           -selects state and url columns
#grep "200 "                -selects the ones with 200 code
#cut -d ' ' -f2             -selects url
#grep http://               -selects the ones with http (other protocols are no good?...)
#cut -d '/' -f1,2,3         -selects only url, stripping the rest of the adress (http://www.site.com/index.html becomes http://www.site.com)
#sort -u > $2               -sorts and removes dups

