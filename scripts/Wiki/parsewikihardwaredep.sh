#!/bin/sh

#this script parses the wiki hardware table 
#http://wiki.priv.fccn.pt/Hardware_do_Tomba#Storage_em_utiliza.C3.A7.C3.A3o_pelo_AWP
#from a presaved file (you can save the whole wiki page to a file)
#it is used by checkWikiHardwareDependencies.sh
#if this script stops working, check the page version on the wiki history for the date 06-08-2014 because 
#it expects the page in that format

#check file parameters
if [ $# -ne 1 ]
then 
  echo "USAGE:" $0 "INPUT_FILE"
  exit 127
fi

WIKIFILE=$1
WIKIFILECLEAN=$WIKIFILE".clean.tmp"
WIKIFILEBLOCKS=$WIKIFILE".blocks.tmp"
WIKIFILEPARTITIONS=$WIKIFILE".partitions.tmp"
WIKIFILEPARTITIONSPARSED=$WIKIFILE".partitions.parsed.tmp"

cat $WIKIFILE | sed -n '/^<a name=\"Tabela_de_depend/,/^Obtido em \"<a href=\"http/p' | sed -n '/^<table/,/^<\/td><\/tr><\/table>/p' |   sed ':a;N;$!ba;s/\n/ /g' | sed 's/<tr/\n<tr/g' | sed 's/<td/\n<td/g' | sed 's/<tr>//g' | sed 's/<td>[ ]*<\/td>/-/g' | sed 's/<td>//g' | sed 's/<\/td>//g' | sed 's/<\/tr>//g' | sed 's/<td.*%;\"> //g' | sed 's/<\/table>//g' | sed 's/<td .*\">//g' | tail -n +3 > $WIKIFILECLEAN

#read the file in blocks separated by blank lines
#the first line is the header for each block and it has the machine name, e.g. t1, which will
#be repeated in the beginning of each line
BLOCK=""
HEADER=0
HEADERLINE=""

#separate the file in blocks per machine
while read LINE
do
  if [ -n "$LINE" ]
  then
    if [ "$LINE" != "-" ]
    then
      if [ $HEADER -eq 0 ]
      then
        HEADERLINE=$LINE
        HEADER=1
      else
        echo $HEADERLINE $LINE >> $WIKIFILEBLOCKS
      fi
    fi
  else
      HEADER=0
      HEADERLINE=""
  fi
done < $WIKIFILECLEAN

#separate the file in blocks of partitions per machine
cat $WIKIFILEBLOCKS | grep "<li>" | sed 's/<ul>//g' | sed 's/<\/ul>//g' | sed 's/<p>.*<\/p>//g' | sed "s/$/\n/g" | sed 's/<li>/\n<li>/g' | sed 's/<li> *//g' | sed 's/<\/li>//g' | sed 's/, \//\n\//g' | sed 's/ .*//g' | sed 's/\/$//g' >> $WIKIFILEPARTITIONS

#read the file to process the partitions per machine
BLOCK=""
HEADER=0
HEADERLINE=""

while read LINE
do 
  if [ -n "$LINE" ]
  then
    if [ $HEADER -eq 0 ]
    then
      HEADERLINE=$LINE
      HEADER=1
    else
      echo $HEADERLINE $LINE >> $WIKIFILEPARTITIONSPARSED
    fi    
  else
    HEADER=0
    HEADERLINE=""
  fi
done < $WIKIFILEPARTITIONS

#output the file
cat $WIKIFILEPARTITIONSPARSED | sort

#remove temp files
rm $WIKIFILECLEAN
rm $WIKIFILEBLOCKS
rm $WIKIFILEPARTITIONS
rm $WIKIFILEPARTITIONSPARSED
