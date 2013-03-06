package net.sourceforge.marathon.util;

import java.util.ArrayList;


public class AssertionLogManager extends Object {
    private static AssertionLogManager instance = new AssertionLogManager();
    public ArrayList<String> types = new ArrayList<String>();
    public ArrayList<String> assertions = new ArrayList<String>();
    public ArrayList<Boolean> passed = new ArrayList<Boolean>();

    private AssertionLogManager() {
    }

    public static AssertionLogManager getInstance() {
        return AssertionLogManager.instance;
    }

    public void addAssertion(String type, String assertion) {
        this.types.add(type);
        this.assertions.add(assertion);

        if (assertions.size() != passed.size()) {
            this.passed.add(true);
        }
    }

    public ArrayList<String> getTypes() {
        return types;
    }

    public ArrayList<String> getAssertions() {
        return assertions;
    }

    public ArrayList<Boolean> getPassed() {
        return passed;
    }

    public void assertionFailed() {
        this.passed.add(false);
    }
}
