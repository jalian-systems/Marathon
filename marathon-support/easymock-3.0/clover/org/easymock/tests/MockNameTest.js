var clover = new Object();

// JSON: {classes : [{name, id, sl, el,  methods : [{sl, el}, ...]}, ...]}
clover.pageData = {"classes":[{"el":36,"id":5531,"methods":[{"el":35,"sc":5,"sl":29}],"name":"MockNameTest","sl":27}]}

// JSON: {test_ID : {"methods": [ID1, ID2, ID3...], "name" : "testXXX() void"}, ...};
clover.testTargets = {"test_356":{"methods":[{"sl":29}],"name":"defaultName","pass":true,"statements":[{"sl":31},{"sl":32},{"sl":33},{"sl":34}]},"test_464":{"methods":[{"sl":29}],"name":"defaultName","pass":true,"statements":[{"sl":31},{"sl":32},{"sl":33},{"sl":34}]}}

// JSON: { lines : [{tests : [testid1, testid2, testid3, ...]}, ...]};
clover.srcFileLines = [[], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [464, 356], [], [464, 356], [464, 356], [464, 356], [464, 356], [], []]
