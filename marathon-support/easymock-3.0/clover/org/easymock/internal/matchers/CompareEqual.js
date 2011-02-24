var clover = new Object();

// JSON: {classes : [{name, id, sl, el,  methods : [{sl, el}, ...]}, ...]}
clover.pageData = {"classes":[{"el":42,"id":2680,"methods":[{"el":31,"sc":5,"sl":29},{"el":36,"sc":5,"sl":33},{"el":41,"sc":5,"sl":38}],"name":"CompareEqual","sl":25}]}

// JSON: {test_ID : {"methods": [ID1, ID2, ID3...], "name" : "testXXX() void"}, ...};
clover.testTargets = {"test_1096":{"methods":[{"sl":29},{"sl":33},{"sl":38}],"name":"testCompareEqual","pass":true,"statements":[{"sl":30},{"sl":35},{"sl":40}]},"test_540":{"methods":[{"sl":29},{"sl":38}],"name":"cmpTo","pass":true,"statements":[{"sl":30},{"sl":40}]},"test_561":{"methods":[{"sl":29},{"sl":38}],"name":"cmpTo","pass":true,"statements":[{"sl":30},{"sl":40}]},"test_574":{"methods":[{"sl":29},{"sl":38}],"name":"testCompare","pass":true,"statements":[{"sl":30},{"sl":40}]},"test_736":{"methods":[{"sl":29},{"sl":33},{"sl":38}],"name":"testCompareEqual","pass":true,"statements":[{"sl":30},{"sl":35},{"sl":40}]},"test_947":{"methods":[{"sl":29},{"sl":38}],"name":"testCompare","pass":true,"statements":[{"sl":30},{"sl":40}]}}

// JSON: { lines : [{tests : [testid1, testid2, testid3, ...]}, ...]};
clover.srcFileLines = [[], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [561, 540, 947, 1096, 736, 574], [561, 540, 947, 1096, 736, 574], [], [], [1096, 736], [], [1096, 736], [], [], [561, 540, 947, 1096, 736, 574], [], [561, 540, 947, 1096, 736, 574], [], []]
