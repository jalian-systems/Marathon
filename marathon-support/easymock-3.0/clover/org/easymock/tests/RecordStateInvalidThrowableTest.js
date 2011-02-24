var clover = new Object();

// JSON: {classes : [{name, id, sl, el,  methods : [{sl, el}, ...]}, ...]}
clover.pageData = {"classes":[{"el":78,"id":5741,"methods":[{"el":41,"sc":5,"sl":38},{"el":53,"sc":5,"sl":43},{"el":65,"sc":5,"sl":55},{"el":77,"sc":5,"sl":67}],"name":"RecordStateInvalidThrowableTest","sl":30},{"el":36,"id":5741,"methods":[],"name":"RecordStateInvalidThrowableTest.CheckedException","sl":34}]}

// JSON: {test_ID : {"methods": [ID1, ID2, ID3...], "name" : "testXXX() void"}, ...};
clover.testTargets = {"test_1028":{"methods":[{"sl":67}],"name":"throwWrongCheckedException","pass":true,"statements":[{"sl":69},{"sl":70},{"sl":71},{"sl":74}]},"test_1043":{"methods":[{"sl":43}],"name":"throwNull","pass":true,"statements":[{"sl":45},{"sl":46},{"sl":47},{"sl":50}]},"test_35":{"methods":[{"sl":67}],"name":"throwWrongCheckedException","pass":true,"statements":[{"sl":69},{"sl":70},{"sl":71},{"sl":74}]},"test_563":{"methods":[{"sl":55}],"name":"throwCheckedExceptionWhereNoCheckedExceptionIsThrown","pass":true,"statements":[{"sl":57},{"sl":58},{"sl":59},{"sl":62}]},"test_723":{"methods":[{"sl":43}],"name":"throwNull","pass":true,"statements":[{"sl":45},{"sl":46},{"sl":47},{"sl":50}]},"test_90":{"methods":[{"sl":55}],"name":"throwCheckedExceptionWhereNoCheckedExceptionIsThrown","pass":true,"statements":[{"sl":57},{"sl":58},{"sl":59},{"sl":62}]}}

// JSON: { lines : [{tests : [testid1, testid2, testid3, ...]}, ...]};
clover.srcFileLines = [[], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [723, 1043], [], [723, 1043], [723, 1043], [723, 1043], [], [], [723, 1043], [], [], [], [], [90, 563], [], [90, 563], [90, 563], [90, 563], [], [], [90, 563], [], [], [], [], [35, 1028], [], [35, 1028], [35, 1028], [35, 1028], [], [], [35, 1028], [], [], [], []]
