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
package net.sourceforge.marathon.mocks;

import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.action.ClickAction;
import net.sourceforge.marathon.action.WindowState;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IDebugger;
import net.sourceforge.marathon.api.IPlaybackListener;
import net.sourceforge.marathon.api.IPlayer;
import net.sourceforge.marathon.api.IScript;
import net.sourceforge.marathon.api.IScriptModelClientPart;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.api.MarathonAppType;
import net.sourceforge.marathon.api.PlaybackResult;
import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.api.module.Function;
import net.sourceforge.marathon.api.module.Module;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.mpf.ISubPropertiesPanel;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.ClassPathHelper;
import net.sourceforge.marathon.util.Indent;
import net.sourceforge.marathon.util.KeyStrokeParser;
import net.sourceforge.marathon.util.OSUtils;
import net.sourceforge.marathon.util.PythonEscape;

public class MockScriptModel implements IScriptModelServerPart, IScriptModelClientPart {

    public static class MockScript implements IScript {

        public Module getModuleFuctions() {
            return null;
        }

        public String[][] getArgumentsFor(DefaultMutableTreeNode node) {
            return null;
        }

        public String getFunctionDocumentation(DefaultMutableTreeNode node) {
            return null;
        }

        public IPlayer getPlayer(IPlaybackListener playbackListener, PlaybackResult result) {
            return null;
        }

        public void runFixtureSetup() {

        }

        public void runFixtureTeardown() {
        }

        public void exec(String function) {
        }

        public IDebugger getDebugger() {
            return null;
        }

        public void attachPlaybackListener(IPlaybackListener listener) {
        }

        public Runnable playbackBody(boolean shouldRunFixture, Thread playbackThread) {
            return null;
        }

        public String evaluate(String code) {
            return null;
        }

        public boolean isCustomAssertionsAvailable() {
            return false;
        }

        public void setDataVariables(Properties dataVariables) {
        }
    }

    private IScript script = new MockScript();

    public IScript getScript(Writer out, Writer err, String script, String filename, ComponentFinder resolver, boolean isDebugging,
            WindowMonitor windowMonitor, MarathonAppType type) {
        return this.script;
    }

    public ISubPropertiesPanel[] getSubPanels(JDialog parent) {
        return null;
    }

    public String getScriptCodeForAssertContent(ComponentId componentId, String[][] arrayContent) {
        StringBuffer pythonizedString = new StringBuffer();
        pythonizedString.append("assert_content(").append(PythonEscape.encode(componentId.getName()));
        pythonizedString.append(", [ ");
        for (int i = 0; i < arrayContent.length; i++) {
            pythonizedString.append("[");
            String[] data = arrayContent[i];
            for (int j = 0; j < data.length - 1; j++) {
                pythonizedString.append(PythonEscape.encode(data[j])).append(", ");
            }
            if (data.length > 0)
                pythonizedString.append(PythonEscape.encode(data[data.length - 1]));
            if (i == arrayContent.length - 1)
                pythonizedString.append("]\n");
            else
                pythonizedString.append("],\n");
        }
        pythonizedString.append("]").append(finish(componentId));
        return pythonizedString.toString();
    }

    private String finish(ComponentId componentId) {
        String finish;
        if (componentId.getComponentInfo() != null) {
            finish = ", " + PythonEscape.encode(componentId.getComponentInfo()) + ")\n";
        } else {
            finish = ")\n";
        }
        return finish;
    }

    public String getScriptCodeForAssertProperty(ComponentId componentId, String property, String value) {
        return "assert_p(" + PythonEscape.encode(componentId.getName()) + ", " + PythonEscape.encode(property) + ", "
                + escape(PythonEscape.encode(value)) + finish(componentId);
    }

    public String getScriptCodeForWaitProperty(ComponentId componentId, String property, String value) {
        return "wait_p(" + PythonEscape.encode(componentId.getName()) + ", " + PythonEscape.encode(property) + ", "
                + escape(PythonEscape.encode(value)) + finish(componentId);
    }

    public static String escape(String encode) {
        if (encode.startsWith("/"))
            return "/" + encode;
        return encode;
    }

