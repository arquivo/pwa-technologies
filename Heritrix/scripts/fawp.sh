#!/bin/sh
#this script sets up the frequent AWP crawls


#verifies number of arguments
if [ $# -ne 12 ]; then
  echo Usage: $0 PROFILE SEEDS_PATH LOG_PATH HERITRIX_PASSWORD GUI_PORT JMX_PORT JMX_PATH HOST JOBS_PATH EMAIL_ALERT_RECIPIENT HERITRIX_START_SCRIPT NUM_RETRIES
  exit 127
fi


#PROFILE="FAWP"
PROFILE=$1
NAME="FAWP-"`date +%d-%m-%y`
DESCRIPTION="FrequentAWPcrawl-"`date +%d-%m-%y`
#SEEDS=/home/heritrix/testes/seeds.txt
SEEDS=$2
#LOG="/home/heritrix/testes/heritrix-fawp.log"
LOG=$3
PASSWORD=$4
GUI_PORT=$5
#JMX_PORT="8849"
JMX_PORT=$6
#JMX_PATH="/home/heritrix/heritrix-1.14.3_1/bin/cmdline-jmxclient-0.10.5.jar"
JMX_PATH=$7
#HOST="T7"
HOST=$8
#JOBS_PATH="/home/heritrix/heritrix-1.14.3_1/jobs/"
JOBS_PATH=$9
#you have to shift the argument list because of the 9 elements limit
shift
RECIPIENT=$9
shift
HERITRIXSTARTSCRIPT=$9
shift
RETRIES=$9

PATH=/usr/java/default/bin/:$PATH
export PATH
 
function malert {
  echo $1 | mail -s "Problem setting up a FAWP" $RECIPIENT
}

BIGRETRIES=$RETRIES
JOBRUNNING=0

while [ $JOBRUNNING -eq 0 ] && [ $BIGRETRIES -gt 0 ]; do


  #starts heritrix, runs a new job and warns me by mail if it managed to run it or not
  echo -e "\n=============================\n`date`\n=============================" >>$LOG
  echo -e "Starting a new crawl via JMX." >>$LOG
  RUNNING=0

  #while heritrix is not running we will try the specified number of times
  #we shut it down first because sometimes it fails to start the job when it is already running
  while [ $RUNNING -eq 0 ] && [ $RETRIES -gt 0 ]; do
    #check if heritrix is running
    java -jar $JMX_PATH controlRole:$PASSWORD localhost:$JMX_PORT 2>>$LOG
    EXITCODE=$?

    if [ $EXITCODE -eq 0 ]; then
      #if running shut down and wait
      echo -e "\nShutting down Heritrix...\n" >>$LOG
      java -jar $JMX_PATH controlRole:$PASSWORD localhost:$JMX_PORT org.archive.crawler:guiport=$GUI_PORT,host=$HOST,jmxport=$JMX_PORT,name=Heritrix,type=CrawlService shutdown 2>>$LOG    
      sleep 20 
    else
      echo -e "\nHeritrix is not running.\n" >>$LOG
    fi

    #then start it, decrement retries, and wait
    echo -e "\nStarting Heritrix...\n" >>$LOG
    $HERITRIXSTARTSCRIPT >>$LOG
    let RETRIES=RETRIES-1
    sleep 20
  
    #check if we got it to work
    echo -e "\nTesting if it is running now...\n" >>$LOG
    java -jar $JMX_PATH controlRole:$PASSWORD localhost:$JMX_PORT 2>>$LOG
    EXITCODE2=$?

    if [ $EXITCODE2 -eq 0 ]; then
      echo -e "\nManaged to start Heritrix. It is now running.\n" >>$LOG     
      RUNNING=1
    else
      echo -e "\nDidn't manage to start Heritrix. It is not running.\n" >>$LOG     
    fi   
    sleep 20
  done

  if [ $RUNNING -eq 1  ]; then
    sleep 10
    #creates new job based on the given profile
    echo -e "Creating a new job based on $PROFILE profile..." >>$LOG
    COMANDO=`java -jar $JMX_PATH controlRole:$PASSWORD localhost:$JMX_PORT org.archive.crawler:guiport=$GUI_PORT,host=$HOST,jmxport=$JMX_PORT,name=Heritrix,type=CrawlService addJobBasedon=$PROFILE,$NAME,$DESCRIPTION,' '  2>&1  `
    EXITCODE3=$?
    if [ $EXITCODE3 -eq 1 ]; then
      echo $COMANDO >>$LOG
      echo -e "\nNope, failed to create new job. Help needed...\n" >>$LOG
      malert "Heritrix is running but I failed to create a new job. Help needed... Check /data/heritrix/FAWP/heritrix-fawp.log and /var/spool/mail/heritrix at $HOST."
    else
      JOB=`echo $COMANDO | sed 's/  */ /g' | cut -d " " -f7`
      echo -e "OK, new job created. It was named" $NAME-$JOB >>$LOG
      
      #should we get the name from the pending jobs, instead?
      #JOB1=`java -jar $JMX_PATH controlRole:$PASSWORD localhost:$JMX_PORT org.archive.crawler:guiport=$GUI_PORT,host=$HOST,jmxport=$JMX_PORT,name=Heritrix,type=CrawlService pendingJobs 2>&1  | grep uid | cut -d " " -f3 `

      #imports seeds, since the job was created without seeds; we'll need the job's name
      JOB="$NAME-$JOB"
      echo -e "Setting Heritrix into crawling mode, so that I can add the seeds..." >>$LOG
      java -jar $JMX_PATH controlRole:$PASSWORD localhost:$JMX_PORT org.archive.crawler:guiport=$GUI_PORT,host=$HOST,jmxport=$JMX_PORT,name=Heritrix,type=CrawlService startCrawling 2>>$LOG
      EXITCODE4=$?   
      if [ $EXITCODE4 -eq 1 ]; then
        echo -e "\nFailed to set Heritrix into crawling mode. Help needed...\n" >>$LOG
        malert "I created the job but I couldn't set Heritrix into crawling mode. Help needed... Check /data/heritrix/FAWP/heritrix-fawp.log and /var/spool/mail/heritrix at $HOST."
      else
        echo -e "Heritrix is set into crawling mode." >>$LOG
        sleep 20
        echo -e "Now adding the seeds..." >>$LOG
        java -jar $JMX_PATH controlRole:$PASSWORD localhost:$JMX_PORT org.archive.crawler:host=$HOST,jmxport=$JMX_PORT,mother=Heritrix,name=$JOB,type=CrawlService.Job importUris=$SEEDS,default,false,true 2>>$LOG
        EXITCODE5=$?   
        if [ $EXITCODE5 -eq 1 ]; then
          echo -e "\nFailed to add seeds to new job. Help needed...\n" >>$LOG
          malert "Failed to add seeds to new job. Help needed... Check /data/heritrix/FAWP/heritrix-fawp.log and /var/spool/mail/heritrix at $HOST."
        else
          echo -e "Seeds added." >>$LOG
          #need the job directory here to create the surts files
          echo -e "Creating surts files..." >>$LOG
          touch $JOBS_PATH$JOB/surts.txt
          touch $JOBS_PATH$JOB/surts_rej.txt
###########missing a test here to check that surts were created
          echo -e "Surts created." >>$LOG
          sleep 10
          #now resuming job to start it, finally
          echo -e "Resuming job..." >>$LOG
          java -jar $JMX_PATH controlRole:$PASSWORD localhost:$JMX_PORT org.archive.crawler:host=$HOST,jmxport=$JMX_PORT,mother=Heritrix,name=$JOB,type=CrawlService.Job resume 2>>$LOG
          EXITCODE6=$?   
          if [ $EXITCODE6 -eq 1 ]; then
            echo -e "\nFailed to resume job. Help needed...\n" >>$LOG
            malert "I added the seeds but failed to resume job. That means it didn't start. Help needed... Check /data/heritrix/FAWP/heritrix-fawp.log and /var/spool/mail/heritrix at $HOST."
          else
            echo -e "Job resumed, which means it is starting now." >>$LOG
          fi
        fi
      fi
    fi
  else
      malert "I couldn't make Heritrix work. Help needed... Check /data/heritrix/FAWP/heritrix-fawp.log and /var/spool/mail/heritrix at $HOST."
  fi

#wait for a few minutes, make a check if the job was really created: arc directory exists, craw-log exists, etc
#if they exist, we assume everything is ok, set the variable jobrunning to 1 and the while exits
#if not, we decrement the bigretries and do the cicle again, until the number of retries expires or the crawl is considered as running

  sleep 60

  #arc dir exists and has arc files and crawl.log exists and is not empty
  if  [ -d "$JOBS_PATH$JOB/arcs" ] && [ "$(ls -A $JOBS_PATH$JOB/arcs)" ] && [ -e "$JOBS_PATH$JOB/logs/crawl.log" ] && [ -s "$JOBS_PATH$JOB/logs/crawl.log" ]; then
    #everything is ok, so, exit the while loop   
    echo -e "\nJust checked the files on disk and everything seems to be OK. The Job appears to be running.\n" >>$LOG
    JOBRUNNING=1    
  else 
    #something is not ok; so try again
    echo -e "\nProblem found: Just checked the files on disk and the Job doesn't seem to be running.\n" >>$LOG
    let BIGRETRIES=BIGRETRIES-1
  fi

done


#need a final check here; if the number of retries is zero and the job is not running, then something has to be done manually
#if [ $JOBRUNNING -eq 0 ] && [ $BIGRETRIES -eq 0 ]; then
if [ $JOBRUNNING -eq 0 ]; then
  echo -e "\nCritical problem: could not set the crawl up correctly. Manual intervention is needed. An email was sent to $RECIPIENT\n" >>$LOG
  malert "Critical problem: could not set the crawl up correctly. Manual intervention is needed. Check /data/heritrix/FAWP/heritrix-fawp.log and /var/spool/mail/heritrix at $HOST."
fi
