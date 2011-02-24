var clover = new Object();

// JSON: {classes : [{name, id, sl, el,  methods : [{sl, el}, ...]}, ...]}
clover.pageData = {"classes":[{"el":60,"id":5988,"methods":[{"el":36,"sc":5,"sl":31},{"el":43,"sc":5,"sl":38},{"el":50,"sc":5,"sl":45},{"el":59,"sc":5,"sl":52}],"name":"ResultTest","sl":29}]}

// JSON: {test_ID : {"methods": [ID1, ID2, ID3...], "name" : "testXXX() void"}, ...};
clover.testTargets = {"test_102":{"methods":[{"sl":31}],"name":"createThrowResultToString","pass":true,"statements":[{"sl":33},{"sl":34},{"sl":35}]},"test_129":{"methods":[{"sl":31}],"name":"createThrowResultToString","pass":true,"statements":[{"sl":33},{"sl":34},{"sl":35}]},"test_327":{"methods":[{"sl":45}],"name":"createDelegateResultToString","pass":true,"statements":[{"sl":47},{"sl":48},{"sl":49}]},"test_376":{"methods":[{"sl":52}],"name":"emptyResults","pass":true,"statements":[{"sl":56},{"sl":57},{"sl":58}]},"test_559":{"methods":[{"sl":45}],"name":"createDelegateResultToString","pass":true,"statements":[{"sl":47},{"sl":48},{"sl":49}]},"test_692":{"methods":[{"sl":38}],"name":"createReturnResultToString","pass":true,"statements":[{"sl":40},{"sl":41},{"sl":42}]},"test_881":{"methods":[{"sl":52}],"name":"emptyResults","pass":true,"statements":[{"sl":56},{"sl":57},{"sl":58}]},"test_912":{"methods":[{"sl":38}],"name":"createReturnResultToString","pass":true,"statements":[{"sl":40},{"sl":41},{"sl":42}]}}

// JSON: { lines : [{tests : [testid1, testid2, testid3, ...]}, ...]};
clover.srcFileLines = [[], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [102, 129], [], [102, 129], [102, 129], [102, 129], [], [], [692, 912], [], [692, 912], [692, 912], [692, 912], [], [], [327, 559], [], [327, 559], [327, 559], [327, 559], [], [], [376, 881], [], [], [], [376, 881], [376, 881], [376, 881], [], []]
