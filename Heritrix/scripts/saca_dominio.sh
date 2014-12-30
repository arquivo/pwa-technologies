#!/bin/sh
#get URIs from file under a specified domain

#verifies number of arguments
if [ $# -ne 2 ]; then
#  echo saca.sh - Get URIs from file under a specified domain
 echo Usage: $0 INPUT_FILE DOMAIN      
 exit 127       
fi  

INPUT_FILE=$1
DOMAIN=$2
#executes command
#cat $1 | tr ";" "\n" | grep '.' | tr "\t" " " | tr " " "\n" | sed 's/^[ ]*//;s/[ ]*$//' | tr "[:upper:]" "[:lower:]" | grep -v -i "não tem" | sed 's/^/http:\/\//' | sed 's/http:\/\/http:\/\//http:\/\//g' | sed 's/\/$//g' | egrep -i "\.$2$|.*\.$2/.*" | sort -u

cat $INPUT_FILE | tr ";" "\n" | grep '.' | tr "\t" " " | tr " " "\n" | sed 's/^[ ]*//;s/[ ]*$//' | tr "[:upper:]" "[:lower:]" | grep -v -i "não tem" | sed 's/^/http:\/\//' | sed 's/http:\/\/http:\/\//http:\/\//g' | sed 's/\/$//g' | egrep -i "\.$DOMAIN$|\.$DOMAIN\:[0-9][0-9]?[0-9]?[0-9]?$" | sort -u



#meaning
#cat $1                                     -cats specified document
#tr ";" "\n"                                -replaces ";" with "\n" for .csv contents
#grep '.'                                   -outputs non-empty lines
#tr "\t" " "                                -replaces any existing tabs with spaces (in case there is more than one URI per line)
#tr " " "\n"                                -replaces all spaces with newline
#sed 's/^[ ]*//;s/[ ]*$//'                  -removes trailing spaces
#tr "[:upper:]" "[:lower:]"                 -converts to lower case
#grep -v -i "não tem"                       -removes string "não tem" existing in some seed files
#sed 's/^/http:\/\//'                       -inserts "http://" at the beginning of each line
#sed d 's/http:\/\/http:\/\//http:\/\//g'   -removes duplicate "http://" in case it already existed
#sed 's/\/$//g'                             -removes any slash at the end of lines
#egrep -i "\.$2$|.*\.$2/.*"                 -greps case insensitive the specified domain (eg: pt, PT, com, COM)
#sort -u                                    -sorts and removes dups
