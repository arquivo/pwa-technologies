#!/bin/sh

#This script is used to compare hardware dependencies and the information available on our wiki
#It connects automatically using the exec_command available on the share_t2 and a file given
# by the user with the wiki page of the hardware table

#check parameters 
if [ $# -ne 1 ]
then
  echo "USAGE:" $0 "WIKI_FILE"
  exit 127
fi

FSFILEALL="fstab_all.txt"
FSFILEPARSED="fs.txt"
WIKIPARSED="wiki.txt"

#download wiki web page with hardware information
#can't be done because it's available only for the 44 network
WIKIFILE=$1

#parse the wiki table and get dependencies for each machine
parsewikihardwaredep.sh $WIKIFILE | sort -u > $WIKIPARSED

#connect to every machine and check dependencies for each machine
/shareT2/scripts/execCommand_all.sh "cat /etc/fstab | sed '/^$/d'" > $FSFILEALL

parsefstab.sh $FSFILEALL | sort -u > $FSFILEPARSED

#compare the info from the machines with the one on the wiki
echo -e "WIKI\tFSTAB\tBOTH"
comm $WIKIPARSED $FSFILEPARSED

#alert if differences were found
###TODO

#remove temp files
rm $FSFILEALL
rm $FSFILEPARSED
rm $WIKIPARSED

