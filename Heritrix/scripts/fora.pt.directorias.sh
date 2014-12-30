#!/bin/sh

#verifies number of arguments
if [ $# -ne 1 ]; then
echo Usage: $0 SEEDS
exit 127
fi

SEEDS=$1

cat $SEEDS | egrep "^http://.*/[^(\.|/)]+$" | sed 's/$/\//g' > tmp.base.fora

cat $SEEDS | egrep -v "^http://.*/[^(\.|/)]+$"  >> tmp.base.fora

#Para sacar endereços com directorias, fora de .PT: 
cat tmp.base.fora | sed 's/ //g' | egrep -v "^http://[^/]+/$|^http://[^/]+$" | egrep -v "^http://[^/]+\.pt" | egrep "^http://" | egrep "^http://[^/]*[a-z|A-Z]+[^/]*" | egrep -v "^http://[^/]+/[^/]+$" > tmp.fora

#Para sacar endereços só com o domínio com e sem barra final, removendo a barra final à saída, fora de .PT: 
#cat tmp.base.fora | sed 's/ //g' | egrep "^http://[^/]+/$|^http://[^/]+$" | sed 's/\/$//g' | egrep -v "^http://[^/]+\.pt" | egrep "^http://[^/]*[a-z|A-Z]+[^/]*" >> tmp.fora

cat tmp.fora | sort -u | egrep -v "^http://www\.$" 

rm -rf tmp.fora
rm -rf tmp.base.fora

