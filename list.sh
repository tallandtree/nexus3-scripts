#!/bin/bash
# A simple example script that lists all scripts in the Nexus Repository Manager

# fail if anything errors
set -e
set -u

while getopts u:p:h: option
do
 case "${option}"
 in
 u) username=${OPTARG};;
 p) password=${OPTARG};;
 h) host=${OPTARG};;
 esac
done

printf "Listing Integration API Scripts\n"

curl -v -u $username:$password "$host/service/siesta/rest/v1/script"
