# nexus3-scripts
NXRM3 scripts for maintaining nexus 3 repositories. It contains mainly groovy scripts
to list or delete assets based on regular expression which can be scheduled via cron or in
the Nexus Task scheduler (with few modifications).

## Disclaimer
The scripts are tested with nexus 3.5.2, but please test them yourself thoroughly in a test
environment before using them on a production instance.

## Deploy scripts

```bash
provision.sh -h https://repository.host.com -u admin -p ****
```

## Remove scripts

```bash
delete.sh -h https://repository.host.com -u admin -p ****
```

## Call script

Load depending classes:

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
    https://repository.host.com/service/siesta/rest/v1/script/listDockerAssets/run
```

### Delete Docker assets

```bash
curl -v -X POST -u admin:****** \
    --header "Content-Type: text/plain" \
    -d "{\"repoName\": \"test\", \"versionsToKeep\": \"5\", \"dryRun\": \"y\"}" \
    https://repository.host.com/service/siesta/rest/v1/script/deleteDockerAssets/run
```