    public String getScriptCodeForClick(ComponentId componentId, int numberOfClicks, int modifiers, int record_click, Point position) {
        String modifiersText = ClickAction.getModifiersText(modifiers
                & ~(InputEvent.BUTTON1_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK));
        if (!modifiersText.equals(""))
            modifiersText = ", " + PythonEscape.encode(modifiersText);
        String clickCountText = (numberOfClicks > 1) ? "" + numberOfClicks : "";
        if (!clickCountText.equals(""))
            clickCountText = ", " + clickCountText;
        String positionText = "";
        if (record_click != ClickAction.RECORD_CLICK) {
            positionText = ", " + position.x + ", " + position.y;
        }
        boolean popupTrigger = (modifiers & InputEvent.BUTTON3_DOWN_MASK) != 0;
        if (popupTrigger)
            return "rightclick" + "(" + PythonEscape.encode(componentId.getName()) + clickCountText + positionText + modifiersText
                    + finish(componentId);
        else if (numberOfClicks == 2)
            return "doubleclick" + "(" + PythonEscape.encode(componentId.getName()) + positionText + modifiersText
                    + finish(componentId);
        else
            return "click" + "(" + PythonEscape.encode(componentId.getName()) + clickCountText + positionText + modifiersText
                    + finish(componentId);
    }

    public String getScriptCodeForDragAndDrop(ComponentId source, ComponentId target, int action) {
        StringBuffer python = new StringBuffer();
        python.append("drag_and_drop(" + PythonEscape.encode(source.getName()));
        if (source.getComponentInfo() != null) {
            python.append(", " + PythonEscape.encode(source.getComponentInfo()) + ", ");
        } else
            python.append(", None, ");
        python.append(PythonEscape.encode(target.getName()));
        if (target.getComponentInfo() != null) {
            python.append(", " + PythonEscape.encode(target.getComponentInfo()) + ", ");
        } else
            python.append(", None, ");
        if (action == DnDConstants.ACTION_COPY)
            python.append(PythonEscape.encode("copy"));
        else if (action == DnDConstants.ACTION_MOVE)
            python.append(PythonEscape.encode("move"));
        else if (action == DnDConstants.ACTION_LINK)
            python.append(PythonEscape.encode("link"));
        else {
            throw new RuntimeException("Unknown drag and drop operation - " + action);
        }
        python.append(")\n");
        return python.toString();
    }

    public String getScriptCodeForCapture(String windowName, String fileName) {
        String result;
        if (windowName == null) {
            result = "screen_capture(" + PythonEscape.encode(fileName) + ")\n";
        } else {
            result = "window_capture(" + PythonEscape.encode(fileName) + ", " + PythonEscape.encode(windowName) + ")\n";
        }
        return result;
    }

    public String getScriptCodeForSelect(ComponentId componentId, String text) {
        return "select(" + PythonEscape.encode(componentId.getName()) + ", " + PythonEscape.encode(text) + finish(componentId);
    }

    public String getScriptCodeForSelectMenu(KeyStroke ks, ArrayList<Object> menuList) {
        StringBuffer python = new StringBuffer();
        if (ks == null) {
            for (int i = 0; i < menuList.size() - 1; i++) {
                python.append(((MComponent) menuList.get(i)).getComponentId().getName() + ">>");
            }
            python.append(((MComponent) menuList.get(menuList.size() - 1)).getComponentId().getName());
            return "select_menu(" + PythonEscape.encode(python.toString()) + ")\n";
        } else {
            for (int i = 0; i < menuList.size() - 1; i++) {
                python.append(((JMenuItem) menuList.get(i)).getText() + ">>");
            }
            python.append(((JMenuItem) menuList.get(menuList.size() - 1)).getText());
            String keyText = KeyStrokeParser.getKeyModifierText(ks.getModifiers());
            if (!"".equals(keyText))
                keyText += "+";
            keyText += OSUtils.keyEventGetKeyText(ks.getKeyCode());
            return "select_menu(" + PythonEscape.encode(python.toString()) + ", " + PythonEscape.encode(keyText) + ")\n";
        }
    }

