#!/usr/bin/env bash

##
## Deploy and run
##

set -e

if [ "$#" -ne 2 ]; then
    echo "usage: $0 <ssh key> <ec2 instance> <profile>"
    echo "    where <profile> is PART1 or PART2"
    echo "    eg. $0 eventProcessing-awskey.pem ec2-34-243-251-193.eu-west-1.compute.amazonaws.com PART1"
    exit 2
fi

KEY=$1
SERVER=$2
COMMAND="java -jar PhotoCompetition-1.0-SNAPSHOT.jar"

echo "Testing to see if already running on the server"
ssh -i ${KEY} ec2-user@${SERVER} "! ps ax | grep \"$COMMAND\" | grep -v grep"

echo "Building package"
mvn clean package

echo "Copy jar to server"
scp -q -i ${KEY} target/PhotoCompetition-1.0-SNAPSHOT.jar prod-config.yml ec2-user@${SERVER}:~

echo "Starting the backend, you can safely disconnect with ^C and it will"
echo " keep running. if you would like to stop the emitter, then ssh to"
echo " the server and run: "
echo " ps ax | grep \"$COMMAND\" | grep -v grep | awk '{print \$1}' | xargs -r kill"

ssh -i ${KEY} ec2-user@${SERVER} <<EOF
    set -e

    # Apply DB Migrations
    ${COMMAND} db migrate prod-config.yml

    # Start server
    ${COMMAND} server prod-config.yml | tee backend.log &

    tail -f backend.log
EOF
