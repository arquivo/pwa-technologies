#!/bin/sh

#this script parses the output of doing cat /etc/fstab on all machine using execCommand
#this aux script is used by checkWikiHardwareDependencies.sh

#check arguments
if [ $# -ne 1 ]
then
  echo "USAGE:" $0 "INPUT_FILE"
  exit 127
fi

FSFILE=$1
PARSETEMP=$FSFILE".tmp"

#read the file in blocks separated by blank lines 
#the first line is the header for each block and it has the machine name, e.g. t1.tomba.fccn.pt, which will
#be repeated in the beginning of each line
BLOCK=""
HEADER=0
HEADERLINE=""

while read LINE
do
  if [ -n "$LINE" ]
  then
    if [ $HEADER -eq 0 ] 
    then
      HEADERLINE=`echo $LINE | cut -d '.' -f1`    #use only the t1 instead of t1.tomba.fccn.pt
      HEADER=1
    else
      if [[ "$LINE" =~ \#.* ]]
      then       #it's a comment line, discard it
         :  #do nothing
      else
        echo $HEADERLINE $LINE >> $PARSETEMP
      fi
    fi
  else
    HEADER=0
    HEADERLINE=""
  fi
done < $FSFILE

#parse that output, leave only non local filesystems and adjust the output format
cat $PARSETEMP | grep -v "^#" | cut -d ' ' -f 1,2,3,4 | awk '{ print $3 " " $2 " " $4 " " $1 }' | grep -v "^/ " | grep -v "/boot " | grep -v "/dev/shm " | grep -v "/dev/pts " | grep -v "/home " | grep -v "/var " | grep -v "/proc " | grep -v "/sys " | grep -v "^swap " | grep "^/" | awk '{ print $4 " " $1 }' | sed 's/\/$//g' | sort

#remove temp files
rm $PARSETEMP
