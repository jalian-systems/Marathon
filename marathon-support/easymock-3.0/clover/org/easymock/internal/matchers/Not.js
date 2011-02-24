var clover = new Object();

// JSON: {classes : [{name, id, sl, el,  methods : [{sl, el}, ...]}, ...]}
clover.pageData = {"classes":[{"el":45,"id":2779,"methods":[{"el":34,"sc":5,"sl":32},{"el":38,"sc":5,"sl":36},{"el":44,"sc":5,"sl":40}],"name":"Not","sl":26}]}

// JSON: {test_ID : {"methods": [ID1, ID2, ID3...], "name" : "testXXX() void"}, ...};
clover.testTargets = {"test_135":{"methods":[{"sl":32},{"sl":36}],"name":"notOverloaded","pass":true,"statements":[{"sl":33},{"sl":37}]},"test_163":{"methods":[{"sl":32},{"sl":36}],"name":"testNull","pass":true,"statements":[{"sl":33},{"sl":37}]},"test_226":{"methods":[{"sl":32}],"name":"equalsMissing","pass":true,"statements":[{"sl":33}]},"test_368":{"methods":[{"sl":32},{"sl":40}],"name":"notToString","pass":true,"statements":[{"sl":33},{"sl":41},{"sl":42},{"sl":43}]},"test_417":{"methods":[{"sl":32},{"sl":40}],"name":"notToString","pass":true,"statements":[{"sl":33},{"sl":41},{"sl":42},{"sl":43}]},"test_423":{"methods":[{"sl":32},{"sl":36}],"name":"testNotNull","pass":true,"statements":[{"sl":33},{"sl":37}]},"test_437":{"methods":[{"sl":32},{"sl":36}],"name":"notOverloaded","pass":true,"statements":[{"sl":33},{"sl":37}]},"test_595":{"methods":[{"sl":32},{"sl":36}],"name":"testNull","pass":true,"statements":[{"sl":33},{"sl":37}]},"test_751":{"methods":[{"sl":32},{"sl":36}],"name":"testNotNull","pass":true,"statements":[{"sl":33},{"sl":37}]},"test_900":{"methods":[{"sl":32}],"name":"equalsMissing","pass":true,"statements":[{"sl":33}]}}

// JSON: { lines : [{tests : [testid1, testid2, testid3, ...]}, ...]};
clover.srcFileLines = [[], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [368, 900, 595, 417, 135, 437, 423, 751, 163, 226], [368, 900, 595, 417, 135, 437, 423, 751, 163, 226], [], [], [595, 135, 437, 423, 751, 163], [595, 135, 437, 423, 751, 163], [], [], [368, 417], [368, 417], [368, 417], [368, 417], [], []]
