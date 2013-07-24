/*******************************************************************************
 *  
 *  $Id: PythonScriptModel.java 273 2009-01-18 05:02:54Z kd $
 *  Copyright (C) 2006 Jalian Systems Private Ltd.
 *  Copyright (C) 2006 Contributors to Marathon OSS Project
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
package net.sourceforge.marathon.python;

import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.action.ClickAction;
import net.sourceforge.marathon.action.WindowState;
import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IScript;
import net.sourceforge.marathon.api.IScriptModelClientPart;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.api.MarathonAppType;
import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.api.module.Argument;
import net.sourceforge.marathon.api.module.Function;
import net.sourceforge.marathon.api.module.Module;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.mpf.ISubPropertiesPanel;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.script.FixturePropertyHelper;
import net.sourceforge.marathon.util.ClassPathHelper;
import net.sourceforge.marathon.util.FileUtils;
import net.sourceforge.marathon.util.Indent;
import net.sourceforge.marathon.util.KeyStrokeParser;
import net.sourceforge.marathon.util.OSUtils;

import org.python.core.PyString;
import org.python.util.PythonInterpreter;

public class PythonScriptModel implements IScriptModelServerPart, IScriptModelClientPart {

    public static final String MARATHON_START_MARKER = "#{{{ Marathon";
    public static final String MARATHON_END_MARKER = "#}}} Marathon";
    public static final String MARATHON_IMPORT_FROM_PLAYBACK = "from marathon.playback import *";

    private static PythonInterpreter interpreter;

    public IScript getScript(Writer out, Writer err, String script, String filename, ComponentFinder resolver, boolean isDebugging,
            WindowMonitor windowMonitor, MarathonAppType type) {
        return new PythonScript(out, err, script, filename, resolver, windowMonitor, type);
    }

    public ISubPropertiesPanel[] getSubPanels(JDialog parent) {
        return new ISubPropertiesPanel[] { new PythonPathPanel(parent) };
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

    /**
     * Returns the function call script prefixed with the location of the file
     * in which this function is present.
     */
    public String getFunctionCallForInsertDialog(Function function, String[] arguments) {
        String argumentList = makeArgumentsList(function.getArguments(), arguments);
        String functionCall = function.getName() + "(" + argumentList + ")";
        String prefix = getPrefixForImport(function);
        return prefix + functionCall;
    }

    private String getPrefixForImport(Function function) {
        StringBuilder require = new StringBuilder();
        Module parent = function.getParent();
        while (parent.getParent() != null) {
            require.insert(0, '.').insert(0, parent.getName());
            parent = parent.getParent();
        }
        return require.toString();
    }

    /**
     * Creates the arguments list which has to be in the function call.
     * 
     * @param defnArguments
     * @param callArguments
     * @return
     */
    private String makeArgumentsList(List<Argument> defnArguments, String[] callArguments) {
        StringBuilder sbr = new StringBuilder();
        int size = defnArguments.size();
        for (int i = 0; i < size; i++) {
            sbr.append(encode(callArguments[i]));
            sbr.append(", ");
        }

        int argsEndIndex = sbr.lastIndexOf(", ");
        if (argsEndIndex != -1)
            return sbr.substring(0, argsEndIndex).toString();
        else
            return sbr.toString();
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
        FixtureGenerator fixtureGenerator = new FixtureGenerator();
        File fixtureFile = new File(fixtureDir, "default.py");
        if (fixtureFile.exists()) {
            int option = JOptionPane.showConfirmDialog(parent, "File " + fixtureFile + " exists\nDo you want to overwrite",
                    "File Exists", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            if (option != JOptionPane.YES_OPTION)
                return;
        }
        PrintStream ps = null;
        try {
            ps = new PrintStream(new FileOutputStream(fixtureFile));
            String launcher = props.getProperty(Constants.PROP_PROJECT_LAUNCHER_MODEL);
            props.setProperty(Constants.FIXTURE_DESCRIPTION, props.getProperty(Constants.FIXTURE_DESCRIPTION, "Default Fixture"));
            fixtureGenerator.printFixture(props, ps, launcher, keys);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ps != null)
                ps.close();
        }
    }

    public String getDefaultTestHeader(String fixture) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        ps.println(MARATHON_START_MARKER);
        ps.print("from ");
        ps.print(fixture);
        ps.println(" import *");
        ps.println(MARATHON_END_MARKER);
        ps.println();
        ps.println("def test():");
        ps.println();
        ps.println();
        ps.print(Indent.getDefaultIndent());
        ps.print("pass");

        return new String(baos.toByteArray());
    }

    public String getDefaultFixtureHeader(Properties props, String launcher, List<String> keys) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        new FixtureGenerator().printFixture(props, ps, launcher, keys);
        return baos.toString();
    }

    public String getClasspath() {
        StringBuffer sb = new StringBuffer();

        sb.append(getJythonJarPath()).append(File.pathSeparator);
        sb.append(ClassPathHelper.getClassPath(PythonScriptModel.class));
        return sb.toString();
    }

    private String getJythonJarPath() {
        String property = System.getProperty(PythonScript.PROP_APPLICATION_PYTHONHOME);
        if (property != null && !property.equals("")) {
            File file = new File(property, "jython.jar");
            if (file.exists())
                return file.getAbsolutePath();
            file = new File(property, "jython-complete.jar");
            if (file.exists())
                return file.getAbsolutePath();
            System.err.println("Warning: Could not find jython.jar in " + property + " Using default");
        }
        String path;
        path = ClassPathHelper.getClassPath("org.python.core.Py");
        if (path != null)
            return path;
        path = ClassPathHelper.getClassPath(PythonScriptModel.class);
        if (new File(path).isFile()) {
            path = new File(path).getParentFile().getAbsolutePath();
        }
        StringBuffer sb = new StringBuffer();
        sb.append(path).append(File.separator).append("jython").append(File.separator).append("jython.jar");
        File file = new File(sb.toString());
        if (file.exists())
            return file.getAbsolutePath();
        System.err.println("Could not find jython.jar... is the setup OK?");
        return sb.toString();
    }

    public String getFixtureHeader(String fixture) {
        return "from " + fixture + " import *\n";
    }

    public String getModuleHeader(String moduleFunction, String description) {
        String moduleSignature = "\ndef " + moduleFunction + "():\n";
        String moduleDesc = description.trim().equals("") ? "" : Indent.getIndent() + "'''" + description + "'''\n";
        return moduleSignature + moduleDesc + "\n" + Indent.getIndent() + "pass\n";
    }

    public String getScriptCodeForImportAction(String pkg, String function) {
        int funcNameEndIndex = function.indexOf('(');
        if (funcNameEndIndex != -1)
            function = function.substring(0, funcNameEndIndex);
        return "from " + pkg + " import " + function + "\n";
    }

    /**
     * Returns back only the function call after stripping the package name/path
     * if prefixed.
     * 
     * @param function
     */
    public String getFunctionFromInsertDialog(String function) {
        String pkg = getPackageFromInsertDialog(function);
        if (pkg != null) {
            return function.substring(pkg.length() + 1);
        }
        return function;
    }

    /**
     * Returns the package name if it is prefixed with the function specified.
     * 
     * That is if the function string is Mod/subDir/file/func('/abcd','1') then
     * Mod/subDir/file is returned.
     * 
     * @param function
     * @return
     */
    public String getPackageFromInsertDialog(String function) {
        int argBeginIndex = function.indexOf('(');
        if (argBeginIndex != -1) {
            function = function.substring(0, argBeginIndex);
        }

        int packageEndIndex = function.lastIndexOf(".");
        if (packageEndIndex == -1)
            return null;

        return function.substring(0, packageEndIndex);
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
        } catch (IOException e) {
        }
        return false;
    }

    public int getLinePositionForInsertion() {
        return 6;
    }

    public String getScriptCodeForWindowClosing(WindowId id) {
        return "window_closed(" + PythonEscape.encode(id.toString()) + ")\n";
    }

    public String getScriptCodeForWindowState(WindowId id, WindowState state) {
        return "window_changed('" + state.toString() + "')\n";
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
        return ((PythonScript) script).getCustomAssertions(mcomponent);
    }

    public int getLinePositionForInsertionModule() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Inserts the import statements in between Marathon Monikers and returns
     * the script after insertion of the import statements.
     */
    public String updateScriptWithImports(String text, HashSet<String> importStatements) {
        StringBuilder concatImports = new StringBuilder();

        String existingImports = getImportsBetweenMonikers(text);

        for (String imports : importStatements) {
            imports = imports.replace(File.separatorChar, '.');
            if (!importAlreadyExists(existingImports, imports))
                concatImports.append(imports);
        }
        String importAddedScript = addImportToScript(text, concatImports.toString());
        return importAddedScript;
    }

    /**
     * Returns the script thats present between the marathon monikers. Empty
     * string is returned if either of monikers is not found.
     * 
     * @param text
     * @return
     */
    private String getImportsBetweenMonikers(String script) {
        int endMonikerIndex = findMonikerIndex(script, MARATHON_END_MARKER);
        int beginMonikerIndex = findMonikerIndex(script, MARATHON_START_MARKER);
        if (endMonikerIndex == -1 || beginMonikerIndex == -1)
            return "";
        return script.substring(beginMonikerIndex + MARATHON_START_MARKER.length(), endMonikerIndex);
    }

    /**
     * Checks the script between the monikers, whether the given import
     * statement is already present or not.
     * 
     * @param existingImports
     * @param importStatement
     * @return
     */
    private boolean importAlreadyExists(String existingImports, String importStatement) {
        return existingImports.indexOf(importStatement) != -1;
    }

    /**
     * Finds the marathon monikers and adds the import given import statements
     * between the begin and end monikers.
     * 
     * @param script
     * @param importStatements
     * @return
     */
    private String addImportToScript(String script, String importStatements) {
        StringBuilder updatedScript = new StringBuilder(script);
        int endMonikerIndex = findMonikerIndex(script, MARATHON_END_MARKER);
        if (endMonikerIndex == -1) {
            importStatements = MARATHON_START_MARKER + "\n" + importStatements + MARATHON_END_MARKER + "\n";
            endMonikerIndex = 0;
        }
        updatedScript.insert(endMonikerIndex, importStatements);
        return updatedScript.toString();
    }

    /**
     * Returns the first offset of the given moniker in the script
     * 
     * @param script
     * @param moniker
     * @return
     */
    private int findMonikerIndex(String script, String moniker) {
        return script.indexOf(moniker);
    }

    public String getJavaRecordedVersionTag() {
        return "set_java_recorded_version(\"" + System.getProperty("java.version") + "\")";
    }

    public void fileUpdated(File file, SCRIPT_FILE_TYPE type) {
        try {
            if (type == SCRIPT_FILE_TYPE.MODULE) {
                File projectHome = new File(System.getProperty(Constants.PROP_PROJECT_DIR));
                updateInitForDirectory(file.getCanonicalFile().getParentFile(), projectHome);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds the __init__.py file in the required directories.
     * 
     * @param directory
     * @param projectHome
     */
    private void updateInitForDirectory(File directory, File projectHome) {
        try {
            if (directory.getCanonicalPath().equals(projectHome.getCanonicalPath()))
                return;
            File initFile = new File(directory, "__init__.py");
            if (!initFile.exists())
                if (!initFile.createNewFile())
                    throw new IOException("Creation of init file " + initFile + " failed");
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateInitForDirectory(directory.getParentFile(), projectHome);
    }

    public static PythonInterpreter getInterpreter() {
        if (interpreter == null)
            interpreter = new PythonInterpreter();
        return interpreter;
    }

    public String encode(String arg) {
        if (arg == null)
            return "None";
        String decodedArg = PyString.decode_UnicodeEscape(arg, 0, arg.length(), "", true);
        return (new PyString(decodedArg)).__repr__().toString();
    }

    public String getMarathonStartMarker() {
        return MARATHON_START_MARKER;
    }

    public String getMarathonEndMarker() {
        return MARATHON_END_MARKER;
    }

    public String getPlaybackImportStatement() {
        return MARATHON_IMPORT_FROM_PLAYBACK;
    }

    private static final Pattern FIXTURE_IMPORT_MATCHER = Pattern.compile("\\s*from\\s\\s*(.*)\\s\\s*import \\*");
    private static final String MARATHON_RT_PYTHON = locateRTJar();

    public Map<String, Object> getFixtureProperties(String script) {
        return new FixturePropertyHelper(this).getFixtureProperties(script, FIXTURE_IMPORT_MATCHER);
    }

    private static String locateRTJar() {
        String home = System.getProperty("marathon.home");
        if (home == null)
            home = ".";
        File f = FileUtils.findFile(home, "marathon-rt-python.jar");
        if (f != null) {
            return f.getAbsolutePath();
        }
        return null;
    }

    public Object eval(String script) {
        getInterpreter().exec(script);
        return getInterpreter().eval("Fixture_properties");
    }

    public String getAgentJar() {
        return MARATHON_RT_PYTHON;
    }

}
