#!/bin/bash

# A simple example script that publishes a number of scripts to the Nexus Repository Manager
# and executes them.

# fail if anything errors
set -e
# fail if a function call is missing an argument

while getopts u:p:h:s: option
do
 case "${option}"
 in
  u) username=${OPTARG};;
  p) password=${OPTARG};;
  h) host=${OPTARG};;
  n) provisionedName=${OPTARG};;
 esac
done

function usage {
    echo "Delete provisioned groovy script from nexus instance."
    echo
    echo "$0 -u username -p password -h host [-s script]"
    echo "-u username: nexus username with permissions to provision scripts"
    echo "-p password: password of the username"
    echo "-h host: url to nexus host"
    echo "-n name: optional provisioned script to remove, if omitted all scripts"
    echo "           in src/main/groovy will be removed from nexus instance"
    exit 1
}
# some of the arguments are mandatory
if [[ -z "${host}" || -z "${username}" || -z "${password}" ]]; then
    usage
fi

# remove a script to the repository manager and run it
function deleteScript {
  name=$1

  curl -v -X DELETE -u $username:$password "$host/service/siesta/rest/v1/script/$name"
  printf "\nDeleted script $name\n\n"
}

printf "Deleting Integration API Scripts Starting \n\n"
printf "Deleting on $host\n"
if [[ -z "${provisionedName}" ]]; then
    for file in src/main/groovy/*.groovy; do
        [ -e "${file}" ] || continue
        name=${file##*/}
        base=${name%.groovy}
        deleteScript ${base}
    done
else
    deleteScript ${provisionedName}
fi

printf "\nDeleting Scripts Completed\n\n"
