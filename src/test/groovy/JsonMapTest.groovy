class MapToJsonTests extends GroovyTestCase {
        private final def mapper = new JsonMap()

        void test_convertMapWithListsOfMapsIntoJSON() {
            def map = ["a": "a", "b": [["b": "a"], 'd', [12, 12, "e"], ["r": 12]]]
            def expected = '''{"a":"a", "b":[
\t{"b":"a"}, "d", ["12", "12", "e"],
\t{"r":"12"}]}'''

        def result = mapper.toJSON(map)
        assert expected == result
    }
}
