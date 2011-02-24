var clover = new Object();

// JSON: {classes : [{name, id, sl, el,  methods : [{sl, el}, ...]}, ...]}
clover.pageData = {"classes":[{"el":43,"id":2825,"methods":[{"el":34,"sc":5,"sl":32},{"el":38,"sc":5,"sl":36},{"el":42,"sc":5,"sl":40}],"name":"StartsWith","sl":26}]}

// JSON: {test_ID : {"methods": [ID1, ID2, ID3...], "name" : "testXXX() void"}, ...};
clover.testTargets = {"test_123":{"methods":[{"sl":32},{"sl":36}],"name":"testStartsWith","pass":true,"statements":[{"sl":33},{"sl":37}]},"test_402":{"methods":[{"sl":32},{"sl":40}],"name":"startsWithToString","pass":true,"statements":[{"sl":33},{"sl":41}]},"test_446":{"methods":[{"sl":32},{"sl":36}],"name":"testStartsWith","pass":true,"statements":[{"sl":33},{"sl":37}]},"test_516":{"methods":[{"sl":32},{"sl":40}],"name":"startsWithToString","pass":true,"statements":[{"sl":33},{"sl":41}]}}

// JSON: { lines : [{tests : [testid1, testid2, testid3, ...]}, ...]};
clover.srcFileLines = [[], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [402, 446, 123, 516], [402, 446, 123, 516], [], [], [446, 123], [446, 123], [], [], [402, 516], [402, 516], [], []]
