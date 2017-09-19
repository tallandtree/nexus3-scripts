class JsonMap {
    def toJSON(elements, depth = 0) {
        def json = ""
        depth.times { json += "\t" }
        json += "{"
        elements.each { key, value ->
            json += "\"$key\":"
            json += jsonValue(value, depth)
            json += ", "
        }

        json = (elements.size() > 0) ? json.substring(0, json.length() - 2) : json
        json += "}"
        json
    }

    private def jsonValue(element, depth) {
        if (element instanceof Map) {
            return "\n" + toJSON(element, depth + 1)
        }
        if (element instanceof List) {
            def list = "["
            element.each { elementFromList ->
                list += jsonValue(elementFromList, depth)
                list += ", "
            }
            list = (element.size() > 0) ? list.substring(0, list.length() - 2) : list
            list += "]"
            return list
        }
        (element instanceof String) ? "\"$element\"": element?.toString()
    }
}
