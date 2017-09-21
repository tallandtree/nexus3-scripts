/*
// This groovy script adds an LDAP role with privileges to Nexus 3
// Note:
// ldapGroup
// nexusRoles
*/
import groovy.json.JsonSlurper

def roles = new JsonSlurper().parseText(args)

//
// disable anonymous access
//
security.setAnonymousAccess(false)
log.info('Anonymous access disabled')

roles.each {
    security.addRole(it.id, it.name, it.description, it.privileges, it.roles)
    log.info('Role {} created', it.name)
}

