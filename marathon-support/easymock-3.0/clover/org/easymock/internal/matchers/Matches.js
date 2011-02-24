var clover = new Object();

// JSON: {classes : [{name, id, sl, el,  methods : [{sl, el}, ...]}, ...]}
clover.pageData = {"classes":[{"el":43,"id":2773,"methods":[{"el":34,"sc":5,"sl":32},{"el":38,"sc":5,"sl":36},{"el":42,"sc":5,"sl":40}],"name":"Matches","sl":26}]}

// JSON: {test_ID : {"methods": [ID1, ID2, ID3...], "name" : "testXXX() void"}, ...};
clover.testTargets = {"test_560":{"methods":[{"sl":32},{"sl":36}],"name":"testMatches","pass":true,"statements":[{"sl":33},{"sl":37}]},"test_638":{"methods":[{"sl":32},{"sl":40}],"name":"matchesToString","pass":true,"statements":[{"sl":33},{"sl":41}]},"test_87":{"methods":[{"sl":32},{"sl":40}],"name":"matchesToString","pass":true,"statements":[{"sl":33},{"sl":41}]},"test_914":{"methods":[{"sl":32},{"sl":36}],"name":"testMatches","pass":true,"statements":[{"sl":33},{"sl":37}]}}

// JSON: { lines : [{tests : [testid1, testid2, testid3, ...]}, ...]};
clover.srcFileLines = [[], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [87, 638, 914, 560], [87, 638, 914, 560], [], [], [914, 560], [914, 560], [], [], [87, 638], [87, 638], [], []]
