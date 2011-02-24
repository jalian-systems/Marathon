var clover = new Object();

// JSON: {classes : [{name, id, sl, el,  methods : [{sl, el}, ...]}, ...]}
clover.pageData = {"classes":[{"el":43,"id":2787,"methods":[{"el":34,"sc":5,"sl":32},{"el":38,"sc":5,"sl":36},{"el":42,"sc":5,"sl":40}],"name":"NotNull","sl":26}]}

// JSON: {test_ID : {"methods": [ID1, ID2, ID3...], "name" : "testXXX() void"}, ...};
clover.testTargets = {"test_1008":{"methods":[{"sl":40}],"name":"notNullToString","pass":true,"statements":[{"sl":41}]},"test_161":{"methods":[{"sl":36}],"name":"differentConstraintsOnSameCall","pass":true,"statements":[{"sl":37}]},"test_283":{"methods":[{"sl":36}],"name":"callbackGetsArgumentsEvenIfAMockCallsAnother","pass":true,"statements":[{"sl":37}]},"test_309":{"methods":[{"sl":36}],"name":"callbackGetsArgumentsEvenIfAMockCallsAnother","pass":true,"statements":[{"sl":37}]},"test_315":{"methods":[{"sl":36}],"name":"differentConstraintsOnSameCall","pass":true,"statements":[{"sl":37}]},"test_379":{"methods":[{"sl":32},{"sl":36}],"name":"callbackGetsArguments","pass":true,"statements":[{"sl":37}]},"test_423":{"methods":[{"sl":36}],"name":"testNotNull","pass":true,"statements":[{"sl":37}]},"test_558":{"methods":[{"sl":40}],"name":"notNullToString","pass":true,"statements":[{"sl":41}]},"test_716":{"methods":[{"sl":32},{"sl":36}],"name":"callbackGetsArguments","pass":true,"statements":[{"sl":37}]},"test_751":{"methods":[{"sl":36}],"name":"testNotNull","pass":true,"statements":[{"sl":37}]}}

// JSON: { lines : [{tests : [testid1, testid2, testid3, ...]}, ...]};
clover.srcFileLines = [[], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [716, 379], [], [], [], [283, 161, 716, 423, 379, 315, 309, 751], [283, 161, 716, 423, 379, 315, 309, 751], [], [], [1008, 558], [1008, 558], [], []]
