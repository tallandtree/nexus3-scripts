//package ccv.cm.nexus

//import groovy.transform.PackageScope
import org.joda.time.DateTime

class ReverseDateTimeComparator implements Comparator<DateTime> {
    @Override
    int compare(DateTime o1, DateTime o2) {
        return o2.compareTo(o1)
    }
}
