var clover = new Object();

// JSON: {classes : [{name, id, sl, el,  methods : [{sl, el}, ...]}, ...]}
clover.pageData = {"classes":[{"el":45,"id":5483,"methods":[{"el":43,"sc":5,"sl":37}],"name":"MethodSerializationWrapperTest","sl":29},{"el":35,"id":5483,"methods":[{"el":34,"sc":9,"sl":32}],"name":"MethodSerializationWrapperTest.A","sl":31}]}

// JSON: {test_ID : {"methods": [ID1, ID2, ID3...], "name" : "testXXX() void"}, ...};
clover.testTargets = {"test_413":{"methods":[{"sl":37}],"name":"testGetMethod","pass":true,"statements":[{"sl":39},{"sl":41},{"sl":42}]},"test_737":{"methods":[{"sl":37}],"name":"testGetMethod","pass":true,"statements":[{"sl":39},{"sl":41},{"sl":42}]}}

// JSON: { lines : [{tests : [testid1, testid2, testid3, ...]}, ...]};
clover.srcFileLines = [[], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [413, 737], [], [413, 737], [], [413, 737], [413, 737], [], [], []]
