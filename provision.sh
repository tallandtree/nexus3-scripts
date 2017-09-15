#!/bin/bash

# A simple example script that publishes a number of scripts to the Nexus Repository Manager
# This version does not run the scripts.

# fail if anything errors
set -e

while getopts u:p:h:s: option
do
 case "${option}"
 in
 u) username=${OPTARG};;
 p) password=${OPTARG};;
 h) host=${OPTARG};;
 s) script=${OPTARG};;
 esac
done

function usage {
    echo "Provision groovy script to nexus instance."
    echo
    echo "$0 -u username -p password -h host [-s script]"
    echo "-u username: nexus username with permissions to provision scripts"
    echo "-p password: password of the username"
    echo "-h host: url to nexus host"
    echo "-s script: optional script to publish, if omitted all scripts"
    echo "           in src/main/groovy will be published"
    exit 1
}

# add a script to the repository manager
function addScript {
  name=$1
  file=$2
  # using grape config that points to local Maven repo and Central Repository , default grape config fails on some downloads although artifacts are in Central
  # change the grapeConfig file to point to your repository manager, if you are already running one in your organization
  groovy -Dgroovy.grape.report.downloads=true -Dgrape.config=grapeConfig.xml addUpdateScript.groovy -u "$username" -p "$password" -n "$name" -f "$file" -h "$host"
  printf "\nPublished $file as $name\n\n"
}

# some of the arguments are mandatory
if [[ -z "${host}" || -z "${username}" || -z "${password}" ]]; then
    usage
fi

printf "Provisioning Integration API Scripts Starting \n\n"
printf "Publishing on $host\n"
if [[ -z "${script}" ]]; then
    for file in src/main/groovy/*.groovy; do
        [ -e "${file}" ] || continue
        name=${file##*/}
        base=${name%.groovy}
        addScript ${base} ${file}
    done
else
    [ -e "${script}" ] || usage
    name=${script##*/}
    base=${name%.groovy}
    addScript ${base} ${script}
fi

printf "\nProvisioning Scripts Completed\n\n"
