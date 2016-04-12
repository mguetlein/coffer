#!/bin/bash

mvn javadoc:javadoc
rsync -ruv coffer-api/target/site/apidocs/* coffer-webapp/src/main/webapp/api/
