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
package net.sourceforge.marathon.player;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Window;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.JInternalFrame;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.action.AbstractMarathonAction;
import net.sourceforge.marathon.action.AssertAction;
import net.sourceforge.marathon.action.AssertColumnCount;
import net.sourceforge.marathon.action.AssertContent;
import net.sourceforge.marathon.action.AssertPropertyAction;
import net.sourceforge.marathon.action.AssertRowCount;
import net.sourceforge.marathon.action.AssertText;
import net.sourceforge.marathon.action.AssertionFailedError;
import net.sourceforge.marathon.action.ClickAction;
import net.sourceforge.marathon.action.ClickAction.ActionType;
import net.sourceforge.marathon.action.DragAction;
import net.sourceforge.marathon.action.DragAndDropAction;
import net.sourceforge.marathon.action.ImageCompareAction;
import net.sourceforge.marathon.action.KeyStrokeAction;
import net.sourceforge.marathon.action.ScreenCaptureAction;
import net.sourceforge.marathon.action.SelectAction;
import net.sourceforge.marathon.action.SelectMenuAction;
import net.sourceforge.marathon.action.TestException;
import net.sourceforge.marathon.action.WaitPropertyAction;
import net.sourceforge.marathon.action.WindowClosingAction;
import net.sourceforge.marathon.action.WindowState;
import net.sourceforge.marathon.action.WindowStateAction;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.component.INamingStrategy;
import net.sourceforge.marathon.component.MCollectionComponent;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.component.MTable;
import net.sourceforge.marathon.component.WindowIdCreator;
import net.sourceforge.marathon.event.AWTSync;
import net.sourceforge.marathon.providers.ResolversProvider;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.runtime.JavaRuntime;
import net.sourceforge.marathon.util.DataReader;
import net.sourceforge.marathon.util.Snooze;

public class MarathonJava extends Marathon {
    private static String failMessage = null;
    private static int failType;
    private final IScriptModelServerPart scriptModel;
    private final INamingStrategy<Component, Component> namingStrategy;
    private final WindowMonitor windowMonitor;

    private static final Logger logger = Logger.getLogger(MarathonJava.class.getName());
    private static final int FAIL_TEST = 1;
    private static final int ABORT_TEST = 2;

    public MarathonJava() {
        this(JavaRuntime.getInstance().getNamingStrategy(), JavaRuntime.getInstance().getScriptModel(), new ResolversProvider(),
                JavaRuntime.getInstance().getWindowMonitor());
    }

    public MarathonJava(INamingStrategy<Component, Component> namingStrategy, IScriptModelServerPart scriptModel,
            ResolversProvider resolversProvider, WindowMonitor windowMonitor) {
        this.namingStrategy = namingStrategy;
        this.windowMonitor = windowMonitor;
        // this.finder = new ComponentFinder(Boolean.FALSE, namingStrategy,
        // resolversProvider, scriptModel, windowMonitor);
        this.finder = JavaRuntime.getInstance().getFinder();
        this.scriptModel = scriptModel;
    }

    public void play(AbstractMarathonAction action) {
        if(failMessage != null)
            failX(failMessage);
        if (getDelayInMS() != 0) {
            new Snooze(getDelayInMS());
            AWTSync.sync();
        }
        try {
            action.setDelay(getDelayInMS());
            action.playProtected(finder);
        } catch (TestException e) {
            handleFailure(e);
        }
    }

    public void assertEquals(String message, Object expected, Object actual) {
        if(failMessage != null)
            failX(failMessage);
        if (!AbstractMarathonAction.objectEquals(expected, actual)) {
            AssertionFailedError e = new AssertionFailedError(message, expected, actual, scriptModel, windowMonitor);
            e.captureScreen();
            throw e;
        }
    }

    public void assertTrue(String message, boolean actual) {
        if(failMessage != null)
            failX(failMessage);
        try {
            if (!actual) {
                throw new TestException(message, scriptModel, windowMonitor, false);
            }
        } catch (TestException e) {
            handleFailure(e);
        }
    }

    public void handleFailure(TestException e) {
        throw e;
    }

    public void window(String windowTitle, int windowOpenWaitTime) {
        if(failMessage != null)
            failX(failMessage);
        if (windowOpenWaitTime == 0)
            windowOpenWaitTime = Integer.parseInt(System.getProperty(Constants.PROP_WINDOW_TIMEOUT, "60"));
        int timeout = windowOpenWaitTime * 1000;
        Window window = windowMonitor.waitForWindowToOpen(timeout, windowTitle, scriptModel);
        finder.push(window);
    }

