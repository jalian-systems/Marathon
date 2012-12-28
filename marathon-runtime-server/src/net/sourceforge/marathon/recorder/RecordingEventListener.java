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
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;

import net.sourceforge.marathon.action.ClickAction;
import net.sourceforge.marathon.action.DragAction;
import net.sourceforge.marathon.action.DragAndDropAction;
import net.sourceforge.marathon.action.KeyStrokeAction;
import net.sourceforge.marathon.action.SelectAction;
import net.sourceforge.marathon.action.SelectMenuAction;
import net.sourceforge.marathon.action.UndoOperation;
import net.sourceforge.marathon.action.WindowClosingAction;
import net.sourceforge.marathon.action.WindowState;
import net.sourceforge.marathon.action.WindowStateAction;
import net.sourceforge.marathon.api.IMarathonRuntime;
import net.sourceforge.marathon.api.IRecorder;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.api.RuntimeLogger;
import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.MCellComponent;
import net.sourceforge.marathon.component.MCollectionComponent;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.component.MUnknownComponent;
import net.sourceforge.marathon.component.WindowIdCreator;
import net.sourceforge.marathon.util.ContextMenuTriggers;
import net.sourceforge.marathon.util.ExceptionUtil;
import net.sourceforge.marathon.util.OSUtils;

public class RecordingEventListener implements AWTEventListener {
    private static RecordingEventListener instance;

    private IRecorder recorder;
    private ComponentFinder finder;
    private RecordingEventQueue eventQueue;
    private MComponent focusComponent;
    private String focusComponentText;
    private MComponent mouseComponent;

