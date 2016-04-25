#!/bin/bash

set -e

PROCESS_MARKER=M_NODE_MARKER
SIGAR_NATIVE_PATH=native

echo "Killing existent instances of m-node (if any)..."
if [ "$(uname)" == "Darwin" ]; then
    ps aux | grep ${PROCESS_MARKER} | grep -v grep | awk '{print $2}' | xargs kill
else
    ps aux | grep ${PROCESS_MARKER} | grep -v grep | awk '{print $2}' | xargs -r kill
fi
sleep 5 # just to ensure that port cleared

echo "Unzip..."
unzip m-node.zip

echo "Place config..."
mkdir "config"
mv config.json config

echo "Starting m-node..."
START_PARAMETERS="-Djava.library.path=${SIGAR_NATIVE_PATH} -Dprocess_marker=${PROCESS_MARKER}"
nohup java ${START_PARAMETERS} -cp "config:lib/*" com.github.terma.m.node.Node > m-node.out 2>&1 &

echo "m-node started"