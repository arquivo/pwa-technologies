#!/bin/sh      
#author:dgomes                                                                                            
#USAGE: generateItems.sh CRAWL_NAME DIRECTORY_OF_THE_CRAWL COLLECTION
#output format: ITEMNAME FILENAME MD5  
# IAS3 access keys must be set on environment (~/.bashrc)
# AWS_ACCESS_KEY_ID
# AWS_SECRET_ACCESS                                    
if [ $# -ne 1 ]; then
  echo "Usage: $0 CONFIG_FILE"
  exit 127
fi

source $1
CRAWL_NAME=$CRAWL_NAME
DIRECTORY_OF_THE_CRAWL=$DIRECTORY_OF_THE_CRAWL
COLLECTION=$COLLECTION
YEAR=$YEAR
OUTPUTFILE=$OUTPUTFILE

#list arc files                                           
cd "$DIRECTORY_OF_THE_CRAWL"
ls -m1 -X *.arc.gz > /tmp/crawlFiles.txt
nuploadedfiles=0

#create item 
while read line 
do
        FILENAME=$line
        if [[ "$nuploadedfiles" -gt 99 ]] || [[ "$nuploadedfiles" == 0 ]]; then
        #get date for item name                                                                            
        #NOTE: this depends on the crawl file format. May have to be adapted                                                                  
        ITEMDATE=$(echo "$FILENAME"|cut -d "-" -f 2)
        #item name                                
        ITEMNAME="$COLLECTION-$CRAWL_NAME$YEAR-$ITEMDATE"
		nuploadedfiles=0
        fi

#generate md5                                 
MD5=`md5sum $FILENAME | awk '{ print $1 }'`
echo "$ITEMNAME $FILENAME $MD5" >> $OUTPUTFILE
nuploadedfiles=$[$nuploadedfiles +1]
done < /tmp/crawlFiles.txt


