#!/bin/sh

#verifies number of arguments
if [ $# -ne 1 ]; then
echo Usage: $0 SEEDS
exit 127
fi

SEEDS=$1

#Para sacar endereços dos blogs (saca só o domínio):

cat $SEEDS | egrep "(\.blogs\.sapo\.pt)|(\.blogs\.sapo\.ao)|(\.blogs\.sapo\.mz)|(\.blogs\.sapo\.cv)|(\.blogs\.sapo\.tl)" | grep http:// | cut -d '/' -f1,2,3 | sort -u

