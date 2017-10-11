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

### Delete or list Docker assets

```bash
curl -v -X POST -u admin:****** \
    --header "Content-Type: text/plain" \
    -d "{\"repoName\": \"test\", \"versionsToKeep\": \"5\", \"delete\": \"y\", \"imageFilter\": \"prefix/.*\"}" \
    https://repository.host.com/service/siesta/rest/v1/script/DeleteDockerAssets/run
```
To only list docker assets, set the delete parameter to "n".

or

```bash
./run.sh -h https://repository.host -u admin -p **** -n DeleteDockerAssets -d mydocker-repo.json
```

Option -d is a json file that should contain at least the docker repoName and number of versionsToKeep.
See example [docker-repo.json].

### Configure LDAP

```bash
curl -v -X POST -u admin:****** \
    --header "Content-Type: text/plain" \
    -d @ldap.json https://repository.host.com/service/siesta/rest/v1/script/ConfigureLDAP/run
```

or

```bash
./run.sh -h https://repository.host -u admin -p **** -n ConfigureLDAP -d ldap.json
```

Option -d is a json file containing the ldap configuration. See example [ldap.json].

### Add LDAP Groups with permissions

```bash
curl -v -X POST -u admin:****** \
    --header "Content-Type: text/plain" \
    -d @ldap-roles.json https://repository.host.com/service/siesta/rest/v1/script/AddLDAPRole/run
```

or

```bash
./run.sh -h https://repository.host -u admin -p **** -n AddLDAPRole -d ldap-roles.json
```

Option -d is a json file containing the ldap roles to be created with permissions.
See example [ldap-roles.json].

## Resetting admin account

https://support.sonatype.com/hc/en-us/articles/115002930827-Accessing-the-OrientDB-Console

### Reset admin password

Run this:
```psql
connect plocal:../sonatype-work/nexus3/db/security admin admin

update user SET password="$shiro1$SHA-512$1024$NE+wqQq/TmjZMvfI7ENh/g==$V4yPw8T64UQ6GfJfxYq2hLsVrBY8D1v+bktfOxGdt4b/9BthpWPNUy/CBk6V9iA0nHpzYzJFWO8v/tZFtES8CA==" UPSERT WHERE id="admin"

exit
```
### Reset admin role

Run this:

connect plocal:../sonatype-work/nexus3/db/security admin admin
update user_role_mapping set roles=["nx-admin"] upsert where userId='admin' 
exit

[docker-repo.json]: src/test/integration-test/docker-test.json
[ldap.json]: src/test/integration-test/ldap.json
[ldap-roles.json]: src/test/integration-test/ldap-roles.json
