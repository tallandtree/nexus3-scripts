/*
// This groovy script adds an LDAP role with privileges to Nexus 3
// Note:
// ldapGroup
// nexusRoles
*/
import groovy.json.JsonSlurper
import org.sonatype.nexus.security.user.UserManager
import org.sonatype.nexus.security.role.NoSuchRoleException

def roles = new JsonSlurper().parseText(args)

//
// disable anonymous access
//
security.setAnonymousAccess(false)
log.info('Anonymous access disabled')

authManager = security.getSecuritySystem().getAuthorizationManager(UserManager.DEFAULT_SOURCE)

roles.each {
    def existingRole = null
    try {
        existingRole = authManager.getRole(it.id)
    } catch (NoSuchRoleException ignored) {
        // could not find role
    }
    privileges = (it.privileges == null ? new HashSet() : it.privileges.toSet())
    roles = (it.roles == null ? new HashSet() : it.roles.toSet())

    if (existingRole != null) {
        existingRole.setName(it.name)
        existingRole.setDescription(it.description)
        existingRole.setPrivileges(privileges)
        existingRole.setRoles(roles)
        authManager.updateRole(existingRole)
        log.info('Role {} updated', it.name)
    } else {
        security.addRole(it.id, it.name, it.description, privileges.toList(), roles.toList())
        log.info('Role {} created', it.name)
    }
}

