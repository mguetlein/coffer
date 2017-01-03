#!/bin/bash

SRC=/home/martin/results
DST=/usr/share/tomcat7/results

for s in "coffer" "cdklib"; do
  echo $s
#  rm -rf $DST/$s
  rsync -ruv $SRC/$s $DST
  chown -R tomcat7 $DST/$s
done

