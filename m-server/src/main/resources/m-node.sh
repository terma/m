#!/bin/bash

set -e

PROCESS_MARKER="M_NODE_$1"
SIGAR_NATIVE_PATH="native"

# ----------------------------------------------------------------------------------------------------------------------
# stop if running

echo "Killing existent instances of m-node (if any)..."
if [ "$(uname)" == "Darwin" ]; then
    ps aux | grep ${PROCESS_MARKER} | grep -v grep | awk '{print $2}' | xargs kill -9
else
    ps aux | grep ${PROCESS_MARKER} | grep -v grep | awk '{print $2}' | xargs -r kill -9
fi
sleep 5 # just to ensure that port cleared
echo "m-node stopped"

# ----------------------------------------------------------------------------------------------------------------------
# clean up

echo "Cleanup..."
rm -rf lib
rm -rf native
rm -rf config
rm -rf *.out

# ----------------------------------------------------------------------------------------------------------------------
# install

echo "Unzip..."
unzip m-node.zip

echo "Place config..."
mkdir config
mv config.json config

# ----------------------------------------------------------------------------------------------------------------------
# start

echo "Starting m-node..."

LOG_FILE=/dev/null
if [ "$2" == "--enableLog" ]; then
    LOG_FILE=m-node.out
fi

# just node no need a lot of mem
JVM_PARAMETERS="-Xmx64m -Xms64m"
START_PARAMETERS="-Djava.library.path=${SIGAR_NATIVE_PATH} -Dprocess_marker=${PROCESS_MARKER}"

nohup java ${JVM_PARAMETERS} ${START_PARAMETERS} -cp "config:lib/*" com.github.terma.m.node.Node > ${LOG_FILE} 2>&1 &
echo "m-node started"