    public void frame(String windowTitle, int windowOpenWaitTime) {
        if(failMessage != null)
            failX(failMessage);
        boolean pushed = false;
        if (!finder.hasTopLevelWindow()) {
            pushed = true;
            Window window = WindowMonitor.getTopLevelWindowWithFocus();
            finder.push(window);
        }
        MComponent component = finder.getMContainerById(new ComponentId(windowTitle));
        if (pushed)
            finder.pop();
        finder.push(component.getComponent());
    }

    public void windowClosed(String windowTitle) {
        if(failMessage != null)
            failX(failMessage);
        Window window = windowMonitor.getWindow(windowTitle);

        if (window == null) {
            logger.info("Could not find window in the open window list....");
            return;
        }
        try {
            new WindowClosingAction(window).play(finder);
        } catch (Throwable t) {
            logger.info("Warning: ignoring an error in window closed. The AUT might have been closed programmatically.");
            logger.info("windowClosed: " + t.getMessage());
        }
    }

    public void windowChanged(String state) {
        if(failMessage != null)
            failX(failMessage);
        Window window = getWindowObject();
        new WindowStateAction(WindowIdCreator.createWindowId(window, windowMonitor), new WindowState(state), scriptModel,
                windowMonitor).play(finder);
    }

    public void close() {
        if(failMessage != null)
            failX(failMessage);
        finder.pop();
    }

    public void keystroke(ComponentId id, String keySequence) {
        play(new KeyStrokeAction(id, keySequence, scriptModel, windowMonitor));
    }

    public void click(Object componentName, boolean isPopupTrigger, Object o1, Object o2, Object o3, Object o4, Object o5) {
        ArrayList<Object> params = new ArrayList<Object>();
        if (o1 != null)
            params.add(o1);
        if (o2 != null)
            params.add(o2);
        if (o3 != null)
            params.add(o3);
        if (o4 != null)
            params.add(o4);
        if (o5 != null)
            params.add(o5);
        int clickCount = getClickCount(params);
        Point position = getPosition(params);
        String modifiers = getModifiers(params);
        Object componentInfo = getComponentInfo(params);
        play(new ClickAction(new ComponentId(componentName, componentInfo), position, clickCount, modifiers, ActionType.CLICK,
                isPopupTrigger, scriptModel, windowMonitor));
    }

    public void hover(Object componentName, int delay, Object componentInfo) {
        ClickAction action = new ClickAction(new ComponentId(componentName, componentInfo), null, 0, null, ActionType.HOVER, false,
                scriptModel, windowMonitor);
        action.setHoverDelay(delay);
        play(action);
    }

    public void drag(Object componentName, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6) {
        ArrayList<Object> params = new ArrayList<Object>();
        if (o1 != null)
            params.add(o1);
        if (o2 != null)
            params.add(o2);
        if (o3 != null)
            params.add(o3);
        if (o4 != null)
            params.add(o4);
        if (o5 != null)
            params.add(o5);
        Point start = getPosition(params);
        Point end = getPosition(params);
        // String modifiers = getModifiers(params) ;
        Object componentInfo = getComponentInfo(params);
        // FIXME: modifiers not yet supported
        play(new DragAction(new ComponentId(componentName, componentInfo), /*
                                                                            * modifiers
                                                                            * ,
                                                                            */start, end, scriptModel, windowMonitor));
    }

    private Point getPosition(ArrayList<Object> params) {
        if (params.size() < 2 || !(params.get(0) instanceof Number) || !(params.get(1) instanceof Number))
            return null;
        int x = ((Number) params.remove(0)).intValue();
        int y = ((Number) params.remove(0)).intValue();
        return new Point(x, y);
    }

    private int getClickCount(ArrayList<Object> params) {
        if (params.size() < 1 || !(params.get(0) instanceof Number))
            return 1;
        if (params.size() == 1 || (params.size() > 1 && !(params.get(1) instanceof Number))
                || (params.size() > 2 && (params.get(1) instanceof Number) && (params.get(2) instanceof Number)))
            return ((Number) params.remove(0)).intValue();
        return 1;
    }

    private String getModifiers(ArrayList<Object> params) {
        if (params.size() < 1 || !(params.get(0) instanceof String))
            return null;
        if (params.size() > 1) {
            return (String) params.remove(0);
        }
        try {
            new KeyStrokeAction(((String) params.get(0)) + "+A", scriptModel, windowMonitor);
            return (String) params.remove(0);
        } catch (Exception e) {
            return null;
        }
    }

