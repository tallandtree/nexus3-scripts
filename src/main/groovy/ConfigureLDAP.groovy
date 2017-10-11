import org.sonatype.nexus.ldap.persist.LdapConfigurationManager
import org.sonatype.nexus.ldap.persist.entity.LdapConfiguration

import groovy.json.JsonSlurper

def ldap = new JsonSlurper().parseText(args)
log.info("Configuring LDAP Connection.")

def manager = container.lookup(LdapConfigurationManager.class.name)
def ldapConfig = new LdapConfiguration(ldap)
boolean update = false;

// Look for existing config to update
manager.listLdapServerConfigurations().each {
    if (it.name == ldap.name) {
        ldapConfig.id = it.id
        update = true
    }
}

if (update) {
    log.info("LDAP Connection {} updated", ldapConfig.name)
    manager.updateLdapServerConfiguration(ldapConfig)
} else {
    log.info("LDAP Connection {} added", ldapConfig.name)
    manager.addLdapServerConfiguration(ldapConfig)
}

