package net.sourceforge.marathon.component;

import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.Test;


public class TestPropertyWrapper {

    @Test(expected = NullPointerException.class) public void properyWrapperGetPropertyWhenComponentIsNull() {
        PropertyWrapper wrapper = new PropertyWrapper(null, WindowMonitor.getInstance());
        wrapper.getProperty("type");
    }
}
