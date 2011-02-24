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
import java.awt.Point;
import java.awt.Window;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import net.sourceforge.marathon.action.DragAction;
import net.sourceforge.marathon.action.DragAndDropAction;
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
import net.sourceforge.marathon.api.PlaybackResult;
import net.sourceforge.marathon.component.ComponentFinder;
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

public class Marathon {
    private int delayInMS = 0;
    public ComponentFinder finder;
    public PlaybackResult result = null;
    private final IScriptModelServerPart scriptModel;
    private final INamingStrategy namingStrategy;
    private final WindowMonitor windowMonitor;

    public Marathon() {
        this(JavaRuntime.getInstance().getNamingStrategy(), JavaRuntime.getInstance().getScriptModel(), new ResolversProvider(),
                JavaRuntime.getInstance().getWindowMonitor());
    }

    public Marathon(INamingStrategy namingStrategy, IScriptModelServerPart scriptModel, ResolversProvider resolversProvider,
            WindowMonitor windowMonitor) {
        this.namingStrategy = namingStrategy;
        this.windowMonitor = windowMonitor;
//        this.finder = new ComponentFinder(Boolean.FALSE, namingStrategy, resolversProvider, scriptModel, windowMonitor);
        this.finder = JavaRuntime.getInstance().getFinder();
        this.scriptModel = scriptModel;
        String property = System.getProperty(Constants.PROP_RUNTIME_DELAY);
        if (property == null || "".equals(property))
            delayInMS = 0 ;
        else {
            try {
                delayInMS = Integer.parseInt(property);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

    }

    public void play(AbstractMarathonAction action) {
        if (delayInMS != 0) {
            new Snooze(delayInMS);
            AWTSync.sync();
        }
        try {
            action.setDelay(delayInMS);
            action.playProtected(finder);
        } catch (TestException e) {
            handleFailure(e);
        }
    }

    public void assertEquals(String message, Object expected, Object actual) {
        if (!AbstractMarathonAction.objectEquals(expected, actual)) {
            AssertionFailedError e = new AssertionFailedError(message, expected, actual, scriptModel, windowMonitor);
            e.captureScreen();
            throw e;
        }
    }

    public void assertTrue(String message, boolean actual) {
        try {
            if (!actual) {
                throw new TestException(message, scriptModel, windowMonitor);
            }
        } catch (TestException e) {
            handleFailure(e);
        }
    }

    public void setDelayInMS(int delayInMS) {
        this.delayInMS = delayInMS;
    }

    protected void handleFailure(TestException e) {
        throw e;
    }

    public void window(String windowTitle, int windowOpenWaitTime) {
        if (windowOpenWaitTime == 0)
            windowOpenWaitTime = Integer.parseInt(System.getProperty(Constants.PROP_WINDOW_TIMEOUT, "60"));
        int timeout = windowOpenWaitTime * 1000;
        Window window = windowMonitor.waitForWindowToOpen(timeout, windowTitle, scriptModel);
        finder.push(window);
    }

    public void frame(String windowTitle, int windowOpenWaitTime) {
        MComponent component = finder.getMContainerById(new ComponentId(windowTitle));
        finder.push(component.getComponent());
    }

    public void windowClosed(String windowTitle) {
        Window window = windowMonitor.getWindow(windowTitle);
        try {
            new WindowClosingAction(WindowIdCreator.createWindowId(window, windowMonitor), scriptModel).play(finder);
        } catch (Throwable t) {
            System.err.println("Warning: ignoring an error in window closed. The AUT might have been closed programmatically.");
            System.err.println("windowClosed: " + t.getMessage());
        }
    }

    public void windowChanged(String state) {
        Window window = getWindowObject();
        new WindowStateAction(WindowIdCreator.createWindowId(window, windowMonitor), new WindowState(state), scriptModel,
                windowMonitor).play(finder);
    }

    public void close() {
        finder.pop();
    }

    public void keystroke(ComponentId id, String keySequence) {
        play(new KeyStrokeAction(id, keySequence, scriptModel, windowMonitor));
    }

    public void click(String componentName, boolean isPopupTrigger, Object o1, Object o2, Object o3, Object o4, Object o5) {
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
        String componentInfo = getComponentInfo(params);
        play(new ClickAction(new ComponentId(componentName, componentInfo), position, clickCount, modifiers, isPopupTrigger,
                scriptModel, windowMonitor));
    }

    public void drag(String componentName, Object o1, Object o2, Object o3, Object o4, Object o5, Object o6) {
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
        String componentInfo = getComponentInfo(params);
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

    private String getComponentInfo(ArrayList<Object> params) {
        if (params.size() < 1)
            return null;
        return (String) params.remove(0);
    }

    public void select(ComponentId id, String text) {
        play(new SelectAction(id, text, scriptModel, windowMonitor));
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
        handleFailure(new TestException(message, scriptModel, windowMonitor));
    }

    public String getText(ComponentId id) {
        return finder.getMComponentById(id).getText();
    }

    public Component getComponent(ComponentId id) {
        return finder.getMComponentById(id).getComponent();
    }

    public MComponent getMComponent(ComponentId id) {
        return finder.getMComponentById(id);
    }

    public int getRowCount(ComponentId id) {
        MCollectionComponent component = (MCollectionComponent) finder.getMComponentById(id);
        return component.getRowCount();
    }

    public int getColumnCount(ComponentId id) {
        MTable component = (MTable) finder.getMComponentById(id);
        return component.getColumnCount();
    }

    public String[][] getContent(ComponentId id) {
        MCollectionComponent component = (MCollectionComponent) finder.getMComponentById(id);
        return component.getContent();
    }

    public String getWindow() {
        List<Window> windows = windowMonitor.getWindows();
        if (windows.size() > 0)
            return namingStrategy.getName((Window) windows.get(windows.size() - 1));
        return "";
    }

    public Window getWindowObject() {
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
        MComponent component = finder.getMComponentById(id);
        return component.getProperty(property);
    }

    public Object getPropertyObject(ComponentId id, String property) {
        MComponent component = finder.getMComponentById(id);
        return component.getPropertyObject(property);
    }

    public void screenCapture(String imageName) {
        play(new ScreenCaptureAction(imageName, scriptModel, windowMonitor));
    }

    public void screenCapture(String imageName, String windowName) {
        play(new ScreenCaptureAction(imageName, windowName, scriptModel, windowMonitor));
    }

    public Object getNamedComponents() {
        return finder.getAllComponents();
    }

    public String dumpComponents() {
        Map<String, Component> map = finder.getAllComponents();
        StringBuffer sb = new StringBuffer();
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            Object key = iterator.next();
            Component component = (Component) map.get(key);
            sb.append(key.toString() + " - " + component.getClass().getName() + "\n");
        }
        return sb.toString();
    }

    /**
     * Checks if the two files have identical binary content
     * 
     * @return true if identical
     */
    public boolean filesEqual(String path1, String path2) throws Exception {
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

        InputStream in1 = null;
        InputStream in2 = null;
        try {

            in1 = new FileInputStream(f1);
            in2 = new FileInputStream(f2);

            BufferedInputStream bin1 = new BufferedInputStream(in1);
            BufferedInputStream bin2 = new BufferedInputStream(in2);

            while (true) {
                int b1 = bin1.read();
                int b2 = bin2.read();
                if (b1 != b2)
                    return false;
                if (b1 < 0)
                    return true; // end reached
            }

        } finally {
            try {
                in1.close();
            } catch (Exception e) {/* ignore */
            }
            try {
                in2.close();
            } catch (Exception e) {/* ignore */
            }
        }
    }

    public DataReader getDataReader(String fileName) throws IOException {
        return new DataReader(fileName, JavaRuntime.getInstance().getScript());
    }
}
