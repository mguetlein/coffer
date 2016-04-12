#!/bin/bash
set -e
for s in "JavaLib" "cdk-lib" "weka-lib" "cfp-miner"; do 
    cd ~/workspace/$s
    mvn clean install
    cd -
done
export MAVEN_OPTS=-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n
mvn tomcat7:run
