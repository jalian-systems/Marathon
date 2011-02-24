/*******************************************************************************
 *  
 *  Copyright (C) 2010 Jalian Systems Private Ltd.
 *  Copyright (C) 2010 Contributors to Marathon OSS Project
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 * 
 *  You should have received a copy of the GNU Library General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Project website: http://www.marathontesting.com
 *  Help: Marathon help forum @ http://groups.google.com/group/marathon-testing
 * 
 *******************************************************************************/

package net.sourceforge.marathon.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.junit.Before;
import org.junit.Test;

public class TestMarathonNamingStrategy {
    private static class MyComponent extends JComponent {
        private static final long serialVersionUID = 1L;
        private JTextField fTextField;

        public MyComponent(Container parent, String name) {
            if (parent != null) {
                parent.add(this);
            }
            this.setName(name);
        }

        public MyComponent(Container parent, String name, JTextField f) {
            if (parent != null) {
                parent.add(this);
            }
            this.setName(name);
            fTextField = f;
        }

        public Component getTextFeild() {
            return fTextField;
        }

        @Override public boolean isShowing() {
            return true;
        }
    }

    public static class MyMyComponent extends MyComponent {
        public MyMyComponent(Container parent, String name) {
            super(parent, name);
        }

        public MyMyComponent(Container parent, String name, JTextField f) {
            super(parent, name, f);
        }

        private static final long serialVersionUID = 1L;
    }

    private MarathonNamingStrategy namingStrategy ;
    
    @Before public void setup() {
        namingStrategy = new MarathonNamingStrategy();
    }

    @Test public void testNamesWithProperties() {
        MyComponent parent = new MyComponent(null, "a");
        MyComponent comp1 = new MyComponent(parent, "component");
        namingStrategy.setTopLevelComponent(parent);
        Component component = namingStrategy.getComponent("{name:component, parent.name:a}", ComponentFinder.getRetryCount(), false);
        assertNotNull(component);
        assertEquals(comp1, component);
    }

    @Test public void testSimpleNames() {
        MyComponent a = new MyComponent(null, "a");
        MyComponent b = new MyComponent(a, "b");
        MyComponent c = new MyComponent(b, "c");
        namingStrategy.setTopLevelComponent(a);
        assertEquals("a", namingStrategy.getName(a));
        assertEquals("b", namingStrategy.getName(b));
        assertEquals("c", namingStrategy.getName(c));
    }

    @Test public void testNamesWithParentheses() {
        MyComponent a = new MyComponent(null, "a");
        MyComponent b = new MyComponent(a, "b()");
        MyComponent c = new MyComponent(b, "c#");
        namingStrategy.setTopLevelComponent(a);
        assertEquals("a", namingStrategy.getName(a));
        assertEquals("b#{#}", namingStrategy.getName(b));
        assertEquals("c##", namingStrategy.getName(c));
        assertSame(a, namingStrategy.getComponent("a", ComponentFinder.getRetryCount(), false));
        assertSame(b, namingStrategy.getComponent("b#{#}", ComponentFinder.getRetryCount(), false));
    }

    @Test public void testAddingParentsForUniqueness() {
        MyComponent a = new MyComponent(null, "a");
        MyComponent b = new MyComponent(a, "b");
        MyComponent c = new MyComponent(b, "c");
        MyComponent d = new MyComponent(a, "d");
        MyComponent c2 = new MyComponent(d, "c");
        MyComponent c3 = new MyComponent(d, "c");
        namingStrategy.setTopLevelComponent(a);
        assertEquals("c", namingStrategy.getName(c));
        assertEquals("c1", namingStrategy.getName(c2));
        assertEquals("c2", namingStrategy.getName(c3));
    }

    @Test public void testButtonNameDefaultingToButtonText() {
        MyComponent a = new MyComponent(null, "a");
        JButton button = new JButton("text");
        a.add(button);
        namingStrategy.setTopLevelComponent(a);
        assertEquals("text", namingStrategy.getName(button));
    }

    @Test public void testUnnamedComponent() {
        MyComponent a = new MyComponent(null, "a");
        MyComponent myComponent = new MyComponent(a, null);
        MyComponent myComponent2 = new MyComponent(a, null);
        namingStrategy.setTopLevelComponent(a);
        assertEquals("TestMarathonNamingStrategy$MyComponent", namingStrategy.getName(myComponent));
        assertEquals("TestMarathonNamingStrategy$MyComponent1", namingStrategy.getName(myComponent2));
    }

    @Test public void testRecursive() {
        MyComponent a = new MyComponent(null, "a");
        MyComponent a2 = new MyComponent(a, "a");
        MyComponent a3 = new MyComponent(a2, "a");
        MyComponent a4 = new MyComponent(a2, "a");
        namingStrategy.setTopLevelComponent(a);
        assertEquals("a", namingStrategy.getName(a));
        assertEquals("a1", namingStrategy.getName(a2));
        assertEquals("a2", namingStrategy.getName(a3));
        assertEquals("a3", namingStrategy.getName(a4));
    }

