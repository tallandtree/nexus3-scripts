/*
// This groovy script deletes docker images and tags from a Nexus docker repository
// but preserves the latest 'versionsToKeep'
// Use option "imageFilter=filter" to set a filter on docker images
// Use option "delete=y" to delete images, else images are only listed.
// Note:
// Nexus docker repository maps to a docker registry
// Nexus docker repository components map to a docker repository or docker image
// Nexus docker repository components versions map to docker image tags
*/
import org.joda.time.DateTime
import org.sonatype.nexus.repository.storage.Component
import org.sonatype.nexus.repository.storage.Query
import org.sonatype.nexus.repository.storage.StorageFacet
import org.sonatype.nexus.repository.storage.StorageTx
import groovy.json.JsonSlurper

import ReverseDateTimeComparator

def request = new JsonSlurper().parseText(args)
def result = ""
assert request.repoName: 'repoName parameter is required'
assert request.versionsToKeep.isInteger(): 'versionsToKeep parameter is required. Eg. versionsToKeep=10.'
boolean deleteImages = request.delete.toBoolean() ?: false
int versionsToKeep = request.versionsToKeep.toInteger()
def filterImage = request.imageFilter ?: ''

log.info("Gathering Asset list for repository: {} with max versionsToKeep: {}, delete: {}",
        request.repoName, versionsToKeep, deleteImages)

def repo = repository.repositoryManager.get(request.repoName)

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

    Iterable<Component> components = storageTx.findComponents(Query.builder().where('name MATCHES ').param(filterImage).build(), [repo])
    components.each { component ->
        log.debug("Found: {} {}",component.name(),component.version())
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
    log.info("Found {} artifacts (groupId:artifactId)", artifacts.size())

    Component component
    def imagesDeleted = []

    for (String artifactString : artifacts.keySet()) {
        log.info("Processing artifact {} in repo {}", artifactString, repo.name)
        sortedComponents = artifacts.get(artifactString)
        Iterator componentsIteratorReverse = sortedComponents.iterator().reverse()
        int versionsToRemove = sortedComponents.iterator().size() - versionsToKeep
        log.info("Number of components to remove: {}", versionsToRemove)

        def componentMap = [:]
        def versions = [];
        def name = ""

        // Components are sorted by creation date, last created is printed last
        int counter=0
        while (componentsIteratorReverse.hasNext() && counter < versionsToRemove) {
            component = componentsIteratorReverse.next().getValue()
            name = component.name()
            versions.add(component.version())
            if (deleteImages) {
                    log.info("Deleting component: {}:{}:{}", component.group(), component.name(), component.version())
                    storageTx.deleteComponent(component)
            } else {
                    log.info("Dry Run deleting component: {}:{}:{}", component.group(), component.name(), component.version())
            }
            componentMap.put('name',name)
            componentMap.put('versions',versions)
            counter++
        }
        imagesDeleted << componentMap
    }
    def mapper = new JsonMap()
    def deletedImagesMap = ['repositories':imagesDeleted]
    log.info("RepositoryMap: {}", deletedImagesMap)
    result = mapper.toJSON(deletedImagesMap)
    storageTx.commit()
}
catch (Exception e) {
    log.warn("Cleanup failed!!!")
    log.warn("Exception details: {}", e.toString())
    log.warn("Rolling back storage transaction")
    storageTx.rollback()
}
finally {
    storageTx.close()
}

return result
