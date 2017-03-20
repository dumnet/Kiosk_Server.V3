#!/bin/bash
JAVA="/usr/bin/java"
APP_DIR="/home/medankota/KioskServer"

cd $APP_DIR
$JAVA -Xms512m -Xmx1024m -jar KioskServer.v3.jar
