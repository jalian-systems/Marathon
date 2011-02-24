var clover = new Object();

// JSON: {classes : [{name, id, sl, el,  methods : [{sl, el}, ...]}, ...]}
clover.pageData = {"classes":[{"el":44,"id":2737,"methods":[{"el":35,"sc":5,"sl":33},{"el":39,"sc":5,"sl":37},{"el":43,"sc":5,"sl":41}],"name":"Find","sl":27}]}

// JSON: {test_ID : {"methods": [ID1, ID2, ID3...], "name" : "testXXX() void"}, ...};
clover.testTargets = {"test_497":{"methods":[{"sl":33},{"sl":37}],"name":"testFind","pass":true,"statements":[{"sl":34},{"sl":38}]},"test_887":{"methods":[{"sl":33},{"sl":37}],"name":"testFind","pass":true,"statements":[{"sl":34},{"sl":38}]},"test_915":{"methods":[{"sl":33},{"sl":41}],"name":"findToString","pass":true,"statements":[{"sl":34},{"sl":42}]},"test_988":{"methods":[{"sl":33},{"sl":41}],"name":"findToString","pass":true,"statements":[{"sl":34},{"sl":42}]}}

// JSON: { lines : [{tests : [testid1, testid2, testid3, ...]}, ...]};
clover.srcFileLines = [[], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [988, 497, 915, 887], [988, 497, 915, 887], [], [], [497, 887], [497, 887], [], [], [988, 915], [988, 915], [], []]