    private Object getComponentInfo(ArrayList<Object> params) {
        if (params.size() < 1)
            return null;
        return params.remove(0);
    }

    public void select(ComponentId id, String text) {
        play(new SelectAction(id, text, scriptModel, windowMonitor));
    }

    public void select(ComponentId id, List<Map<Object, Object>> v) {
        List<Properties> list = new ArrayList<Properties>();
        for (Map<Object, Object> map : v) {
            Properties p = new Properties();
            for (Entry<Object, Object> entry : map.entrySet()) {
                p.put(entry.getKey().toString(), entry.getValue().toString());
            }
            list.add(p);
        }
        play(new SelectAction(id, list, scriptModel, windowMonitor));
    }

    public void assertText(ComponentId id, String text) {
        play(new AssertText(id, text, scriptModel, windowMonitor));
    }

    public void assertColor(ComponentId id, Color color) {
        play(new AssertAction(id, color, scriptModel, windowMonitor));
    }

    public void assertEnabled(ComponentId id, boolean enabled) {
        play(new AssertAction(id, enabled, scriptModel, windowMonitor));
    }

    public void assertRowCount(ComponentId id, int rowCount) {
        play(new AssertRowCount(id, rowCount, scriptModel, windowMonitor));
    }

    public void assertColumnCount(ComponentId id, int columnCount) {
        play(new AssertColumnCount(id, columnCount, scriptModel, windowMonitor));
    }

    public void assertContent(ComponentId id, String[][] content) {
        play(new AssertContent(id, content, scriptModel, windowMonitor));
    }

    public void sleep(long seconds) {
        new Snooze(seconds * 1000);
        AWTSync.sync();
    }

    public void fail(String message) {
        handleFailure(new TestException(message, scriptModel, windowMonitor, false));
    }

    public String getText(ComponentId id) {
        if(failMessage != null)
            failX(failMessage);
        return finder.getMComponentById(id).getText();
    }

    public Component getComponent(ComponentId id) {
        if(failMessage != null)
            failX(failMessage);
        return finder.getMComponentById(id).getComponent();
    }

    public MComponent getMComponent(ComponentId id) {
        if(failMessage != null)
            failX(failMessage);
        return finder.getMComponentById(id);
    }

    public int getRowCount(ComponentId id) {
        if(failMessage != null)
            failX(failMessage);
        MCollectionComponent component = (MCollectionComponent) finder.getMComponentById(id);
        return component.getRowCount();
    }

    public int getColumnCount(ComponentId id) {
        if(failMessage != null)
            failX(failMessage);
        MTable component = (MTable) finder.getMComponentById(id);
        return component.getColumnCount();
    }

    public String[][] getContent(ComponentId id) {
        if(failMessage != null)
            failX(failMessage);
        MCollectionComponent component = (MCollectionComponent) finder.getMComponentById(id);
        return component.getContent();
    }

    public String getWindow() {
        if(failMessage != null)
            failX(failMessage);
        List<Window> windows = windowMonitor.getWindows();
        if (windows.size() > 0)
            return namingStrategy.getName((Window) windows.get(windows.size() - 1));
        return "";
    }

    public List<String> getFrames() {
        if(failMessage != null)
            failX(failMessage);
        ArrayList<String> frames = new ArrayList<String>();
        List<Window> windows = windowMonitor.getWindows();
        if (windows.size() > 0) {
            Window topWindow = (Window) windows.get(windows.size() - 1);
            List<JInternalFrame> frameObjects = collectFrames(topWindow);
            for (JInternalFrame f : frameObjects) {
                frames.add(namingStrategy.getName(f));
            }
        }
        return frames;
    }

    private List<JInternalFrame> collectFrames(Container c) {
        return collectFrames(c, new ArrayList<JInternalFrame>());
    }

    private List<JInternalFrame> collectFrames(Container c, ArrayList<JInternalFrame> arrayList) {
        if (c instanceof JInternalFrame)
            arrayList.add((JInternalFrame) c);
        Component[] components = c.getComponents();
        for (Component component : components) {
            if (component instanceof Container)
                collectFrames((Container) component, arrayList);
        }
        return arrayList;
    }

    public Map<String, JInternalFrame> getFrameObjects() {
        if(failMessage != null)
            failX(failMessage);
        Map<String, JInternalFrame> frames = new HashMap<String, JInternalFrame>();
        List<Window> windows = windowMonitor.getWindows();
        if (windows.size() > 0) {
            Window topWindow = (Window) windows.get(windows.size() - 1);
            List<JInternalFrame> frameObjects = collectFrames(topWindow);
            for (JInternalFrame f : frameObjects) {
                frames.put(namingStrategy.getName(f), f);
            }
        }
        return frames;
    }

