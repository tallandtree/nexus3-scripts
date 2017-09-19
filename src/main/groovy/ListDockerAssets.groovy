/*
// This groovy script gets a list of docker images and tags from a Nexus docker repository
// Note:
// Nexus docker repository maps to a docker registry
// Nexus docker repository components map to a docker repository or docker image
// Nexus docker repository components versions map to docker image tags
*/


import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import org.joda.time.DateTime
import org.sonatype.nexus.repository.storage.Component
import org.sonatype.nexus.repository.storage.StorageFacet
import org.sonatype.nexus.repository.storage.StorageTx
import groovy.json.JsonSlurper

import ReverseDateTimeComparator
import JsonMap

def request = new JsonSlurper().parseText(args)
def result = ""
assert request.repoName: 'repoName parameter is required'

def repo = repository.repositoryManager.get(request.repoName)

log.info("Gathering Asset list for repository: {}", request.repoName)

assert repo: "Repository ${request.repoName} does not exist"
assert repo.getFormat().getValue() == 'docker': "Repository ${request.repoName} is not docker"
log.info("Cleaning repository {}, format {}", repo.toString(), repo.getFormat().toString())

StorageTx storageTx = repo.facet(StorageFacet).txSupplier().get()
try {
    storageTx.begin()

    log.info("Collecting components history")
    HashMap<String, SortedMap<DateTime, Component>> artifacts = new HashMap<String, SortedMap<DateTime, Component>>()
    SortedMap<DateTime, Component> sortedComponents
    ReverseDateTimeComparator reverseComparator = new ReverseDateTimeComparator()
    String gaString
    for (Component component : storageTx.browseComponents(storageTx.findBucket(repo))) {
        gaString = sprintf("%s:%s", [component.group(), component.name()])
        if (artifacts.containsKey(gaString)) {
            sortedComponents = artifacts.get(gaString)
            sortedComponents.put(component.lastUpdated(), component)

        } else {// first time
            sortedComponents = new TreeMap<DateTime, Component>(reverseComparator)
            sortedComponents.put(component.lastUpdated(), component)
            artifacts.put(gaString, sortedComponents)
        }
    }

    Component component
    def repositoryList = []

    for (String artifactString : artifacts.keySet()) {
        sortedComponents = artifacts.get(artifactString)
        Iterator componentsIterator = sortedComponents.iterator()
        def componentMap = [:]
        def versions = [];
        def name = ""
        while (componentsIterator.hasNext()) {
            component = componentsIterator.next().getValue()
            name = component.name()
            versions.add(component.version())
        }
        componentMap.put('name',name)
        componentMap.put('versions',versions)
        repositoryList << componentMap
    }
    def mapper = new JsonMap()
    def repositoryMap = ['repositories':repositoryList]
    log.info("RepositoryMap: {}", repositoryMap)
    result = mapper.toJSON(repositoryMap)
    storageTx.commit()

}
catch (Exception e) {
    log.warn("Listing failed!!!")
    log.warn("Exception details: {}", e.toString())
    log.warn("Rolling back storage transaction")
    storageTx.rollback()
}
finally {
    storageTx.close()
}

return result


