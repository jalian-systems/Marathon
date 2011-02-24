var clover = new Object();

// JSON: {classes : [{name, id, sl, el,  methods : [{sl, el}, ...]}, ...]}
clover.pageData = {"classes":[{"el":42,"id":2792,"methods":[{"el":33,"sc":5,"sl":32},{"el":37,"sc":5,"sl":35},{"el":41,"sc":5,"sl":39}],"name":"Null","sl":26}]}

// JSON: {test_ID : {"methods": [ID1, ID2, ID3...], "name" : "testXXX() void"}, ...};
clover.testTargets = {"test_0":{"methods":[{"sl":39}],"name":"nullToString","pass":true,"statements":[{"sl":40}]},"test_1086":{"methods":[{"sl":32},{"sl":35}],"name":"testCleanupAfterFailureInRecordPhase","pass":true,"statements":[{"sl":36}]},"test_161":{"methods":[{"sl":35}],"name":"differentConstraintsOnSameCall","pass":true,"statements":[{"sl":36}]},"test_163":{"methods":[{"sl":35}],"name":"testNull","pass":true,"statements":[{"sl":36}]},"test_315":{"methods":[{"sl":35}],"name":"differentConstraintsOnSameCall","pass":true,"statements":[{"sl":36}]},"test_332":{"methods":[{"sl":32},{"sl":35}],"name":"testCleanupAfterFailureInRecordPhase","pass":true,"statements":[{"sl":36}]},"test_404":{"methods":[{"sl":35}],"name":"allKinds","pass":true,"statements":[{"sl":36}]},"test_595":{"methods":[{"sl":35}],"name":"testNull","pass":true,"statements":[{"sl":36}]},"test_607":{"methods":[{"sl":35}],"name":"allKinds","pass":true,"statements":[{"sl":36}]},"test_668":{"methods":[{"sl":39}],"name":"nullToString","pass":true,"statements":[{"sl":40}]}}

// JSON: { lines : [{tests : [testid1, testid2, testid3, ...]}, ...]};
clover.srcFileLines = [[], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [1086, 332], [], [], [161, 595, 1086, 404, 607, 315, 163, 332], [161, 595, 1086, 404, 607, 315, 163, 332], [], [], [0, 668], [0, 668], [], []]
