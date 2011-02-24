var clover = new Object();

// JSON: {classes : [{name, id, sl, el,  methods : [{sl, el}, ...]}, ...]}
clover.pageData = {"classes":[{"el":42,"id":2749,"methods":[{"el":31,"sc":5,"sl":29},{"el":36,"sc":5,"sl":33},{"el":41,"sc":5,"sl":38}],"name":"GreaterThan","sl":25}]}

// JSON: {test_ID : {"methods": [ID1, ID2, ID3...], "name" : "testXXX() void"}, ...};
clover.testTargets = {"test_120":{"methods":[{"sl":29},{"sl":38}],"name":"lessOrEqual","pass":true,"statements":[{"sl":30},{"sl":40}]},"test_388":{"methods":[{"sl":29},{"sl":38}],"name":"greaterThan","pass":true,"statements":[{"sl":30},{"sl":40}]},"test_432":{"methods":[{"sl":29},{"sl":33},{"sl":38}],"name":"testGreateThan","pass":true,"statements":[{"sl":30},{"sl":35},{"sl":40}]},"test_476":{"methods":[{"sl":29},{"sl":38}],"name":"lessOrEqual","pass":true,"statements":[{"sl":30},{"sl":40}]},"test_485":{"methods":[{"sl":29},{"sl":38}],"name":"greaterThan","pass":true,"statements":[{"sl":30},{"sl":40}]},"test_612":{"methods":[{"sl":29},{"sl":38}],"name":"greaterThanOverloaded","pass":true,"statements":[{"sl":30},{"sl":40}]},"test_683":{"methods":[{"sl":29},{"sl":33},{"sl":38}],"name":"testGreateThan","pass":true,"statements":[{"sl":30},{"sl":35},{"sl":40}]},"test_808":{"methods":[{"sl":29},{"sl":38}],"name":"greaterThanOverloaded","pass":true,"statements":[{"sl":30},{"sl":40}]}}

// JSON: { lines : [{tests : [testid1, testid2, testid3, ...]}, ...]};
clover.srcFileLines = [[], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [485, 808, 388, 612, 432, 120, 683, 476], [485, 808, 388, 612, 432, 120, 683, 476], [], [], [432, 683], [], [432, 683], [], [], [485, 808, 388, 612, 432, 120, 683, 476], [], [485, 808, 388, 612, 432, 120, 683, 476], [], []]
