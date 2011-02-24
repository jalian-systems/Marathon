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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.action.ClickAction;
import net.sourceforge.marathon.action.SelectAction;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.InterruptionError;
import net.sourceforge.marathon.api.MarathonException;
import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.event.AWTSync;
import net.sourceforge.marathon.event.FireableMouseClickEvent;
import net.sourceforge.marathon.event.FireableMouseDragEvent;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.EventQueueRunner;
import net.sourceforge.marathon.util.PropertyAccessor;

/**
 * Instances of the class <code>MComponent</code> represent AWT components in a
 * running Java application. For extending Marathon you need to subclass the
 * <code>MComponent</code> class and pass the instance to Marathon through a
 * custom <code>ComponentResolver</code>.
 * 
 * A <code>MComponent</code> provides:
 * <ul>
 * <li>A base class for standard and custom components that need to be supported
 * by Marathon.</li>
 * <li>Keystroke and Mouse recording for the components.</li>
 * <li>Provide assertions to Marathon runtime that can be injected into the
 * script at runtime.</li>
 * <li>Utility functions to simulate Mouse clicks in the subclasses</li>
 * </ul>
 * 
 * When Marathon does not find a <code>ComponentResolver</code> that can handle
 * a Component, it wraps such Components with <code>MComponent</code>. By
 * default, all keystrokes and mouse clicks (including the position and
 * modifiers) are recorded in such cases.
 * 
 * <code>MComponent</code> maintains a list of assertable and properties. Even
 * when a subclass provides an assertion, the project configuration should have
 * an entry for that property for Marathon to display it in the assertion list.
 */

public abstract class MComponent extends PropertyAccessor {
    protected Component component;
    private String name;
    private WindowId windowId;
    protected static ArrayList<AssertProperty> propertyList = new ArrayList<AssertProperty>();

    final protected ComponentFinder finder;
    final protected static EventQueueRunner eventQueueRunner = new EventQueueRunner();
    protected final WindowMonitor windowMonitor;

    /**
     * This class represents an assertable property
     */
    public static class AssertProperty {
        public AssertProperty(String property) {
            this(property, null);
        }

        public AssertProperty(String property, Class<?> forClass) {
            this(property, forClass, null);
        }

        public AssertProperty(String property, Class<?> forClass, String displayName) {
            this.property = property;
            this.forClass = forClass;
            this.displayName = displayName == null ? property : displayName;
        }

        public String property;
        public Class<?> forClass;
        public String displayName;

        public String toString() {
            return "[property=" + property + ",forClass=" + (forClass == null ? "*" : forClass.getName()) + ",displayName="
                    + displayName + "]";
        }
    }

    public static class AssertPropertyInstance {
        public final AssertProperty prop;
        public final Object value;

        public AssertPropertyInstance(AssertProperty prop, Object value) {
            this.prop = prop;
            this.value = value;

        }
    }

    static {
        String assertionsProp = System.getProperty(Constants.PROP_RECORDER_ASSERTIONS, "");
        if (assertionsProp.length() == 0) {
            propertyList.add(new AssertProperty("Enabled"));
            propertyList.add(new AssertProperty("Background"));
            propertyList.add(new AssertProperty("Foreground"));
            propertyList.add(new AssertProperty("RowCount", JTable.class));
            propertyList.add(new AssertProperty("ColumnCount", JTable.class));
            propertyList.add(new AssertProperty("ItemCount", JComboBox.class));
            propertyList.add(new AssertProperty("Model.Size", null, "ItemCount"));
            propertyList.add(new AssertProperty("Font"));
            propertyList.add(new AssertProperty("Font.Family", null, "FontFamily"));
            propertyList.add(new AssertProperty("Border"));
            propertyList.add(new AssertProperty("Border.LineColor", null, "BorderLineColor"));
        } else {
            String[] assertions = assertionsProp.split(";");
            for (int i = 0; i < assertions.length; i++) {
                String[] details = assertions[i].split(":");
                String property = details[0];
                Class<?> forClass = null;
                if (details.length > 1) {
                    try {
                        if (details[1].length() == 0)
                            forClass = null;
                        else
                            forClass = Class.forName(details[1]);
                    } catch (ClassNotFoundException e) {
                        continue;
                    }
                }
                String displayName = null;
                if (details.length > 2) {
                    displayName = details[2];
                }
                propertyList.add(new AssertProperty(property, forClass, displayName));
            }
        }
    }

