#!/bin/bash
cd $(dirname $0)

#
# This is to create the agent emitter that uses the previous JetFuel.java protobuf generated protocol
#

cp pom.xml pom.xml.bkup
sed -i '/java.version/a \ \ \ \ <fs-utils.version>1.0.5-RELEASE</fs-utils.version>' pom.xml
sed -zi 's/<version>.*/<version>1.1.0-RELEASE<\/version>/2m' pom.xml
mvn clean package
cp pom.xml.bkup pom.xml
