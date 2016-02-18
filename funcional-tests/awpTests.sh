#!/bin/bash
export DISPLAY=:0
cd /root/pwa-technologies/functional-tests/
plataResult=`/usr/bin/ant test -Dtestcase=pt.fccn.arquivo.tests.AllTests -Dtest.url=http://arquivo.pt  | grep "Failures: 0, Errors: 0"`

if [ -z "$plataResult" ]; then
	echo "TESTS FAIL"
else 
	echo "TESTS OK"
fi

echo "Result: $plataResult"
