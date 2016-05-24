#!/bin/bash
#
# $1 - file miss DOCNO

for file in `grep 'Missing' $1 | sort -u | cut -c 15-`
do
  grep $file ~/arcs/GOV1/url2id >> xxx 

done
