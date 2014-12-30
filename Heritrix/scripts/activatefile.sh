#!/bin/sh

#This script "activates" one "inactive" file by copying it to the "active" file
#
#For instance, if the file used by a program is "file.txt" and we have two possible files for that purpose
#that should change according to different dates, "fileA.txt" and "fileB.txt", then we replace "file.txt" with 
#the one we want
#
#This is useful, for instance, to change the order.xml file in Heritrix daily crawls in which we decided
#to perform the crawl without DeDuplicator on the first day of each trimester and with DeDuplicator in 
#the rest of the year

#test arguments
if [ $# -ne 2 ]
then
  echo "USAGE:" $0 "INPUT_FILE OUTPUT_FILE"
  exit 127
fi

INPUTFILE=$1
OUTPUTFILE=$2

cp $INPUTFILE $OUTPUTFILE