    public Window getWindowObject() {
        if(failMessage != null)
            failX(failMessage);
        List<Window> windows = windowMonitor.getWindows();
        if (windows.size() > 0)
            return (Window) windows.get(windows.size() - 1);
        return null;
    }

    public void selectMenu(String menuitems, String keystroke) {
        play(new SelectMenuAction(menuitems, keystroke, scriptModel, windowMonitor));
    }

    public void dragAndDrop(ComponentId source, ComponentId target, String action) {
        play(new DragAndDropAction(source, target, action, scriptModel, windowMonitor));
    }

    public void assertProperty(ComponentId source, String property, String value) {
        play(new AssertPropertyAction(source, property, value, scriptModel, windowMonitor));
    }

    public void waitProperty(ComponentId source, String property, String value) {
        play(new WaitPropertyAction(source, property, value, scriptModel, windowMonitor));
    }

    public String getProperty(ComponentId id, String property) {
        if(failMessage != null)
            failX(failMessage);
        MComponent component = finder.getMComponentById(id);
        return component.getProperty(property);
    }

    public Object getPropertyObject(ComponentId id, String property) {
        if(failMessage != null)
            failX(failMessage);
        MComponent component = finder.getMComponentById(id);
        return component.getPropertyObject(property);
    }

    public void screenCapture(String imageName) {
        play(new ScreenCaptureAction(imageName, scriptModel, windowMonitor));
    }

    public void screenCapture(String imageName, String windowName) {
        play(new ScreenCaptureAction(imageName, windowName, scriptModel, windowMonitor));
    }

    public void screenCapture(String imageName, String windowName, ComponentId id) {
        MComponent component = finder.getMComponentById(id);
        play(new ScreenCaptureAction(imageName, windowName, component, scriptModel, windowMonitor));
    }

    public boolean compareImages(String path1, String path2, double differencesInPercent) throws IOException {
        if(failMessage != null)
            failX(failMessage);
        return ImageCompareAction.compare(path1, path2, differencesInPercent);
    }

    public Object getNamedComponents() {
        if(failMessage != null)
            failX(failMessage);
        return finder.getAllComponents();
    }

    public String dumpComponents() {
        if(failMessage != null)
            failX(failMessage);
        Map<String, Component> map = finder.getAllComponents();
        StringBuffer sb = new StringBuffer();
        Iterator<Entry<String, Component>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Component> entry = iterator.next();
            sb.append(entry.getKey().toString() + " - " + entry.getValue().getClass().getName() + "\n");
        }
        return sb.toString();
    }

    /**
     * Checks if the two files have identical binary content
     * 
     * @return true if identical
     */
    public boolean filesEqual(String path1, String path2) throws Exception {
        if(failMessage != null)
            failX(failMessage);
        File f1 = new File(path1);
        File f2 = new File(path2);
        if (!f1.exists() || !f2.exists())
            throw new Exception("File(s) do not exist");
        if (f1.getCanonicalPath().equals(f2.getCanonicalPath()))
            throw new Exception("Cannot compare the same file with itself");

        long len = f1.length();
        if (len != f2.length())
            return false;
        if (len == 0)
            return true;

        BufferedInputStream bin1 = null;
        BufferedInputStream bin2 = null;

        try {

            bin1 = new BufferedInputStream(new FileInputStream(f1));
            bin2 = new BufferedInputStream(new FileInputStream(f2));

            while (true) {
                int b1 = bin1.read();
                int b2 = bin2.read();
                if (b1 != b2) {
                    bin1.close();
                    bin2.close();
                    return false;
                }
                if (b1 < 0) {
                    bin1.close();
                    bin2.close();
                    return true; // end reached
                }
            }

        } finally {
            try {
                bin1.close();
            } catch (Exception e) {/* ignore */
            }
            try {
                bin2.close();
            } catch (Exception e) {/* ignore */
            }
        }
    }

    public DataReader getDataReader(String fileName) throws IOException {
        if(failMessage != null)
            failX(failMessage);
        return new DataReader(fileName, JavaRuntime.getInstance().getScript());
    }

    private void failX(String m) {
        handleFailure(new TestException(m, scriptModel, windowMonitor, failType == ABORT_TEST));
        failMessage = null;
    }

    public static void failTest(String message) {
        failType = FAIL_TEST ;
        failMessage = message ;
    }

    public static void abortTest(String message) {
        failType = ABORT_TEST ;
        failMessage = message ;
    }

}
