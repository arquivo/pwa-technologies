#!/bin/bash
export DISPLAY=:99
cd /root/pwa-technologies/functional-tests

plataResult=`grep "TESTS OK" /root/plata_result`

#verifica se tem um processo de firefox pendurado e termina-o
firefoxPending=`ps -e | grep firefox`
if [ ! -z "$firefoxPending"]; then
	killall firefox
fi

# verifica se o teste de plata falhou
if [ -z "$plataResult" ]; then
	plataResult=`/usr/bin/ant test -Dtestcase=pt.fccn.arquivo.tests.AllTests -Dtest.url=http://arquivo.pt | grep "Failures: 0, Errors: 0"`
	if [ -z "$plataResult" ]; then
		echo "TESTS FAIL"
	else
		echo "TESTS OK"
		echo "Result: $paltaResult"
	fi
fi
