/*
// This groovy script gets a list of docker images and tags from a Nexus docker repository
// Note:
// Nexus docker repository maps to a docker registry
// Nexus docker repository components map to a docker repository or docker image
// Nexus docker repository components versions map to docker image tags
*/
import org.joda.time.DateTime
import org.sonatype.nexus.repository.storage.Component
import org.sonatype.nexus.repository.storage.StorageFacet
import org.sonatype.nexus.repository.storage.StorageTx
import groovy.json.JsonSlurper

class ReverseDateTimeComparator implements Comparator<DateTime> {
    @Override
    int compare(DateTime o1, DateTime o2) {
        return o2.compareTo(o1)
    }
}
def request = new JsonSlurper().parseText(args)
def result = '['
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
    log.info("Found {} artifacts (groupId:artifactId)", artifacts.size())

    Component component
    for (String artifactString : artifacts.keySet()) {
        log.info("Processing artifact {} in repo {}", artifactString, repo.name)
        sortedComponents = artifacts.get(artifactString)
        Iterator componentsIterator = sortedComponents.iterator()
        while (componentsIterator.hasNext()) {
            component = componentsIterator.next().getValue()
            log.info("Component: {}:{}", component.name(), component.version())
        }
    }

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

//return result


