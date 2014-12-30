#!/bin/sh

#verifies number of arguments
if [ $# -ne 0 ]; then
  echo Usage: $0
  exit 127
fi

DATE=`date +%d%m%Y\-%H\-%M`
PLONE_DIR=/home/plone/Plone-3.0.5
BACKUP_DIR=/shareT2/backups/zopeploneapache
FILES="$PLONE_DIR/ /etc/httpd/conf/ /etc/httpd/conf.d/ /etc/init.d/zopestartup \
       /root/scripts/ /var/log/httpd/ /var/log/logfile.1261440000 /etc/awstats/awstats.arquivo-web.fccn.pt.conf"
#FILES="$PLONE_DIR/zinstance/var/Data.fs $PLONE_DIR/zinstance/lib/python/plone/app/i18n/locales/browser/ $PLONE_DIR/zinstance/lib/python/plone/app/layout/viewlets/ \
#       $PLONE_DIR/zinstance/Products/ /etc/httpd/conf/ /etc/httpd/conf.d/ /etc/init.d/zopestartup /home/plone/Plone-3.0.5/zinstance/etc/ \
#       /home/plone/Plone-3.0.5/lib/python/ZServer/HTTPResponse.py /root/scripts/ /home/plone/Plone-3.0.5/zinstance/log/ /var/log/httpd/ /var/log/logfile.0946080000 \
#       /etc/awstats/awstats.arquivo-web.fccn.pt.conf $PLONE_DIR/zinstance/lib/python/plone/browserlayer/configure.zcml"
#DAILY_BACKUP_DIR=/root/backups
#DAILY_FILES="/var/log/httpd/ /var/log/logfile.0946080000"


if [ -d $BACKUP_DIR ]; then
#do nothing
 echo > /dev/null
else
  mkdir $BACKUP_DIR
fi

#if [ -d $DAILY_BACKUP_DIR ]; then
##do nothing
# echo > /dev/null
#else
#  mkdir $DAILY_BACKUP_DIR
#fi

#tar -cPzpf $DAILY_BACKUP_DIR/apache-logs-$DATE.tar.gz $DAILY_FILES

#tar -cPzpf $BACKUP_DIR/zopePloneApache-$DATE.tar.gz $FILES
tar -czpf $BACKUP_DIR/zopePloneApache-$DATE.tar.gz $FILES


