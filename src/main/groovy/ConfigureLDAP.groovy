import org.sonatype.nexus.ldap.persist.*
import org.sonatype.nexus.ldap.persist.entity.*

import groovy.json.JsonSlurper

def ldap = new JsonSlurper().parseText(args)
log.info("Configuring LDAP Connection.")

def manager = container.lookup(LdapConfigurationManager.class.name)

manager.addLdapServerConfiguration(
  new org.sonatype.nexus.ldap.persist.entity.LdapConfiguration(ldap)
)

