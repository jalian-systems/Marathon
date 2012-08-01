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
package net.sourceforge.marathon.recorder;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.IMarathonRuntime;
import net.sourceforge.marathon.api.IRecorder;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.util.UIUtils;

public class ContextMenuWindow extends JWindow implements IRecordingArtifact, AWTEventListener {

    private static final long serialVersionUID = 1L;
    private TransparentFrame overlayFrame;
    private ComponentFinder finder;
    private final Window parentWindow;
    private Component parentComponent;
    protected int startX;
    protected int startY;
    private JLabel titleLabel;
    private ArrayList<IContextMenu> contextMenus;
    private boolean ignoreMouseEvents;

    public ContextMenuWindow(Window window, IRecorder recorder, ComponentFinder finder, IMarathonRuntime runtime,
            IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        super(window);
        this.parentWindow = window;
        this.finder = finder;
        contextMenus = new ArrayList<IContextMenu>();
        if (recorder.isCreatingObjectMap()) {
            String omapContextMenu = "com.jaliansystems.marathonite.objectmap.ObjectMapContextMenu";
            try {
                Class<?> class1 = Class.forName(omapContextMenu);
                if (AbstractContextMenu.class.isAssignableFrom(class1)) {
                    Constructor<?> constructor;
                    constructor = class1.getConstructor(new Class[] { ContextMenuWindow.class, IRecorder.class,
                            ComponentFinder.class, IScriptModelServerPart.class, WindowMonitor.class });
                    IContextMenu menu = (IContextMenu) constructor.newInstance(new Object[] { this, recorder, finder, scriptModel,
                            windowMonitor });
                    contextMenus.add(menu);
                }
            } catch (Exception e) {
                e.printStackTrace();
                contextMenus.add(new DefaultContextMenu(this, recorder, finder, scriptModel, windowMonitor));
            }
        } else {
            contextMenus.add(new DefaultContextMenu(this, recorder, finder, scriptModel, windowMonitor));
            String extraMenus = System.getProperty(Constants.PROP_CUSTOM_CONTEXT_MENUS);
            if (extraMenus != null) {
                String[] menuClasses = extraMenus.split(";");
                for (int i = 0; i < menuClasses.length; i++) {
                    try {
                        Class<?> class1 = Class.forName(menuClasses[i]);
                        if (AbstractContextMenu.class.isAssignableFrom(class1)) {
                            Constructor<?> constructor = class1.getConstructor(new Class[] { ContextMenuWindow.class,
                                    IRecorder.class, ComponentFinder.class, IScriptModelServerPart.class, WindowMonitor.class });
                            IContextMenu menu = (IContextMenu) constructor.newInstance(new Object[] { this, recorder, finder,
                                    scriptModel, windowMonitor });
                            contextMenus.add(menu);
                        } else if (IContextMenu.class.isAssignableFrom(class1)) {
                            IContextMenu menu = (IContextMenu) class1.newInstance();
                            contextMenus.add(menu);
                        } else {
                            System.err.println(class1.getName() + ": is not a IContextMenu or AbstractContextMenu class");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(menuClasses[i] + ": " + e.getMessage());
                    }
                }
            }
            if (runtime.isCustomAssertionsAvailable()) {
                contextMenus.add(new CustomScriptAssertionsMenu(this, recorder, finder, runtime, scriptModel, windowMonitor));
            }
            contextMenus.add(new ModuleFunctionsMenu(this, recorder, finder, runtime, scriptModel, windowMonitor));
            contextMenus.add(new ChecklistMenu(this, recorder, finder, scriptModel, windowMonitor));
        }
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        JToolBar toolBar = new JToolBar();
        setWindowMove(toolBar);
        Action close = new AbstractAction("Close") {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                ContextMenuWindow.this.setVisible(false);
            }
        };
        JButton closeButton = UIUtils.createActionButton(close);
        closeButton.setText("X");
        toolBar.add(closeButton);
        titleLabel = new JLabel("   Name Of Component");
        setWindowMove(titleLabel);
        toolBar.add(titleLabel);
        toolBar.setFloatable(false);
        Container contentPane = getContentPane();
        contentPane.add(toolBar, BorderLayout.NORTH);
        JTabbedPane tabbedPane = new JTabbedPane();
        Iterator<IContextMenu> iterator = contextMenus.iterator();
        while (iterator.hasNext()) {
            IContextMenu menu = (IContextMenu) iterator.next();
            tabbedPane.addTab(menu.getName(), menu.getContent());
        }
        contentPane.add(tabbedPane, BorderLayout.CENTER);
        KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(escapeStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", close);
        setSize(640, 480);
    }

    private void setWindowMove(Component c) {
        c.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                startX = e.getX();
                startY = e.getY();
            }
        });
        c.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                ContextMenuWindow.this.setLocation(ContextMenuWindow.this.getX() + e.getX() - startX, ContextMenuWindow.this.getY()
                        + e.getY() - startY);
            }

        });
    }

    public void setComponent(Component component, Point point, boolean isTriggered) {
        Iterator<IContextMenu> iterator = contextMenus.iterator();
        while (iterator.hasNext()) {
            IContextMenu menu = (IContextMenu) iterator.next();
            menu.setComponent(component, point, isTriggered);
        }
        MComponent mcomponent = finder.getMComponentByComponent(component, point);
        if (mcomponent == null) {
            return;
        }
        if (isTriggered) {
            overlayFrame = new TransparentFrame(mcomponent);
            overlayFrame.setVisible(true);
        }
        String info = mcomponent.getComponentInfo();
        titleLabel.setText("   " + mcomponent.getMComponentName() + (info == null ? "" : " (" + info + ")"));
        pack();
    }

    public void show(Component parent, int x, int y) {
        if (parentComponent == null)
            parentComponent = parent;
        Point p = new Point(x, y);
        SwingUtilities.convertPointToScreen(p, parent);
        setLocation(p);
        setVisible(true);
    }

    public void setVisible(boolean b) {
        if (b) {
            Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK);
        } else {
            disposeOverlay();
            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
            if (parentWindow != null)
                parentWindow.requestFocus();
            if (parentComponent != null)
                parentComponent.requestFocusInWindow();
        }
        super.setVisible(b);
    }

    private void disposeOverlay() {
        if (overlayFrame != null) {
            overlayFrame.dispose();
            overlayFrame = null;
        }
    }

    public void eventDispatched(AWTEvent event) {
        if (ignoreMouseEvents)
            return;
        Component root = SwingUtilities.getRoot((Component) event.getSource());
        if (root instanceof IRecordingArtifact || root.getName().startsWith("###")) {
            return;
        }
        if (!(event instanceof MouseEvent))
            return;
        MouseEvent mouseEvent = (MouseEvent) event;
        mouseEvent.consume();
        if (event.getID() == MouseEvent.MOUSE_PRESSED) {
            disposeOverlay();
            Component mouseComponent = SwingUtilities.getDeepestComponentAt(mouseEvent.getComponent(), mouseEvent.getX(),
                    mouseEvent.getY());
            if (mouseComponent == null)
                return;
            mouseEvent = SwingUtilities.convertMouseEvent(mouseEvent.getComponent(), mouseEvent, mouseComponent);
            setComponent(mouseComponent, mouseEvent.getPoint(), true);
            return;
        }
    }

    public void setIgnoreMouseEvents(boolean ignoreMouseEvents) {
        this.ignoreMouseEvents = ignoreMouseEvents;
    }
}
