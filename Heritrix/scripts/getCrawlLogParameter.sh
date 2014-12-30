#!/bin/sh

echo 'gets a given parameter from the Heritrix crawl.log'
echo 'usage: getCrawlLogParameter.sh FILE PARAMETER_NUMBER'
cat $1|sed 's/  */ /g'|cut -d " " -f$2