    @Test public void testLabeledByProperty() {
        MyComponent a = new MyComponent(null, "a");
        JLabel label = new JLabel("some text");
        JTextField field = new JTextField();
        a.add(field);
        label.setLabelFor(field);
        assertSame(label, field.getClientProperty("labeledBy"));
        namingStrategy.setTopLevelComponent(a);
        assertEquals("some text", namingStrategy.getName(field));
    }

    @Test public void testHierarchicalComponentView() {
        MyComponent a = new MyComponent(null, "a");
        MyComponent b = new MyComponent(a, "b");
        new MyComponent(b, "c");
        new MyComponent(a, "d");
        namingStrategy.setTopLevelComponent(a);
        namingStrategy.getName(a);
        String expected = "[net.sourceforge.marathon.component.TestMarathonNamingStrategy$MyComponent(a)]\n"
                + "  [net.sourceforge.marathon.component.TestMarathonNamingStrategy$MyComponent(b)]\n"
                + "  [net.sourceforge.marathon.component.TestMarathonNamingStrategy$MyComponent(d)]\n"
                + "    [net.sourceforge.marathon.component.TestMarathonNamingStrategy$MyComponent(c)]\n";

        assertEquals(expected, namingStrategy.getVisibleComponentNames());
    }

    @Test public void testHierarchicalComponentViewWithInvisibleComponents() {
        MyComponent a = new MyComponent(null, "a");
        MyComponent b = new MyComponent(a, "b");
        new MyComponent(b, "c");
        new MyComponent(a, "d");
        MyComponent e = new MyComponent(b, "e");
        e.setVisible(false);
        namingStrategy.setTopLevelComponent(a);
        namingStrategy.getName(a);
        String expected = "[net.sourceforge.marathon.component.TestMarathonNamingStrategy$MyComponent(a)]\n" +
        "  [net.sourceforge.marathon.component.TestMarathonNamingStrategy$MyComponent(b)]\n" +
        "  [net.sourceforge.marathon.component.TestMarathonNamingStrategy$MyComponent(d)]\n" +
        "    [net.sourceforge.marathon.component.TestMarathonNamingStrategy$MyComponent(c)]\n" +
        "    [net.sourceforge.marathon.component.TestMarathonNamingStrategy$MyComponent(e)]\n" + "";
        assertEquals(expected, namingStrategy.getVisibleComponentNames());
        a.setVisible(false);
        namingStrategy.setTopLevelComponent(a);
    }

    @Test public void testFieldName() {
        namingStrategy.setUseFieldNames(true);
        JTextField field = new JTextField();
        MyComponent a = new MyComponent(null, "a", field);
        a.add(field);
        namingStrategy.setTopLevelComponent(a);
        try {
            assertEquals("fTextField", namingStrategy.getName(field));
        } finally {
            namingStrategy.setUseFieldNames(false);
        }
    }

    @Test public void testFieldNameWhenTheContainerIsDerived() {
        namingStrategy.setUseFieldNames(true);
        JTextField field = new JTextField();
        MyMyComponent a = new MyMyComponent(null, "a", field);
        a.add(field);
        namingStrategy.setTopLevelComponent(a);
        try {
            assertEquals("fTextField", namingStrategy.getName(field));
        } finally {
            namingStrategy.setUseFieldNames(false);
        }
    }

    public void xtestTiming() {
        final JDialog dialog = new JDialog((JFrame) null, "TestTiming");
        JDesktopPane pane = new JDesktopPane();
        dialog.setContentPane(pane);
        final Object[] f = { null };
        for (int i = 99; i >= 0; i--) {
            JInternalFrame frame = new JInternalFrame("Internal Frame - " + i, true, true, true, true);
            JButton s = new JButton("button" + i);
            s.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    final long start = System.currentTimeMillis();
                    System.err.println("TestMarathonNamingStrategy.testTiming(): " + start);
                    namingStrategy.setTopLevelComponent(dialog);
                    String name = namingStrategy.getName((Component) e.getSource());
                    System.err.println("TestMarathonNamingStrategy.testTiming(): name = " + name + "::"
                            + (System.currentTimeMillis() - start));
                }
            });
            f[0] = s;
            frame.getContentPane().add(s);
            frame.pack();
            frame.setLocation(i, i);
            frame.setVisible(true);
            pane.add(frame);
        }
        dialog.setSize(500, 500);
        dialog.setModal(true);
        dialog.setVisible(true);
        System.err.println("TestMarathonNamingStrategy.testTiming(): " + ((Component) f[0]).isVisible());
    }
}
