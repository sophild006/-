#!/usr/bin/env bash

key=`grep "def THRIFT_KEY" ../build.gradle | awk -F '"' '{print $2;}'`
iv=`grep "def THRIFT_IV" ../build.gradle | awk -F '"' '{print $2;}'`
echo key is:$key, iv is:$iv

if [ "$key" == "" ]; then
  thrift -r -out ../src/main/java --gen javame ./proto.thrift
else
  thrift -r -out ../src/main/java --gen javame --key $key --iv $iv ./proto.thrift
fi

cd ../src/main/java/com/solid/analytics/model
sys=`uname`
if [ "$sys" == "Darwin" ]; then
  sed -i "" "s/org.apache.thrift/com.solid.analytics.thrift/g" *
else
  sed -i "s/org.apache.thrift/com.solid.analytics.thrift/g" *
fi
cd -
