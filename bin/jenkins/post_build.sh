#!/bin/bash

sudo -S mkdir -p /usr/share/tomcat7-ext-apps/reports/$JOB_NAME/$BUILD_NUMBER/ 
sudo -S cp -r target/testng_reporter-reports /usr/share/tomcat7-ext-apps/reports/$JOB_NAME/$BUILD_NUMBER/
sudo -S cp -r target/screenshots /usr/share/tomcat7-ext-apps/reports/$JOB_NAME/$BUILD_NUMBER/
sudo -S cp -r target/html /usr/share/tomcat7-ext-apps/reports/$JOB_NAME/$BUILD_NUMBER/

#html report resources 
sudo -S cp -r target/screenshots $JENKINS_HOME/jobs/$JOB_NAME/builds/$BUILD_NUMBER/htmlreports/
sudo -S cp -r target/html $JENKINS_HOME/jobs/$JOB_NAME/builds/$BUILD_NUMBER/htmlreports/

export DISPLAY=":0.0"
export NO_AT_BRIDGE=1
sudo -S gnome-web-photo -t 0 --delay=10 http://localhost:8080/jenkins/reports/$JOB_NAME/$BUILD_NUMBER/testng_reporter-reports/overview.html /usr/share/tomcat7-ext-apps/reports/$JOB_NAME/$BUILD_NUMBER/testng_reporter-reports/overview.png