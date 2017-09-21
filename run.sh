#!/bin/bash

# A simple example script that runs a script in the Nexus Repository Manager
# Script runs the 'required' groovy script first to load the groovy classes used
# in the various scripts.

# fail if anything errors
set -e

while getopts u:p:h:n:d: option
do
 case "${option}"
 in
 u) username=${OPTARG};;
 p) password=${OPTARG};;
 h) host=${OPTARG};;
 n) name=${OPTARG};;
 d) data=${OPTARG};;
 esac
done

function usage {
    echo "Run groovy script in nexus instance."
    echo
    echo "$0 -u username -p password -h host -n namescript -p parameters"
    echo "-u username: nexus username with permissions to provision scripts"
    echo "-p password: password of the username"
    echo "-h host: url to nexus host"
    echo "-n namescript: name of script to run"
    echo "-d data: data file"
    exit 1
}

# some of the arguments are mandatory
if [[ -z "${host}" || -z "${username}" || -z "${password}" || -z "${name}" ]]; then
    usage
fi

printf "Running Integration API Scripts Starting \n\n"
printf "Executing on $host\n"

function runScript {
    local name=$1
    local data=$2
    echo $name
    echo \"${data}\"
    curl -v -X POST -u $username:$password --header "Content-Type: text/plain" -d @${data} "$host/service/siesta/rest/v1/script/$name/run"
}

runScript JsonMap ""
runScript ReverseDateTimeComparator ""
runScript ${name} "${data}"

printf "\nRunning Script Completed\n\n"