    public String getScriptCodeForKeystroke(char keyChar, KeyStroke keyStroke, ComponentId componentId, String textForKeyChar) {
        if (keyChar != KeyEvent.CHAR_UNDEFINED
                && (keyStroke.getModifiers() & ~(KeyEvent.SHIFT_DOWN_MASK | KeyEvent.SHIFT_MASK)) == 0) {
            return "keystroke(" + PythonEscape.encode(componentId.getName()) + ", " + PythonEscape.encode("" + textForKeyChar)
                    + finish(componentId);
        }
        String keyModifiersText = KeyStrokeParser.getKeyModifierText(keyStroke.getModifiers());
        return "keystroke(" + PythonEscape.encode(componentId.getName()) + ", "
                + PythonEscape.encode(keyModifiersText + OSUtils.keyEventGetKeyText(keyStroke.getKeyCode())) + finish(componentId);
    }

    public String getScriptCodeForWindowClose(WindowId windowId) {
        return "close()\n";
    }

    public String getScriptCodeForWindow(WindowId windowId2) {
        if (windowId2.isFrame())
            return "if frame(" + PythonEscape.encode(windowId2.toString()) + "):\n";
        return "if window(" + PythonEscape.encode(windowId2.toString()) + "):\n";
    }

    public String getFunctionCallForInsertDialog(Function node, String[] arguments) {
        String function = (String) node.getName();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < arguments.length - 1; i++) {
            buffer.append(PythonEscape.encode(arguments[i]));
            buffer.append(", ");
        }
        if (arguments.length != 0) {
            buffer.append(PythonEscape.encode(arguments[arguments.length - 1]));
        }
        return function + "(" + buffer.toString() + ")";
    }

    public String[] parseMessage(String msg) {
        Pattern p = Pattern.compile(".*\\((.*.py):(.*)\\).*");
        Matcher matcher = p.matcher(msg);
        String[] elements = null;
        if (matcher.matches()) {
            elements = new String[2];
            elements[0] = matcher.group(1);
            elements[1] = matcher.group(2);
        }
        return elements;
    }

    public String[] getFixtures() {
        File fixtureDir = new File(System.getProperty(Constants.PROP_FIXTURE_DIR));
        File[] fixtureFiles = fixtureDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".py") && !pathname.getName().equals("__init__.py")) {
                    return true;
                }
                return false;
            }
        });
        String[] fixtures = new String[fixtureFiles.length];
        for (int i = 0; i < fixtureFiles.length; i++) {
            File file = fixtureFiles[i];
            fixtures[i] = file.getName().substring(0, file.getName().length() - 3);
        }
        Arrays.sort(fixtures);
        return fixtures;
    }

    public boolean isSourceFile(File f) {
        return f.getName().endsWith(".py") && !f.getName().startsWith("__") && !f.getName().startsWith(".");
    }

    public String getSuffix() {
        return ".py";
    }

    public void createDefaultFixture(JDialog parent, Properties props, File fixtureDir, List<String> keys) {
    }

    public String getDefaultTestHeader(String fixture) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        ps.println("#{{{ Marathon");
        ps.print("from ");
        ps.print(fixture);
        ps.println(" import *");
        ps.println("#}}} Marathon");
        ps.println();
        ps.println("def test():");
        ps.print(Indent.getDefaultIndent());
        ps.print("java_recorded_version = '");
        ps.print(System.getProperty("java.version"));
        ps.println("'");
        ps.println();

        return new String(baos.toByteArray());
    }

    public String getClasspath() {
        StringBuffer sb = new StringBuffer();

        sb.append(ClassPathHelper.getClassPath(MockScriptModel.class));
        return sb.toString();
    }

    public String getFixtureHeader(String fixture) {
        return "from " + fixture + " import *\n";
    }

    public String getModuleHeader(String moduleFunction, String description) {
        return "def " + moduleFunction + "():\n" + Indent.getIndent() + "from marathon.playback import *\n\n";
    }

    public String getScriptCodeForImportAction(String pkg, String function) {
        return "from " + pkg + " import " + function + "\n";
    }

    public String getLabelForFunctionDialog(DefaultMutableTreeNode node) {
        String fqn = node.getUserObject().toString();
        if (node.getParent().getParent() != null) {
            int lastDot = fqn.lastIndexOf(".");
            if (lastDot != -1)
                fqn = fqn.substring(lastDot + 1);
        }
        return fqn;
    }

    public String getFunctionFromInsertDialog(String function) {
        String pkg = getPackageName(function);
        if (pkg != null)
            return function.substring(pkg.length() + 1);
        return function;
    }

    public String getPackageFromInsertDialog(String function) {
        return getPackageName(function);
    }

    private String getPackageName(String f) {
        int index = f.indexOf('(');
        if (index != -1)
            f = f.substring(0, index);
        f = f.trim();
        index = f.lastIndexOf('.');
        if (index == -1)
            return null;
        return f.substring(0, index);
    }

    public boolean isTestFile(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.matches("^def.*test.*():")) {
                    reader.close();
                    return true;
                }
            }
            reader.close();
        } catch (Exception e) {
        }
        return false;
    }

    public int getLinePositionForInsertion() {
        return 7;
    }

    public String getScriptCodeForWindowClosing(WindowId id) {
        return "window_closed(" + PythonEscape.encode(id.toString()) + ")\n";
    }

    public String getScriptCodeForWindowState(WindowId id, WindowState state) {
        return "window_changed(" + PythonEscape.encode(id.toString()) + ",'" + state.toString() + "')\n";
    }

    public String getScriptCodeForInsertChecklist(String fileName) {
        return "accept_checklist(" + PythonEscape.encode(fileName) + ")\n";
    }

    public String getScriptCodeForShowChecklist(String fileName) {
        return "show_checklist(" + PythonEscape.encode(fileName) + ")\n";
    }

    public String getScriptCodeForCustom(ComponentId componentId, String fcall, Object[] objects) {
        return fcall + "(" + PythonEscape.encode(componentId.getName()) + ", " + encodeParams(objects) + finish(componentId);
    }

    private String encodeParams(Object[] objects) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < objects.length; i++) {
            sb.append(PythonEscape.encode(objects[i].toString()));
            if (i < objects.length - 1)
                sb.append(", ");
        }
        return sb.toString();
    }

    public String getScriptCodeForDrag(int modifiers, Point start, Point end, ComponentId componentId) {
        String modifiersText = MouseEvent.getModifiersExText(modifiers
                & ~(MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK));
        if (!modifiersText.equals(""))
            modifiersText = ", " + PythonEscape.encode(modifiersText);
        String positionText = ", " + start.x + ", " + start.y + ", " + end.x + ", " + end.y;
        boolean right = (modifiers & MouseEvent.BUTTON3_DOWN_MASK) != 0;
        if (right) // not yet supported
            return "rightdrag(" + PythonEscape.encode(componentId.getName()) + positionText + /*
                                                                                               * modifiersText
                                                                                               * +
                                                                                               */finish(componentId);
        else
            return "drag(" + PythonEscape.encode(componentId.getName()) + positionText + /*
                                                                                          * modifiersText
                                                                                          * +
                                                                                          */finish(componentId);
    }

    public String[][] getCustomAssertions(IScript script, MComponent mcomponent) {
        return new String[0][0];
    }

    public int getLinePositionForInsertionModule() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String updateScriptWithImports(String text, HashSet<String> importStatements) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getDefaultFixtureHeader(Properties props, String launcher, List<String> keys) {
        return "";
    }

    public String getJavaRecordedVersionTag() {
        return "";
    }

    public void fileUpdated(File file, SCRIPT_FILE_TYPE type) {
        // TODO Auto-generated method stub

    }

    public String getMarathonStartMarker() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getMarathonEndMarker() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getPlaybackImportStatement() {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, Object> getFixtureProperties(String script) {
        return new HashMap<String, Object>();
    }

    public Object eval(String script) {
        return new Object();
    }

    public String getAgentJar() {
        // TODO Auto-generated method stub
        return null;
    }
}