    /**
     * Constructs a new <code>MComponent</code> wrapping the given component and
     * name. The name is derived from the component heirarchy by
     * <code>NamingStrategy</code>.
     * 
     * @param component
     * @param name
     * @param windowMonitor
     */
    public MComponent(Component component, String name, ComponentFinder finder, WindowMonitor windowMonitor) {
        this.component = component;
        this.name = name;
        this.finder = finder;
        this.windowMonitor = windowMonitor;
    }

    /**
     * Return the name of the component.
     * 
     * @return name
     */
    public String getMComponentName() {
        return name;
    }

    /**
     * Return the component wrapped by this <code>MComponent</code>
     * 
     * @return component - a Java AWT Component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Return the state of this component wrapped in a String form. Each
     * <code>MComponent</code> should override this function to return a state,
     * if applicable.
     * 
     * @return state, Component state in String form.
     */
    public String getText() {
        return null;
    }

    /**
     * Set the state of a Component to an earlier recorded state. By default,
     * Marathon simulates Mouse/Keyboardoperations by pushing the appropriate
     * events into the EventQueue.
     * 
     * @param text
     */
    public void setText(String text) {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the state of a Component to an earlier recorded state. By default,
     * Marathon simulates Mouse/Keyboardoperations by pushing the appropriate
     * events into the EventQueue.
     * 
     * This is a special case where in if the component is a cell component, the
     * <code>isCellEditing</code> is set to true. Marathon uses this variant to
     * send a CR while editing table/tree cells.
     * 
     * @param text
     * @param isCellEditing
     */
    public void setText(String text, boolean isCellEditing) {
        setText(text);
    }

    /**
     * Get the object that is being wrapped in this MComponent for assert
     * comparison Default implementation is return of normalized text, that
     * converts all line endings into a standard form.
     * 
     * Override this if you want to assert in another context. Examples include
     * HTML/XML documents, date controls etc.
     * 
     * You need to override <code>getComparableObject(String text)</code> also.
     * 
     * @return Comparable Object (normalized text as default)
     */
    public Object getComparableObject() {
        return normalize(getText());
    }

    /**
     * Get the object for assert comparison from the text format recorded in the
     * tag Default implementation is return of normalized text
     * 
     * Override this if you want to assert in another context. Examples include
     * HTML/XML documents, date controls etc.
     * 
     * You need to override <code>getComparableObject()</code> also.
     * 
     * @param text
     *            text from tag
     * @return Comparable Object (normalized text as default)
     */
    public Object getComparableObject(String text) {
        return normalize(text);
    }

    /**
     * Returns the parent window of this Component.
     * 
     * @return
     */
    public WindowId getWindowId() {
        return windowId;
    }

    /**
     * Sets the parent window for this Component
     * 
     * @param windowId
     */
    public void setWindowId(WindowId windowId) {
        this.windowId = windowId;
    }

    /**
     * If the <code>MComponent</code> is a <code>MCellComponent</code> returns
     * the identification information required to find it in a
     * <code>MCollectionComponent</code>
     * 
     * @return componentInfo
     */
    public String getComponentInfo() {
        return null;
    }

    /**
     * Return the ComponentId that can identify this component.
     * 
     * @return componentId
     */
    public ComponentId getComponentId() {
        return new ComponentId(name, getComponentInfo());
    }

    /**
     * Simulate a single/double click on the Component
     * 
     * @param numberOfClicks
     */
    public void click(int numberOfClicks) {
        click(numberOfClicks, false);
    }

    /**
     * Simulate a regular and right click on the component.
     * 
     * @param numberOfClicks
     * @param isPopupTrigger
     */
    public void click(int numberOfClicks, boolean isPopupTrigger) {
        click(numberOfClicks, isPopupTrigger ? InputEvent.BUTTON3_DOWN_MASK : InputEvent.BUTTON1_DOWN_MASK, null);
    }

    /**
     * Simulate a mouse click on the Component. This call will generate only a
     * single or a double click. If numberOfClicks > 1 a double click is
     * simulated. The positon is relative to the component.
     * 
     * modifiers are same as those defined in <code>MouseEvent</code>. Use only
     * _DOWN_MASK version of the modifiers.
     * 
     * @param numberOfClicks
     * @param modifiers
     * @param position
     */
    public void click(int numberOfClicks, int modifiers, Point position) {
        swingWait();
        new FireableMouseClickEvent(getComponent(), numberOfClicks, (modifiers & InputEvent.BUTTON3_DOWN_MASK) != 0).fire(position,
                numberOfClicks, modifiers);
        swingWait();
    }

    /**
     * Utility function. Wraps SwingUtilities.invokeAndWait.
     * 
     * @param runnable
     */
    public static void invokeAndWait(Runnable runnable) {
        try {
            SwingUtilities.invokeAndWait(runnable);
        } catch (InterruptedException e) {
            throw new InterruptionError();
        } catch (InvocationTargetException e) {
            Throwable err = e.getTargetException();
            if (err instanceof RuntimeException)
                throw (RuntimeException) err;
            if (err instanceof Error)
                throw (Error) err;
            throw new MarathonException("error during swing invocation", err);
        }
    }

    /**
     * Override <code>clickNeeded</code> to inform Marathon whether a click need
     * to be recorded. The default behavior is to ignore regular clicks if
     * setText() is overridden by the subclass.
     * 
     * @param e
     *            , the mouse event
     * @return one of RECORD_CLICK (records click without position), RECORD_EX
     *         (records click with position) or RECORD_NONE.
     */
    public int clickNeeded(MouseEvent e) {
        if (isMComponentEditable() == false || isPopupTrigger(e))
            return ClickAction.RECORD_CLICK;
        return ClickAction.RECORD_NONE;
    }

    /**
     * Override {@link MComponent#keyNeeded(KeyEvent)} to inform Marathon
     * whether a particle keystroke should be recorded.
     * 
     * The default behavior is to record the keystroke if it is not needed by
     * the component and it is a special key.
     * 
     * @param e
     * @return
     */
    public boolean keyNeeded(KeyEvent e) {
        return keyNeeded(e, isMComponentEditable());
    }

    /**
     * Override {@link MComponent#keyNeeded(KeyEvent)} to inform Marathon
     * whether a particle keystroke should be recorded.
     * 
     * The default behavior is to record the keystroke if it is not needed by
     * the component and it is a special key.
     * 
     * @param e
     * @return
     */
    protected boolean keyNeeded(KeyEvent e, boolean editable) {
        if (!editable)
            return true;
        if ((e.getModifiers() & ~InputEvent.SHIFT_MASK) == 0 && e.getKeyChar() != KeyEvent.CHAR_UNDEFINED
                && !Character.isISOControl(e.getKeyChar()))
            return false;
        if (getComponent() instanceof JComponent) {
            return ((JComponent) getComponent()).getActionForKeyStroke(KeyStroke.getKeyStrokeForEvent(e)) == null;
        }
        return true;
    }

    /**
     * Cannot use {@link MouseEvent#isPopupTrigger()} as depending on the
     * platform that might return true on mouse release. We record clicks on the
     * mouse press.
     * 
     * You need to use this function and not {@link MouseEvent#isPopupTrigger()}
     * for checking right mouse click.
     * 
     * @param e
     * @return
     */
    public boolean isPopupTrigger(MouseEvent e) {
        return (e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0;
    }

    /**
     * Utility function that waits till an earlier Event is consumed by the
     * application.
     * 
     */
    public void swingWait() {
        AWTSync.sync();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof MComponent) {
            MComponent other = (MComponent) obj;
            return component.equals(other.component) && equalComponentInfo(other.getComponentInfo());
        } else {
            return false;
        }
    }

    private boolean equalComponentInfo(String otherInfo) {
        String info = getComponentInfo();
        if (info == null && otherInfo == null)
            return true;
        if (info == null || otherInfo == null)
            return false;
        return info.equals(otherInfo);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return component.hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getMComponentName() + " (" + getClass().getName().substring(getClass().getName().lastIndexOf('.') + 1) + ")";
    }

    /**
     * Normalize the text so that comparisons can be done across multiple
     * platforms.
     * 
     * @param text
     * @return normalized text
     */
    private String normalize(String text) {
        if (text == null)
            return null;
        StringBuffer buffer = new StringBuffer();
        StringTokenizer token = new StringTokenizer(text, "\n\r");
        while (token.hasMoreTokens()) {
            if (buffer.length() > 0) {
                buffer.append('\n');
            }
            buffer.append(token.nextToken());
        }
        return buffer.toString().trim();
    }

    /**
     * Is the <code>MComponent</code> editable. Marathon keeps track of changes
     * to the State of editable <code>MComponent</code>s and records the
     * {@link SelectAction} actions.
     * 
     * @return true or false
     */
    public boolean isMComponentEditable() {
        return component.isEnabled() && isSetTextOverridden();
    }

    /**
     * Returns true if {@link MComponent#setText(String)} is overridden.
     * 
     * @return
     */
    private boolean isSetTextOverridden() {
        try {
            Method setTextInMComponent = MComponent.class.getMethod("setText", new Class[] { String.class });
            Method setTextInObject = this.getClass().getMethod("setText", new Class[] { String.class });
            return !setTextInMComponent.equals(setTextInObject);
        } catch (RuntimeException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * By default Marathon records the {@link SelectAction} actions only when
     * the state of a component is changes and a focus event occurs. Override
     * {@link MComponent#recordAlways()} to inform Marathon that a
     * {@link SelectAction} need to be recorded always.
     * 
     * @return true or false
     */
    public boolean recordAlways() {
        return false;
    }

    /**
     * Return the value of a property from either <code>MComponent</code> or the
     * component wrapped by it. The selection criteria is as follows:
     * 
     * MComponent.is&lt;property&gt; MComponent.get&lt;property&gt;
     * Component.is&lt;property&gt; Component.get&lt;property&gt;
     * 
     * This works properly for nested properties like Font.Family also.
     * 
     * @param property
     * @return
     */
    public String getProperty(String property) {
        Object o = getPropertyObject(property);
        if (o == null)
            return null;
        return removeClassName(o);
    }

    public Object getPropertyObject(String property) {
        String generic = null;
        String specific = null;
        for (int i = 0; i < propertyList.size(); i++) {
            AssertProperty e = (AssertProperty) propertyList.get(i);
            if (e.displayName.equals(property)) {
                if (e.forClass == null)
                    generic = e.property;
                else if (e.forClass.isInstance(component))
                    specific = e.property;
            }
        }
        if (specific != null)
            property = specific;
        else if (generic != null)
            property = generic;
        Object o = getPropertyObject(this, property);
        if (o == null)
            return getPropertyObject(component, property);
        return o;
    }

    public boolean isFocusNeeded() {
        return true;
    }

    public boolean recordOtherKeys() {
        return true;
    }

    public boolean effectsWindowName() {
        return false;
    }

    public Dimension getSize() {
        if (component instanceof JComponent)
            return ((JComponent) component).getVisibleRect().getSize();
        return component.getSize();
    }

    public Point getLocation() {
        return new Point(0, 0);
    }

    public boolean ignoreComponent() {
        return false;
    }

    public List<Object> getMethods() {
        ArrayList<Object> l = new ArrayList<Object>();
        if (getText() != null)
            addMethod(l, "getText");
        if (getContent() != null)
            addMethod(l, "getContent");
        for (int i = 0; i < propertyList.size(); i++) {
            AssertProperty prop = (AssertProperty) propertyList.get(i);
            if (prop.forClass == null || prop.forClass.isInstance(getComponent()))
                l.add(new AssertPropertyInstance(prop, getPropertyObject(prop.property)));
        }
        addMethod(l, "getComponent");
        return l;
    }

    /**
     * Overridden by CellComponent to return back content from collection
     * component
     * 
     * @return
     */
    public String[][] getContent() {
        return null;
    }

    protected void addMethod(ArrayList<Object> l, String name) {
        try {
            l.add(this.getClass().getMethod(name, new Class[] {}));
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static Object unboxPremitiveArray(Object r) {
        int length = Array.getLength(r);
        ArrayList<Object> list = new ArrayList<Object>();
        for (int i = 0; i < length; i++) {
            Object e = Array.get(r, i);
            if (e != null && e.getClass().isArray())
                list.add(unboxPremitiveArray(e));
            else
                list.add(e);
        }
        return list;
    }

    public boolean recordOnMouseRelease() {
        return false;
    }

    final public ComponentFinder getFinder() {
        return finder;
    }

    public void drag(int modifiers, Point start, Point end, int subDelayInMS) {
        if (!getComponent().isShowing()) {
            throw new RuntimeException("not showing");
        }
        swingWait();
        new FireableMouseDragEvent(getComponent(), start, end).fire(modifiers, subDelayInMS);
        swingWait();
    }

    public boolean matched(Properties props) {
        Enumeration<Object> keys = props.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String expected = props.getProperty(key);
            String actual = getProperty(key);
            if (actual == null && expected.equals("null")) {
                continue;
            }
            if (actual == null || expected.equals("null"))
                return false;
            if (!actual.equals(expected))
                return false;
        }
        return true;
    }

    public String getType() {
        return component.getClass().getSimpleName();
    }
}
