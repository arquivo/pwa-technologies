#!/bin/sh

#verifies number of arguments
if [ $# -ne 2 ]; then
  echo Usage: $0 LOCAL_URI REMOTE_URI
  exit 127
fi

LOCAL_URI=$1
REMOTE_URI=$2
REMOTE_URI_LOCAL_NAME="index.html"
DATE=`date +%d%m%Y\-%H\-%M`

# Load configs:
# RECIPIENT=something
. /shareT2/scripts/Plone/verificapagina.config

if [ ! -f "index.html" ]; then
  #saca
  wget -q $REMOTE_URI

  #compara
#  RESULTADO=`diff $LOCAL_URI $REMOTE_URI_LOCAL_NAME > /dev/null 2>&1`
  RESULTADO=`diff $LOCAL_URI $REMOTE_URI_LOCAL_NAME`

  #se for diferente manda mail
  #An exit status of 0 means no differences were found, 1 means some differences were found, and 2 means trouble.
case "$?" in
 0)
#    echo "nao sao diferentes"
    #se nao for apaga
    rm index.html
    ;;
 1)
#    echo "sao diferentes"
    mail -s "Pagina inicial do arquivo-web.fccn.pt alterada" $RECIPIENT << FIM
             A pagina inicial do arquivo-web foi alterada.             A pagina inicial do arquivo-web foi alterada.             A pagina inicial do arquivo-web foi 
alterada.             A pagina inicial do arquivo-web foi alterada.             A pagina inicial do arquivo-web foi alterada.             A pagina inicial do arquivo-web 
foi alterada.             A pagina inicial do arquivo-web foi alterada.             A pagina inicial do arquivo-web foi alterada.             A pagina inicial do 
arquivo-web foi alterada.             A pagina inicial do arquivo-web foi alterada.             A pagina inicial do arquivo-web foi alterada.             A pagina inicial 
do arquivo-web foi alterada.             A pagina inicial do arquivo-web foi alterada.             A pagina inicial do arquivo-web foi alterada.             A pagina 
inicial do arquivo-web foi alterada.

diff $LOCAL_URI $REMOTE_URI_LOCAL_NAME:

$RESULTADO
FIM
    mv $LOCAL_URI $LOCAL_URI.$DATE.html
    mv $REMOTE_URI_LOCAL_NAME $LOCAL_URI 
    ;;
 2)
#    echo "trouble: arquivo-web most likely unreachable"
    mail -s "Arquivo.pt inacessivel" $RECIPIENT << FIM
             Nao foi possivel aceder a arquivo-web.fccn.pt.              Nao foi possivel aceder a arquivo-web.fccn.pt.             Nao foi possivel aceder a 
arquivo-web.fccn.pt.             Nao foi possivel aceder a arquivo-web.fccn.pt.             Nao foi possivel aceder a arquivo-web.fccn.pt.             Nao foi possivel 
aceder a arquivo-web.fccn.pt.             Nao foi possivel aceder a arquivo-web.fccn.pt.             Nao foi possivel aceder a arquivo-web.fccn.pt.             Nao foi 
possivel aceder a arquivo-web.fccn.pt.             Nao foi possivel aceder a arquivo-web.fccn.pt.             Nao foi possivel aceder a arquivo-web.fccn.pt.             
Nao foi possivel aceder a arquivo-web.fccn.pt.             Nao foi possivel aceder a arquivo-web.fccn.pt.             Nao foi possivel aceder a arquivo-web.fccn.pt.             
Nao foi possivel aceder a arquivo-web.fccn.pt.
FIM
    ;;
esac


fi

