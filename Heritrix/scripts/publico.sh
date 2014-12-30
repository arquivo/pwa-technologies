#!/bin/sh

#create a list of short URLs for publico.pt upt to a certain number
#publico.pt/12345 will expand to www.publico.pt/full_url_of_news_article_12345

#LIMIT=1493516
LIMIT=100

a=1

while [ $a -le $LIMIT ]
do 
  echo "http://www.publico.pt/$a"
  a=$(($a+1))
done
