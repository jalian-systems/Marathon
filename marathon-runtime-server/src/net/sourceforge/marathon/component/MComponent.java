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
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
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

public class MComponent extends PropertyAccessor implements IPropertyAccessor {
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
     * Constructs a new <code>MComponent</code> wrapping the given component and
     * default name.
     * 
     * @param component
     * @param name
     * @param windowMonitor
     */
    public MComponent(Component component, WindowMonitor windowMonitor) {
        this(component, "No Name", null, windowMonitor);
    }

    /**
     * Return the name of the component.
     * 
     * @return name
     */
    public String getMComponentName() {
        return name;
    }

    public void setMComponentName(String name) {
        this.name = name;
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

    public void hover(int hoverDelay) {
        swingWait();
        FireableMouseClickEvent event = new FireableMouseClickEvent(getComponent(), 0, false);
        event.setHoverDelay(hoverDelay);
        event.fire(null, 0, 0);
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
        return getMComponentName() + " (" + getComponent().getClass().getName() + ")";
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
        String name = getComponent().getClass().getName();
        if (name.startsWith("javax.swing")) {
            return name.substring("javax.swing.".length());
        }
        return name;
    }

    public String getName() {
        return getComponent().getName();
    }

    public String getButtonText() {
        if (component instanceof AbstractButton)
            return getCText();
        return null;
    }

    public String getButtonIconFile() {
        if (component instanceof AbstractButton)
            return getIconFile();
        return null;
    }

    public String getCText() {
        Object o = getPropertyObject(getComponent(), "text");
        if (o == null || !(o instanceof String) || o.equals(""))
            return null;
        return (String) o;
    }

    public String getIconFile() {
        Object o = getPropertyObject(getComponent(), "icon");
        if (o == null || !(o instanceof Icon))
            return null;
        Icon icon = (Icon) o;
        if (icon != null && icon instanceof ImageIcon) {
            String description = ((ImageIcon) icon).getDescription();
            if (description != null && description.length() != 0)
                return mapFromImageDescription(description);
        }
        return null;
    }

    public static String mapFromImageDescription(String description) {
        try {
            String name = new URL(description).getPath();
            if (name.lastIndexOf('/') != -1)
                name = name.substring(name.lastIndexOf('/') + 1);
            if (name.lastIndexOf('.') != -1)
                name = name.substring(0, name.lastIndexOf('.'));
            return name;
        } catch (MalformedURLException e) {
            return description;
        }
    }

    public String getLabelText() {
        if (getComponent() instanceof JLabel) {
            String text = ((JLabel) getComponent()).getText();
            if (text != null && !text.equals(""))
                return "lbl:" + stripLastColon(text);
        }
        return null;
    }

    public String getLabeledBy() {
        if (getComponent() instanceof JComponent) {
            try {
                JLabel label = (JLabel) ((JComponent) getComponent()).getClientProperty("labeledBy");
                if (label != null && label.getText() != null && !label.getText().equals("")) {
                    return stripLastColon(label.getText().trim());
                }
            } catch (ClassCastException e) {
            }
        }
        return null;
    }

    private String stripLastColon(String name) {
        if (name.endsWith(":")) {
            name = name.substring(0, name.length() - 1).trim();
        }
        if (name.length() == 0)
            return null;
        return name;
    }

    public String getOMapClassName() {
        if (component instanceof Frame || component instanceof Window || component instanceof Dialog
                || component instanceof JInternalFrame) {
            String className = component.getClass().getName();
            Package pkg = component.getClass().getPackage();
            if (pkg == null)
                return className;
            String pkgName = pkg.getName();
            if (!pkgName.startsWith("javax.swing") && !pkgName.startsWith("java.awt"))
                return className;
            if (className.equals("javax.swing.ColorChooserDialog"))
                return className;
            if (component instanceof JDialog) {
                Component[] components = ((JDialog) component).getContentPane().getComponents();
                if (components.length == 1 && components[0] instanceof JFileChooser)
                    return JFileChooser.class.getName() + "#Dialog";
                if (components.length == 1 && components[0] instanceof JOptionPane)
                    return JOptionPane.class.getName() + "#Dialog_" + ((JOptionPane) components[0]).getMessageType() + "_"
                            + ((JOptionPane) components[0]).getOptionType();
            }
            return null;
        }
        return null;
    }

    public String getOMapClassSimpleName() {
        if (component instanceof Frame || component instanceof Window || component instanceof Dialog
                || component instanceof JInternalFrame) {
            String className = component.getClass().getName();
            String simpleName = component.getClass().getSimpleName();
            Package pkg = component.getClass().getPackage();
            if (pkg == null)
                return simpleName;
            String pkgName = pkg.getName();
            if (!pkgName.startsWith("javax.swing") && !pkgName.startsWith("java.awt"))
                return simpleName;
            if (className.equals("javax.swing.ColorChooserDialog"))
                return simpleName;
            if (component instanceof JDialog) {
                Component[] components = ((JDialog) component).getContentPane().getComponents();
                if (components.length == 1 && components[0] instanceof JFileChooser)
                    return JFileChooser.class.getSimpleName() + "#Dialog";
                if (components.length == 1 && components[0] instanceof JOptionPane)
                    return JOptionPane.class.getSimpleName() + "#Dialog";
            }
            return null;
        }
        return null;
    }

    public int getInternalFrameIndex() {
        if (component instanceof JInternalFrame) {
            JInternalFrame[] frames = ((JInternalFrame) component).getDesktopPane().getAllFrames();
            Arrays.sort(frames, new Comparator<JInternalFrame>() {
                public int compare(JInternalFrame o1, JInternalFrame o2) {
                    return o1.getX() == o2.getX() ? o1.getY() - o2.getY() : o1.getX() - o2.getX();
                }
            });
            for (int i = 0; i < frames.length; i++) {
                if (frames[i] == component) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int getIndexInParent() {
        Container parent = component.getParent();
        Component[] components = parent.getComponents();
        int indexInParent = Arrays.asList(components).indexOf(component);
        if (component instanceof Window) {
            Window owner = ((Window) component).getOwner();
            if (owner != null) {
                indexInParent += Arrays.asList(owner.getOwnedWindows()).indexOf(component);
            }
        }
        return indexInParent;
    }

    public Object getLayoutData() {
        Container parent = component.getParent();
        LayoutManager layout = parent.getLayout();
        if (layout != null) {
            try {
                Method method = layout.getClass().getMethod("getConstraints", Component.class);
                Object constraints = method.invoke(layout, component);
                if (constraints instanceof GridBagConstraints) {
                    Map<String, Object> r = new HashMap<String, Object>();
                    GridBagConstraints gbc = (GridBagConstraints) constraints;
                    r.put("anchor", gbc.anchor);
                    r.put("fill", gbc.fill);
                    r.put("gridheight", gbc.gridheight);
                    r.put("gridwidth", gbc.gridwidth);
                    r.put("gridx", gbc.gridx);
                    r.put("gridy", gbc.gridy);
                    r.put("inserts", gbc.insets);
                    r.put("ipadx", gbc.ipadx);
                    r.put("ipady", gbc.ipady);
                    r.put("weightx", gbc.weightx);
                    r.put("weighty", gbc.weighty);
                    return r;
                }
                return constraints;
            } catch (Exception e) {
            }
        }
        if (layout instanceof GridLayout) {
            int columns = ((GridLayout) layout).getColumns();
            if (columns == 0)
                columns = 1;
            int indexInParent = getIndexInParent();
            return new Point(indexInParent / columns, indexInParent % columns);
        }
        return null;
    }

    public String getPrecedingLabel() {
        Container container = component.getParent();
        if (component == null || container == null)
            return null;
        List<Component> allComponents = findAllComponents();
        // Find labels in the same row (LTR)
        // In the same row: labelx < componentx, labely >= componenty
        Point locComponent = component.getLocationOnScreen();
        List<Component> rowLeft = new ArrayList<Component>();
        for (Component label : allComponents) {
            Point locLabel = label.getLocationOnScreen();
            if (!(label instanceof JPanel) && locLabel.getX() < locComponent.getX() && locLabel.getY() >= locComponent.getY()
                    && locLabel.getY() <= locComponent.getY() + component.getHeight()) {
                rowLeft.add(label);
            }
        }
        Collections.sort(rowLeft, new Comparator<Component>() {
            public int compare(Component o1, Component o2) {
                Point locO1 = o1.getLocationOnScreen();
                Point locO2 = o2.getLocationOnScreen();
                return (int) (locO1.getX() - locO2.getX());
            }
        });
        if (rowLeft.size() > 0 && rowLeft.get(rowLeft.size() - 1) instanceof JLabel) {
            return stripLastColon(((JLabel) rowLeft.get(rowLeft.size() - 1)).getText().trim());
        }
        return null;
    }

    public int getIndexOfType() {
        List<Component> allComponents = findAllComponents();
        int index = 0;
        Class<? extends Component> klass = component.getClass();
        for (Component c : allComponents) {
            if (c == component)
                return index;
            if (c.getClass().equals(klass))
                index++;
        }
        return -1;
    }

    public int getIndexInContainer() {
        List<Component> allComponents = findAllComponents();
        return allComponents.indexOf(component);
    }

    private List<Component> findAllComponents() {
        Component top = getTopWindow(component);
        List<Component> allComponents = new ArrayList<Component>();
        if (top != null)
            fillUp(allComponents, top);
        return allComponents;
    }

    private void fillUp(List<Component> allComponents, Component c) {
        if (!c.isVisible() || !c.isShowing())
            return;
        allComponents.add(c);
        if (c instanceof Container) {
            Component[] components = ((Container) c).getComponents();
            for (Component component : components) {
                fillUp(allComponents, component);
            }
        }
        if (c instanceof Window) {
            Window[] ownedWindows = ((Window) c).getOwnedWindows();
            for (Window window : ownedWindows) {
                fillUp(allComponents, window);
            }
        }
    }

    private Component getTopWindow(Component c) {
        while (c != null) {
            if (c instanceof Window || c instanceof JInternalFrame)
                return c;
            c = c.getParent();
        }
        return null;
    }

    public String getInstanceOf() {
        Class<?> klass = component.getClass();
        while (klass != null && klass.getPackage() != null && !klass.getPackage().getName().startsWith("javax.swing")
                && !klass.getPackage().getName().startsWith("java.awt")) {
            klass = klass.getSuperclass();
        }
        return klass == null ? null : klass.getName();
    }

    public String getFieldName() {
        List<String> fieldNames = getFieldNames();
        if (fieldNames.size() == 0)
            return null;
//        if (fieldNames.size() > 1)
//            logger.warning("For component " + component.getClass().getName() + "(" + name
//                    + "): Found more than one referencing field names: " + fieldNames);
        return fieldNames.get(0);
    }

    public List<String> getFieldNames() {
        List<String> fieldNames = new ArrayList<String>();
        Container container = component.getParent();
        while (container != null) {
            findFields(component, container, fieldNames);
            container = container.getParent();
        }
        return fieldNames;
    }

    private void findFields(Component current, Component container, List<String> fieldNames) {
        Field[] declaredFields = container.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            boolean accessible = field.isAccessible();
            try {
                field.setAccessible(true);
                Object o = field.get(container);
                if (o == current)
                    fieldNames.add(field.getName());
            } catch (Throwable t) {
            } finally {
                field.setAccessible(accessible);
            }
        }
    }

    public MComponent getParentMComponent() {
        if (component.getParent() == null)
            return null;
        return finder.getMComponentByComponent(component.getParent(), "Parent", null);
    }

    public String getAccessibleName() {
        if(component instanceof JTabbedPane)
            return null ;
        return component.getAccessibleContext().getAccessibleName();
    }

    public String getClassName() {
        return component.getClass().getName();
    }

    public boolean getEnabled() {
        return component.isEnabled();
    }

    public Point getPosition() {
        return component.getLocationOnScreen();
    }

    public String getToolTipText() {
        if (component instanceof JComponent)
            return ((JComponent) component).getToolTipText();
        return null;
    }

    public boolean isMatched(String method, String name, String value) {
        if(name.equals("fieldName")) {
            List<String> fieldNames = getFieldNames();
            for (String fieldName : fieldNames) {
                if(match(method, value, fieldName))
                    return true ;
            }
            return false ;
        }
        return match(method, value, getProperty(name));
    }

    private static boolean match(String method, String value, String actual) {
        if (actual == null)
            return false;
        if (method.equals(IPropertyAccessor.METHOD_ENDS_WITH))
            return actual.endsWith(value);
        else if (method.equals(IPropertyAccessor.METHOD_EQUALS))
            return actual.equals(value);
        else if (method.equals(IPropertyAccessor.METHOD_EQUALS_IGNORE_CASE))
            return actual.equalsIgnoreCase(value);
        else if (method.equals(IPropertyAccessor.METHOD_MATCHES))
            return actual.matches(value);
        else if (method.equals(IPropertyAccessor.METHOD_STARTS_WITH))
            return actual.startsWith(value);
        else if (method.equals(IPropertyAccessor.METHOD_CONTAINS))
            return actual.contains(value);
        return false;
    }
    
}
