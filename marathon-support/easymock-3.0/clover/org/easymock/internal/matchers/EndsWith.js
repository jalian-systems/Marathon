var clover = new Object();

// JSON: {classes : [{name, id, sl, el,  methods : [{sl, el}, ...]}, ...]}
clover.pageData = {"classes":[{"el":43,"id":2702,"methods":[{"el":34,"sc":5,"sl":32},{"el":38,"sc":5,"sl":36},{"el":42,"sc":5,"sl":40}],"name":"EndsWith","sl":26}]}

// JSON: {test_ID : {"methods": [ID1, ID2, ID3...], "name" : "testXXX() void"}, ...};
clover.testTargets = {"test_1075":{"methods":[{"sl":32},{"sl":36}],"name":"testEndsWith","pass":true,"statements":[{"sl":33},{"sl":37}]},"test_15":{"methods":[{"sl":32},{"sl":40}],"name":"endsWithToString","pass":true,"statements":[{"sl":33},{"sl":41}]},"test_197":{"methods":[{"sl":32},{"sl":36}],"name":"testEndsWith","pass":true,"statements":[{"sl":33},{"sl":37}]},"test_928":{"methods":[{"sl":32},{"sl":40}],"name":"endsWithToString","pass":true,"statements":[{"sl":33},{"sl":41}]}}

// JSON: { lines : [{tests : [testid1, testid2, testid3, ...]}, ...]};
clover.srcFileLines = [[], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [197, 1075, 15, 928], [197, 1075, 15, 928], [], [], [197, 1075], [197, 1075], [], [], [15, 928], [15, 928], [], []]
