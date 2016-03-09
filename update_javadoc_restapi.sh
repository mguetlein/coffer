#!/bin/bash

mvn javadoc:javadoc
rsync -ruv CFPService-api/target/site/apidocs/* CFPService-webapp/src/main/webapp/api/
