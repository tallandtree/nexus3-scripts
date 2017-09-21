# nexus3-scripts
NXRM3 scripts for maintaining nexus 3 repositories. It contains mainly groovy scripts
to list or delete assets based on regular expression which can be scheduled via cron or in
the Nexus Task scheduler (with few modifications).

## Disclaimer
The scripts are tested with nexus 3.5.2, but please test them yourself thoroughly in a test
environment before using them on a production instance.

## Deploy scripts

You can provision all groovy scripts in src/main/groovy or one specified by option -s (full path to script).

```bash
provision.sh -h https://repository.host.com -u admin -p **** [-s script]
```

## Remove scripts

You can remove all groovy scripts from the server in src/main/groovy or one specified by option -n.

```bash
delete.sh -h https://repository.host.com -u admin -p **** [-n nameScript]
```

## Call script

First load the groovy classes required in the scripts. After every restart of Nexus, this needs to
be done before the other scripts are called:

```bash
curl -v -X POST -u admin:****** \
    --header "Content-Type: text/plain" \
    -d "{}" \
    https://repository.host.com/service/siesta/rest/v1/script/ReverseDateTimeComparator/run
curl -v -X POST -u admin:****** \
    --header "Content-Type: text/plain" \
    -d "{}" \
    https://repository.host.com/service/siesta/rest/v1/script/JsonMap/run
```

### List Docker assets

```bash
curl -v -X POST -u admin:****** \
    --header "Content-Type: text/plain" \
    -d "{\"repoName\": \"docker-local\"}" \
    https://repository.host.com/service/siesta/rest/v1/script/ListDockerAssets/run
```

or

```bash
./run.sh -h https://repository.host -u admin -p **** -n ListDockerAssets -d mydocker-repo.json
```

Option -d is a json file that should contain at least the docker repoName. See example [docker-repo.json].

### Delete Docker assets

```bash
curl -v -X POST -u admin:****** \
    --header "Content-Type: text/plain" \
    -d "{\"repoName\": \"test\", \"versionsToKeep\": \"5\", \"dryRun\": \"y\"}" \
    https://repository.host.com/service/siesta/rest/v1/script/DeleteDockerAssets/run
```

or

```bash
./run.sh -h https://repository.host -u admin -p **** -n ListDockerAssets -d mydocker-repo.json
```

Option -d is a json file that should contain at least the docker repoName and number of versionsToKeep.
See example [docker-repo.json].

### Configure LDAP

```bash
curl -v -X POST -u admin:****** \
    --header "Content-Type: text/plain" \
    -d "{\"repoName\": \"test\", \"versionsToKeep\": \"5\", \"dryRun\": \"y\"}" \
    https://repository.host.com/service/siesta/rest/v1/script/ConfigureLDAP/run
```

or

```bash
./run.sh -h https://repository.host -u admin -p **** -n ConfigureLDAP -d ldap.json
```

Option -d is a json file that should contain at least the docker repoName and number of versionsToKeep.
See example [docker-repo.json].

[docker-repo.json]: ./docker-repo.json
[ldap.json]: src/test/integration-test/ldap.json