    private MouseListener mouseListener = new MouseListener() {
        private boolean contextMenuSequence;

        public void mousePressed(MouseEvent e) {
            e = OSUtils.convert(e);
            clearDrag();
            contextMenuSequence = ContextMenuTriggers.isContextMenuSequence(e);
            if (contextMenuSequence)
                return;
            if (!(e.getSource() instanceof Component))
                return;
            Component source = SwingUtilities.getDeepestComponentAt((Component) e.getSource(), e.getPoint().x, e.getPoint().y);
            if (source == null)
                source = (Component) e.getSource();
            MComponent component = finder.getMComponentByComponent(source, e.getPoint());
            storeDragContext(component);
            storeDragStart(component, e);
            if (component != null) {
                RecordingEventListener.this.focusGained(component);
                RecordingEventListener.this.mousePressed(component, e);
            }
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e1) {
            final MouseEvent e = OSUtils.convert(e1);
            if (contextMenuSequence)
                return;
            if (!(e.getSource() instanceof Component))
                return;
            final MComponent component = finder.getMComponentByComponent((Component) e.getSource(), e.getPoint());
            storeDragEnd(component, e);
            clearDrag();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (component != null && component.isMComponentEditable() && component.recordOnMouseRelease()
                            && component.clickNeeded(e) == ClickAction.RECORD_NONE) {
                        /*
                         * e.g. it's a toggle button or slider which would
                         * otherwise only fire on de-focus. fake a de-focus to
                         * fire the state change immediately after a click.
                         */
                        focusLost(null);
                        if (component != null) {
                            RecordingEventListener.this.mouseReleased(component, e);
                        }
                    }
                }
            });
        }
    };
    private KeyListener keyListener = new KeyAdapter() {
        private MenuSelectionManager msm = MenuSelectionManager.defaultManager();

        public void keyPressed(KeyEvent e) {
            if (!isRecordableKeyCode(e))
                return;
            MenuElement[] selectedPath = msm.getSelectedPath();
            if (!isJMenuActive(selectedPath)) {
                if (!(e.getSource() instanceof Component))
                    return;
                MComponent component = finder.getMComponentByComponent((Component) e.getSource());
                if (component == null)
                    return;
                RecordingEventListener.this.focusGained(component);
                RecordingEventListener.this.keyPressed(component, e, false);
                return;
            }
            Component menuComponent = selectedPath[selectedPath.length - 1].getComponent();
            MComponent component = finder.getMComponentByComponent(menuComponent);
            if (component == null)
                return;
            RecordingEventListener.this.focusGained(component);
            if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (component != null && !(menuComponent instanceof JMenu) && !(menuComponent instanceof JPopupMenu)) {
                    recordMenuClicks(component, true);
                    return;
                }
            }
            /*
             * Find the menuitem that might respond to this key click from the
             * last JMenu /JPopup available
             */
            for (int i = selectedPath.length - 1; i >= 0; i--) {
                Component displayedMenuComponent = selectedPath[i].getComponent();
                if (!isMenuContainer(displayedMenuComponent))
                    continue;
                MenuElement[] elem = getSubElements(displayedMenuComponent);
                for (int j = 0; j < elem.length; j++) {
                    if (!(elem[j].getComponent() instanceof JMenuItem) || (elem[j] instanceof JMenu))
                        continue;
                    JMenuItem menuItem = (JMenuItem) elem[j].getComponent();
                    if (keyActivatesItem(e, menuItem)) {
                        MComponent activeComponent = finder.getMComponentByComponent(menuItem);
                        if (activeComponent != null) {
                            recordMenuClicks(activeComponent, true);
                        }
                        return;
                    }
                }
                if (!OSUtils.isJava5OrLater()) {
                    /*
                     * On linux it looks like only the last JMenu displayed is
                     * active . So once we look at the last menu component we go
                     * back . Need to check how this behaves on windows
                     */
                    break;
                }
            }
            RecordingEventListener.this.keyPressed(component, e, true);
        }

        private boolean isRecordableKeyCode(KeyEvent e) {
            return e.getKeyCode() != KeyEvent.VK_ALT && e.getKeyCode() != KeyEvent.VK_CONTROL
                    && e.getKeyCode() != KeyEvent.VK_SHIFT && e.getKeyCode() != KeyEvent.VK_META
                    && !ContextMenuTriggers.isContextMenuKeySequence(e) && !RecordingEventQueue.isContextMenuOn();
        }

        private boolean keyActivatesItem(KeyEvent e, JMenuItem menuItem) {
            return e.getKeyCode() == menuItem.getMnemonic();
        }

        private boolean isMenuContainer(Component displayedMenuComponent) {
            return displayedMenuComponent instanceof JMenu || displayedMenuComponent instanceof JPopupMenu;
        }

        private MenuElement[] getSubElements(Component displayedMenuComponent) {
            MenuElement[] elem;
            if (displayedMenuComponent instanceof JMenu)
                elem = ((JMenu) displayedMenuComponent).getSubElements();
            else
                elem = ((JPopupMenu) displayedMenuComponent).getSubElements();
            return elem;
        }

        public void keyReleased(KeyEvent e) {
            if (!isRecordableKeyCode(e))
                return;
            /*
             * We check for the keyReleased event to record any changed value in
             * the previous focusComponent . (Fix: Mac OSX table cells).
             */
            if (!(e.getSource() instanceof Component))
                return;
            MComponent component = finder.getMComponentByComponent((Component) e.getSource());
            if (component == null)
                return;
            RecordingEventListener.this.focusGained(component);
        }

        private boolean isJMenuActive(MenuElement[] selectedPath) {
            return selectedPath != null
                    && selectedPath.length > 0
                    && ((selectedPath[0].getComponent() instanceof JMenuBar) || (selectedPath[0].getComponent() instanceof JPopupMenu));
        }
    };
    private FocusListener focusListener = new FocusAdapter() {
        public void focusGained(FocusEvent e) {
            MComponent component = finder.getMComponentByComponent((Component) e.getSource());
            if (component != null && !(component instanceof MCollectionComponent) && component.isFocusNeeded()) {
                RecordingEventListener.this.focusGained(component);
            }
        }
    };

    private DragSourceAdapter dragSourceListener = new DragSourceAdapter() {
        public void dragDropEnd(DragSourceDropEvent dsde) {
            if (!dsde.getDropSuccess())
                return;
            Point location = dsde.getLocation();
            Component source = dsde.getDragSourceContext().getComponent();
            MComponent dragSourceComponent = finder.getMComponentByComponent(source);
            Component window = finder.getTopLevelWindow(source);
            SwingUtilities.convertPointFromScreen(location, window);
            Component drop = SwingUtilities.getDeepestComponentAt(window, location.x, location.y);
            if (drop == null)
                return;
            location = dsde.getLocation();
            SwingUtilities.convertPointFromScreen(location, drop);
            MComponent dragTargetComponent = finder.getMComponentByComponent(drop, location);
            RecordingEventListener.this.recordDragAndDrop(dragSourceComponent, dragTargetComponent, dsde.getDropAction());
        }
    };
    private WindowStateAction lastWindowStateAction;

    private final IMarathonRuntime runtime;

    private final IScriptModelServerPart scriptModel;

    private final WindowMonitor windowMonitor;

    public MComponent getFocusComponent() {
        return focusComponent;
    }

    protected void recordWindowState(final Window window) {
        if (!finder.isRawRecording())
            return;
        List<Window> windows = windowMonitor.getAllWindows();
        if (!windows.contains(window))
            return;

        WindowState windowState = new WindowState(window);
        if (windowState.isEmpty())
            return;

        WindowId id = WindowIdCreator.createWindowId(window, windowMonitor);
        WindowStateAction action = new WindowStateAction(id, windowState, scriptModel, windowMonitor);

        if (lastWindowStateAction != null && lastWindowStateAction.equals(action))
            recorder.record(new UndoOperation(lastWindowStateAction, scriptModel, windowMonitor).enscript(id));
        recorder.record(action.enscript(id));
        lastWindowStateAction = action;
    }

    private void recordWindowClosing(Window window) {
        List<Window> windows = windowMonitor.getAllWindows();
        if (!windows.contains(window))
            return;

        WindowClosingAction action = new WindowClosingAction(WindowIdCreator.createWindowId(window, windowMonitor), scriptModel);
        recorder.record(action.enscript());
    }

    public RecordingEventListener(IRecorder recorder, IMarathonRuntime runtime, IScriptModelServerPart scriptModel,
            WindowMonitor windowMonitor) {
        this.runtime = runtime;
        this.scriptModel = scriptModel;
        this.windowMonitor = windowMonitor;
        setInstance(this);
        this.recorder = recorder;
        reset();
    }

    private static void setInstance(RecordingEventListener inst) {
        instance = inst;
    }

    protected void keyPressed(MComponent component, KeyEvent e, boolean isMenuActive) {
        /* First give a chance to the component to eat away keystrokes */
        if (!component.keyNeeded(e))
            return;
        char keyChar = e.getKeyChar();
        KeyStroke ks;
        ks = KeyStroke.getKeyStrokeForEvent(e);
        /* If the keystroke is going to activate a menu item - ignore it */
        /* This will get recorded using select_menu */
        JMenuBar menuBar = getJMenuBar(component);
        if (isMenuBarKeyStroke(ks, menuBar))
            return;
        /* Search for accelerator keys */
        if (menuBar != null) {
            ArrayList<Object> menuList = getMenuForAccelerator(menuBar, ks, null);
            if (menuList != null) {
                /*
                 * Mac accepts accelerator keys while Menu is active while Linux
                 * doesn't. Need to check how Windows behave.
                 */
                if (!isMenuActive || OSUtils.isMac() || OSUtils.isJava5OrLater()) {
                    if (finder != null) {
                        finder.markUsed(component);
                        for (Object object : menuList) {
                            finder.markUsed((MComponent) object);
                        }
                    }
                    recorder.record(new SelectMenuAction(menuList, ks, scriptModel, windowMonitor).enscript(component));
                }
                return;
            }
        }
        /* If the menu is active only accelerator keys have effect */
        if (isMenuActive) {
            return;
        }
        /* Bailout option if we face problems - by default set to false */
        if (component instanceof MUnknownComponent) {
            if (finder != null)
                finder.markUsed(component);
            recorder.record(new KeyStrokeAction(component.getComponentId(), ks, keyChar, scriptModel, windowMonitor)
                    .enscript(component));
            return;
        }
        /*
         * The final check. See whether anyone in the component heirarchy needs
         * the key. We are handling the TabbedPane using change event - so do
         * not record his keys.
         */
        Component keyForComponent = whoNeedsTheKey(ks, component.getComponent());
        if (keyForComponent == null)
            return;
        MComponent c = finder.getMComponentByComponent(keyForComponent);
        if (c == null || c.recordOtherKeys())
            return;
        keyForComponent = checkInputMap(ks, finder.getTopLevelWindow(component.getComponent()));
        if (keyForComponent == null)
            return;
        if (keyForComponent instanceof JLabel && ((JLabel) keyForComponent).getDisplayedMnemonic() == ks.getKeyCode())
            return;
        focusLost(null);
        if (finder != null)
            finder.markUsed(component);
        recorder.record(new KeyStrokeAction(component.getComponentId(), ks, keyChar, scriptModel, windowMonitor)
                .enscript(component));
    }

    private Component checkInputMap(KeyStroke ks, Component component) {
        if (component instanceof JComponent) {
            JComponent jcomponent = (JComponent) component;
            if (jcomponent.getConditionForKeyStroke(ks) == JComponent.WHEN_IN_FOCUSED_WINDOW)
                return jcomponent;
        }
        if (component instanceof Container) {
            Component[] components = ((Container) component).getComponents();
            for (int i = 0; i < components.length; i++) {
                Component c;
                if ((c = checkInputMap(ks, components[i])) != null)
                    return c;
            }
        }
        return null;
    }

    private Component whoNeedsTheKey(KeyStroke ks, Component component) {
        if (!(component instanceof JComponent))
            return null;
        JComponent jcomponent = (JComponent) component;
        if (jcomponent.getActionForKeyStroke(ks) != null)
            return jcomponent;
        return whoNeedsTheKey(ks, jcomponent.getParent());
    }

    private boolean isMenuBarKeyStroke(KeyStroke ks, JMenuBar menuBar) {
        boolean isMenuBarKey = false;
        if (menuBar != null) {
            MenuElement[] subElements = menuBar.getSubElements();
            for (int i = 0; i < subElements.length; i++) {
                Component component2 = subElements[i].getComponent();
                if (component2 instanceof JComponent)
                    isMenuBarKey = isMenuBarKey || ((JComponent) component2).getActionForKeyStroke(ks) != null;
            }
        }
        return isMenuBarKey;
    }

    private JMenuBar getJMenuBar(MComponent component) {
        Component window = finder.getTopLevelWindow(component.getComponent());
        JMenuBar menuBar = null;
        if (window instanceof JFrame)
            menuBar = ((JFrame) window).getJMenuBar();
        else if (window instanceof JDialog)
            menuBar = ((JDialog) window).getJMenuBar();
        return menuBar;
    }

    private ArrayList<Object> getMenuForAccelerator(MenuElement element, KeyStroke ks, ArrayList<Object> menuList) {
        if (menuList == null) {
            menuList = new ArrayList<Object>();
        }
        MenuElement[] subElements = element.getSubElements();
        if (subElements.length == 0) {
            if (element.getComponent() instanceof JMenuItem && !(element.getComponent() instanceof JMenu)) {
                if (ks.equals(((JMenuItem) element.getComponent()).getAccelerator())) {
                    menuList.add(element);
                    return menuList;
                }
            }
            return null;
        }
        for (int i = 0; i < subElements.length; i++) {
            if (getMenuForAccelerator(subElements[i], ks, menuList) != null) {
                if (element.getComponent() instanceof JMenuItem) {
                    menuList.add(0, element);
                }
                return menuList;
            }
        }
        return null;
    }

    private void reset() {
        focusComponent = null;
        focusComponentText = null;
    }

    public void startListening(ComponentFinder finder) {
        this.finder = finder;
        this.recording = true;
        Toolkit.getDefaultToolkit().addAWTEventListener(
                this,
                AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK | AWTEvent.FOCUS_EVENT_MASK | AWTEvent.WINDOW_STATE_EVENT_MASK
                        | AWTEvent.WINDOW_EVENT_MASK | AWTEvent.COMPONENT_EVENT_MASK);
        eventQueue = new RecordingEventQueue(recorder, this.finder, this.runtime, scriptModel, windowMonitor);
        eventQueue.attach();
        DragSource.getDefaultDragSource().addDragSourceListener(dragSourceListener);
    }

    public void stopListening() {
        DragSource.getDefaultDragSource().removeDragSourceListener(dragSourceListener);
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        eventQueue.detach();
        eventQueue = null;
        reset();
        this.recording = false;
    }

    protected void mousePressed(MComponent component, MouseEvent e) {
        recordClick(component, e);
    }

    private long lastClickRecordedTime = 0;
    private ClickAction lastClickRecorded;

    private void recordClick(MComponent component, MouseEvent e) {
        int record_click = 0;
        // Fixed: select_menu recording misses sometimes
        if (component.getComponent() instanceof JMenuItem
                && (!(component.getComponent() instanceof JMenu) || ((JMenu) component.getComponent()).getMenuComponentCount() == 0)) {
            recordMenuClicks(component, true);
            clearDrag(); // prevent accidental drags being recorded on menu
                         // items
            return;
        } else if (component.getComponent() instanceof JMenuItem) {
            /*
             * We handle JMenuItems in recordMenuClicks
             */
            return;
        } else if ((record_click = component.clickNeeded(e)) != ClickAction.RECORD_NONE) {
            /* Record clickable tells us that we do not need regular clicks */
            /* See that the latest value in the component is recorded */
            /** We don't know why this focusLost being called **/
            if (e.isPopupTrigger())
                focusLost(null);
            mouseComponent = component;

            // For non-menu components added to menus, we need to reveal the
            // menu:
            recordMenuClicks(component, false); // reveal the enclosing menu
            if (e.getWhen() - lastClickRecordedTime < 500) {
                // Remove the previous "click" command, as it is replaced with a
                // "doubleclick" command:
                MouseEvent e2 = new MouseEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiersEx(), e.getX(),
                        e.getY(), e.getClickCount() - 1, e.isPopupTrigger(), e.getButton());
                if (finder != null)
                    finder.markUsed(component);
                recorder.record(new UndoOperation(new ClickAction(component.getComponentId(), e2, record_click, scriptModel,
                        windowMonitor), scriptModel, windowMonitor).enscript(component));
            }
            ClickAction click = new ClickAction(component.getComponentId(), e, record_click, scriptModel, windowMonitor);
            if (finder != null)
                finder.markUsed(component);
            recorder.record(click.enscript(component));
            lastClickRecorded = click;
            lastClickRecordedTime = e.getWhen();
        }
    }

    protected void mouseReleased(MComponent component, MouseEvent e) {
        Component at = SwingUtilities.getDeepestComponentAt((Component) e.getSource(), e.getPoint().x, e.getPoint().y);
        if (mouseComponent != null && at == null && mouseComponent.equals(component)) {
            if (finder != null)
                finder.markUsed(component);
            recorder.record(new UndoOperation(new ClickAction(component.getComponentId(), e, ClickAction.RECORD_CLICK, scriptModel,
                    windowMonitor), scriptModel, windowMonitor).enscript(component));
        }
        mouseComponent = null;
        // focusGained(component);
    }

    private void recordMenuClicks(MComponent mComponent, boolean includeComponentItself) {
        ArrayList<Object> menuList = new ArrayList<Object>();

        Component comp = mComponent.getComponent();
        JPopupMenu popup;
        if (comp instanceof JPopupMenu)
            popup = (JPopupMenu) comp;
        else
            popup = (JPopupMenu) SwingUtilities.getAncestorOfClass(JPopupMenu.class, comp);

        if (popup != null) {

            Component c = popup;
            while (true) {
                if (c instanceof JMenu) {
                    MComponent mc = finder.getMComponentByComponent(c);
                    if (mc == null) {
                        /*
                         * This happens when recording clicking on a non-menu
                         * component buried in a submenu, when, while the
                         * submenu remains open, one of the parent menus gets
                         * rebuilt. This leads to a weird state in the menu
                         * system where the submenus and original menu remain
                         * showing, but the link between the menus has been
                         * severed at some point. This is probably a bug in
                         * Omniscope. This results in Marathon recording the
                         * menus up to the disconnect point. We don't need to
                         * change anything in Marathon, because playback works
                         * given the same situation.
                         */
                        // We may need to change this code to break out at this
                        // point, however.
                    } else {
                        menuList.add(0, mc);
                    }
                    c = c.getParent();
                } else if (c instanceof JPopupMenu) {
                    c = ((JPopupMenu) c).getInvoker();
                } else {
                    break; // null, or not a menu
                }
            }
        }

        if (includeComponentItself && popup != comp) {
            menuList.add(mComponent);
        }

        if (menuList.size() > 0) {
            if (finder != null) {
                finder.markUsed(mComponent);
                for (Object object : menuList) {
                    finder.markUsed((MComponent) object);
                }
            }
            recorder.record(new SelectMenuAction(menuList, scriptModel, windowMonitor).enscript(mComponent));
        }
    }

    public void recordSelect(MComponent component) {
        recordSelect(component, component.getText());
    }

    private void recordSelect(MComponent component, String text) {
        // For non-menu components added to menus, we need to reveal the menu:
        recordMenuClicks(component, false); // reveal the enclosing menu
        if (text != null) {
            if (finder != null)
                finder.markUsed(component);
            recorder.record(new SelectAction(component.getComponentId(), text, scriptModel, windowMonitor).enscript(component));
        }
    }

    protected void recordDragAndDrop(MComponent source, MComponent target, int action) {
        recordDragContext();
        if (finder != null) {
            finder.markUsed(source);
            finder.markUsed(target);
        }
        recorder.record(new DragAndDropAction(source.getComponentId(), target.getComponentId(), action, scriptModel, windowMonitor)
                .enscript(source));
    }

    public void focusGained(MComponent component) {
        if (!component.equals(focusComponent)) {
            focusLost(component);
            if (component.isMComponentEditable() && !component.recordAlways())
                focusComponentText = getText(component);
            else {
                focusComponentText = null;
            }
            focusComponent = component;
        }
    }

    public void focusLost(MComponent currentComponent) {
        MComponent focusComponentContainer = null;
        if (focusComponent instanceof MCellComponent)
            focusComponentContainer = ((MCellComponent) focusComponent).getCollectionComponentWithWindowID();
        if (focusComponent != null && focusComponent.isMComponentEditable() && focusComponent.getText() != null
                && !focusComponent.getText().equals(focusComponentText) && focusComponent.isFocusNeeded()) {
            if (focusComponent.recordAlways() && RecordingEventQueue.isContextMenuOn())
                ;
            else
                recordSelect(focusComponent);
        }
        focusComponent = null;
        if (currentComponent != null && currentComponent instanceof MCellComponent) {
            MComponent container = ((MCellComponent) currentComponent).getCollectionComponent();
            if (container.equals(focusComponentContainer)) {
                return;
            }
        }
        if (focusComponentContainer == null)
            return;
        if (focusComponentContainer.getText() != null) {
            recordSelect(focusComponentContainer);
        }
    }

    private String getText(MComponent component) {
        return component != null ? component.getText() : null;
    }

    private MComponent dragComponent = null;
    private MComponent dragComponentContainer = null;
    private String dragComponentText = null;
    private String dragComponentContainerText = null;
    private boolean recording;

    private void storeDragContext(MComponent component) {
        if (component == null) {
            dragComponent = null;
            dragComponentContainer = null;
            return;
        }
        if (component.isMComponentEditable())
            dragComponentText = getText(component);
        else
            dragComponentText = null;
        dragComponent = component;
        MComponent container = null;
        if (dragComponent instanceof MCellComponent)
            container = ((MCellComponent) dragComponent).getCollectionComponentWithWindowID();
        if (container != null && container.isMComponentEditable()) {
            dragComponentContainer = container;
            dragComponentContainerText = container.getText();
        } else {
            dragComponentContainer = null;
            dragComponentContainerText = null;
        }
    }

    private void recordDragContext() {
        if (dragComponent != null && dragComponentText != null)
            recordSelect(dragComponent, dragComponentText);
        if (dragComponentContainer != null && dragComponentContainerText != null)
            recordSelect(dragComponentContainer, dragComponentContainerText);
    }

    /*
     * Unlike the above methods, which are to support drag and drop, this
     * section deals with dragging within a custom component.
     */
    private MComponent dragStartComponent = null;
    private Point dragStartPoint;
    private long dragStartTime;

    private void storeDragStart(MComponent component, MouseEvent e) {
        if (component == null || component.isMComponentEditable())
            return; // ignore drags on sliders and text boxes - this is done by
                    // value as an editable field on de-focus
        dragStartComponent = component;
        dragStartPoint = e.getPoint();
        dragStartTime = e.getWhen();
    }

    private void storeDragEnd(MComponent component, MouseEvent e) {
        if (dragStartComponent == null || dragStartPoint == null || component == null)
            return;
        if (component.getComponent() instanceof JScrollBar) {
            // Ignore drags on scrollbars - the scrollbar might not be present
            // at a different
            // screen size, and scrolling to components works automatically.
            // Note that once Marathon ignored everything on scrollbars but we
            // brought it back -
            // can't recall why. Clicks still take effect.
        } else if (dragStartPoint.distance(e.getPoint()) < 5) {
            // Less than 5 px is not considered a drag - this is how we ignore
            // clicks
        } else if (dragStartComponent.getComponent() != component.getComponent()) {
            // Event is on a different component - for a drag to be really a
            // drag,
            // the press and release must be on the same one, even if dragged
            // outside its bounds.
        } else {
            if (!finder.isRawRecording())
                return;
            if (lastClickRecorded != null && lastClickRecordedTime >= dragStartTime) {
                // undo the click:
                recorder.record(new UndoOperation(lastClickRecorded, scriptModel, windowMonitor).enscript(component));
            }
            if (finder != null)
                finder.markUsed(component);
            recorder.record(new DragAction(component.getComponentId(), e, dragStartPoint, e.getPoint(), scriptModel, windowMonitor)
                    .enscript(component));
        }
        // reset
        clearDrag();
    }

    private void clearDrag() {
        dragStartComponent = null;
        dragStartPoint = null;
    }

    public void eventDispatched(AWTEvent event) {
        try {
            eventDispatchedX(event);
        } catch(Throwable t) {
            RuntimeLogger.getRuntimeLogger().error("Recorder", t.getMessage(), ExceptionUtil.getTrace(t));
        }
    }
    
    public void eventDispatchedX(AWTEvent event) {
        if (RecordingEventQueue.isContextMenuOn()) {
            return;
        }
        if (event instanceof MouseEvent) {
            switch (event.getID()) {
            case MouseEvent.MOUSE_PRESSED:
                mouseListener.mousePressed((MouseEvent) event);
                break;
            case MouseEvent.MOUSE_CLICKED:
                mouseListener.mouseClicked((MouseEvent) event);
                break;
            case MouseEvent.MOUSE_ENTERED:
                mouseListener.mouseEntered((MouseEvent) event);
                break;
            case MouseEvent.MOUSE_EXITED:
                mouseListener.mouseExited((MouseEvent) event);
                break;
            case MouseEvent.MOUSE_RELEASED:
                mouseListener.mouseReleased((MouseEvent) event);
                break;
            }
        } else if (event instanceof FocusEvent) {
            switch (event.getID()) {
            case FocusEvent.FOCUS_GAINED:
                focusListener.focusGained((FocusEvent) event);
                break;
            case FocusEvent.FOCUS_LOST:
                focusListener.focusLost((FocusEvent) event);
                break;
            }
        } else if (event instanceof KeyEvent) {
            switch (event.getID()) {
            case KeyEvent.KEY_PRESSED:
                keyListener.keyPressed((KeyEvent) event);
                break;
            case KeyEvent.KEY_RELEASED:
                keyListener.keyReleased((KeyEvent) event);
                break;
            case KeyEvent.KEY_TYPED:
                keyListener.keyTyped((KeyEvent) event);
                break;
            }
        } else if (event instanceof WindowEvent) {
            switch (event.getID()) {
            case WindowEvent.WINDOW_CLOSING:
                recordWindowClosing(((WindowEvent) event).getWindow());
                break;
            case WindowEvent.WINDOW_STATE_CHANGED:
                recordWindowState(((WindowEvent) event).getWindow());
                break;
            }
        } else if (event instanceof ComponentEvent) {
            switch (event.getID()) {
            case ComponentEvent.COMPONENT_MOVED:
            case ComponentEvent.COMPONENT_RESIZED:
                if (((ComponentEvent) event).getComponent() instanceof Window) {
                    recordWindowState((Window) ((ComponentEvent) event).getComponent());
                }
            }
        }
    }

    public static RecordingEventListener getInstance() {
        return instance;
    }

    public IRecorder getRecorder() {
        return recorder;
    }

    public ComponentFinder getFinder() {
        return finder;
    }

    public boolean isRecording() {
        return recording;
    }

}
