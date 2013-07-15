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
package net.sourceforge.marathon.ruby;

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
import java.io.PrintWriter;
import java.io.StringWriter;
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
import net.sourceforge.marathon.api.module.Argument.Type;
import net.sourceforge.marathon.api.module.Function;
import net.sourceforge.marathon.api.module.Module;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.mpf.ISubPropertiesPanel;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.script.FixturePropertyHelper;
import net.sourceforge.marathon.util.ClassPathHelper;
import net.sourceforge.marathon.util.FileUtils;
import net.sourceforge.marathon.util.KeyStrokeParser;
import net.sourceforge.marathon.util.OSUtils;

import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jrubyparser.Parser;

public class RubyScriptModel implements IScriptModelClientPart, IScriptModelServerPart {

    private static final String MARATHON_RT_RUBY = locateRTJar();
    private static final String EOL = System.getProperty("line.separator");
    public static final String MARATHON_START_MARKER = "#{{{ Marathon";
    public static final String MARATHON_END_MARKER = "#}}} Marathon";

    private static Ruby ruby;
    private int lastModuleInsertionPoint;

    static {
        RubyInstanceConfig.FULL_TRACE_ENABLED = true;
        ruby = JavaEmbedUtils.initialize(new ArrayList<String>());
    }

    public void createDefaultFixture(JDialog parent, Properties props, File fixtureDir, List<String> keys) {
        FixtureGenerator fixtureGenerator = getFixtureGenerator();
        File fixtureFile = new File(fixtureDir, "default.rb");
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

    private static String locateRTJar() {
        String home = System.getProperty("marathon.home");
        if(home == null)
            home = "." ;
        File f = FileUtils.findFile(home, "marathon-rt-ruby.jar");
        if(f != null) {
            return f.getAbsolutePath();
        }
        return null;
    }

    protected FixtureGenerator getFixtureGenerator() {
        FixtureGenerator fixtureGenerator = new FixtureGenerator();
        return fixtureGenerator;
    }

    public String getClasspath() {
        StringBuffer sb = new StringBuffer();
        sb.append(getRubyJarPath()).append(File.pathSeparator);
        sb.append(ClassPathHelper.getClassPath(Parser.class)).append(File.pathSeparator);
        sb.append(ClassPathHelper.getClassPath(RubyScriptModel.class));
        return sb.toString();
    }

    public static String getRubyJarPath() {
        String property = System.getProperty(RubyScript.PROP_APPLICATION_RUBYHOME);
        if (property != null && !property.equals("")) {
            File file = new File(property, "lib");
            if (file.exists()) {
                file = new File(file, "jruby.jar");
                if (file.exists())
                    return file.getAbsolutePath();
            }
            System.err.println("Warning: Could not find ruby.jar in " + property);
        }
        String path;
        path = ClassPathHelper.getClassPath("org.jruby.Ruby");
        if (path != null)
            return path;
        path = ClassPathHelper.getClassPath(RubyScriptModel.class);
        if (new File(path).isFile()) {
            path = new File(path).getParentFile().getAbsolutePath();
        }
        StringBuffer sb = new StringBuffer();
        return sb.append(path).append(File.separator).append("jruby").append(File.separator).append("lib").append(File.separator)
                .append("jruby.jar").toString();
    }

    public String getDefaultFixtureHeader(Properties props, String launcher, List<String> keys) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        new FixtureGenerator().printFixture(props, ps, launcher, keys);
        return baos.toString();
    }

    public String getDefaultTestHeader(String fixture) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        ps.println(MARATHON_START_MARKER);
        ps.print("require_fixture '");
        ps.print(fixture);
        ps.println("'");
        ps.println(MARATHON_END_MARKER);
        ps.println();
        ps.println("def test");
        ps.println();
        ps.println();
        ps.println("end");

        return new String(baos.toByteArray());
    }

    public String getFixtureHeader(String fixture) {
        return "require_fixture '" + fixture + "'\n";
    }

    public String[] getFixtures() {
        File fixtureDir = new File(System.getProperty(Constants.PROP_FIXTURE_DIR));
        File[] fixtureFiles = fixtureDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".rb")) {
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

    private String encodeArg(String text, Argument argument) {
        if (argument.getType() == Type.REGEX)
            return "/" + text + "/";
        String decoded = ruby.evalScriptlet("\"" + text + "\"").toString();
        return encode(decoded);
    }

    public String getFunctionCallForInsertDialog(Function function, String[] arguments) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < arguments.length - 1; i += 1) {
            buffer.append(encodeArg(arguments[i], function.getArguments().get(i)));
            buffer.append(", ");
        }
        if (arguments.length != 0) {
            buffer.append(encodeArg(arguments[arguments.length - 1], function.getArguments().get(arguments.length - 1)));
        }
        String require = getRequire(function);
        return require + function.getName() + "(" + buffer.toString() + ")";
    }

    private String getRequire(Function function) {
        StringBuilder require = new StringBuilder();

        Module parent = function.getParent();
        while (parent.getParent() != null) {
            require.insert(0, "/").insert(0, parent.getName());
            parent = parent.getParent();
        }
        return require.toString();
    }

    public String getModuleHeader(String moduleFunction, String description) {
        String prefix = "=begin" + EOL + description + EOL + "=end" + EOL + EOL + "def " + moduleFunction + "()" + EOL + EOL
                + "    ";
        lastModuleInsertionPoint = prefix.length();
        return prefix + EOL + "end" + EOL;
    }

    public ISubPropertiesPanel[] getSubPanels(JDialog parent) {
        return new ISubPropertiesPanel[] { new RubyPathPanel(parent) };
    }

    public IScript getScript(Writer out, Writer err, String script, String filename, ComponentFinder resolver, boolean isDebugging,
            WindowMonitor windowMonitor, MarathonAppType type) {
        return new RubyScript(out, err, script, filename, resolver, isDebugging, windowMonitor, type);
    }

    public String getScriptCodeForAssertContent(ComponentId componentId, String[][] arrayContent) {
        StringBuffer pythonizedString = new StringBuffer();
        pythonizedString.append("assert_content(").append(encode(componentId.getName()));
        pythonizedString.append(", [ ");
        for (int i = 0; i < arrayContent.length; i++) {
            pythonizedString.append("[");
            String[] data = arrayContent[i];
            for (int j = 0; j < data.length - 1; j++) {
                pythonizedString.append(encode(data[j])).append(", ");
            }
            if (data.length > 0)
                pythonizedString.append(encode(data[data.length - 1]));
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
            finish = ", " + encode(componentId.getComponentInfo()) + ")\n";
        } else {
            finish = ")\n";
        }
        return finish;
    }

    public String getScriptCodeForAssertProperty(ComponentId componentId, String property, String value) {
        return "assert_p(" + encode(componentId.getName()) + ", " + encode(property) + ", " + escape(encode(value))
                + finish(componentId);
    }

    public String getScriptCodeForWaitProperty(ComponentId componentId, String property, String value) {
        return "wait_p(" + encode(componentId.getName()) + ", " + encode(property) + ", " + escape(encode(value))
                + finish(componentId);
    }

    public static String escape(String encode) {
        if (encode.startsWith("/"))
            return "/" + encode;
        return encode;
    }

    public String getScriptCodeForCapture(String windowName, String fileName) {
        String result;
        if (windowName == null) {
            result = "screen_capture(" + encode(fileName) + ")\n";
        } else {
            result = "window_capture(" + encode(fileName) + ", " + encode(windowName) + ")\n";
        }
        return result;
    }

    public String getScriptCodeForClick(ComponentId componentId, int numberOfClicks, int modifiers, int record_click, Point position) {
        String modifiersText = ClickAction.getModifiersText(modifiers
                & ~(InputEvent.BUTTON1_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK));
        if (!modifiersText.equals(""))
            modifiersText = ", " + encode(modifiersText);
        String clickCountText = (numberOfClicks > 1) ? "" + numberOfClicks : "";
        if (!clickCountText.equals(""))
            clickCountText = ", " + clickCountText;
        String positionText = "";
        if (record_click != ClickAction.RECORD_CLICK) {
            positionText = ", " + position.x + ", " + position.y;
        }
        boolean popupTrigger = (modifiers & InputEvent.BUTTON3_DOWN_MASK) != 0;
        if (popupTrigger)
            return "rightclick" + "(" + encode(componentId.getName()) + clickCountText + positionText + modifiersText
                    + finish(componentId);
        else if (numberOfClicks == 2)
            return "doubleclick" + "(" + encode(componentId.getName()) + positionText + modifiersText + finish(componentId);
        else
            return "click" + "(" + encode(componentId.getName()) + clickCountText + positionText + modifiersText
                    + finish(componentId);
    }

    public String getScriptCodeForDragAndDrop(ComponentId source, ComponentId target, int action) {
        StringBuffer python = new StringBuffer();
        python.append("drag_and_drop(" + encode(source.getName()));
        if (source.getComponentInfo() != null) {
            python.append(", " + encode(source.getComponentInfo()) + ", ");
        } else
            python.append(", nil, ");
        python.append(encode(target.getName()));
        if (target.getComponentInfo() != null) {
            python.append(", " + encode(target.getComponentInfo()) + ", ");
        } else
            python.append(", nil, ");
        if (action == DnDConstants.ACTION_COPY)
            python.append(encode("copy"));
        else if (action == DnDConstants.ACTION_MOVE)
            python.append(encode("move"));
        else if (action == DnDConstants.ACTION_LINK)
            python.append(encode("link"));
        else {
            throw new RuntimeException("Unknown drag and drop operation - " + action);
        }
        python.append(")\n");
        return python.toString();
    }

    public String getScriptCodeForImportAction(String pkg, String function) {
        return "require '" + pkg + "'";
    }

    public String getScriptCodeForSelect(ComponentId componentId, String text) {
        return "select(" + encode(componentId.getName()) + ", " + encode(text) + finish(componentId);
    }

    public String getScriptCodeForSelectMenu(KeyStroke ks, ArrayList<Object> menuList) {
        StringBuffer python = new StringBuffer();
        if (ks == null) {
            for (int i = 0; i < menuList.size() - 1; i++) {
                python.append(((MComponent) menuList.get(i)).getComponentId().getName() + ">>");
            }
            python.append(((MComponent) menuList.get(menuList.size() - 1)).getComponentId().getName());
            return "select_menu(" + encode(python.toString()) + ")\n";
        } else {
            for (int i = 0; i < menuList.size() - 1; i++) {
                python.append(((JMenuItem) menuList.get(i)).getText() + ">>");
            }
            python.append(((JMenuItem) menuList.get(menuList.size() - 1)).getText());
            String keyText = KeyStrokeParser.getKeyModifierText(ks.getModifiers());
            keyText += OSUtils.keyEventGetKeyText(ks.getKeyCode());
            return "select_menu(" + encode(python.toString()) + ", " + encode(keyText) + ")\n";
        }
    }

    public String getScriptCodeForKeystroke(char keyChar, KeyStroke keyStroke, ComponentId componentId, String textForKeyChar) {
        if (keyChar != KeyEvent.CHAR_UNDEFINED
                && (keyStroke.getModifiers() & ~(KeyEvent.SHIFT_DOWN_MASK | KeyEvent.SHIFT_MASK)) == 0
                && !Character.isISOControl(keyChar)) {
            return "keystroke(" + encode(componentId.getName()) + ", " + encode("" + textForKeyChar) + finish(componentId);
        }
        String keyModifiersText = KeyStrokeParser.getKeyModifierText(keyStroke.getModifiers());
        return "keystroke(" + encode(componentId.getName()) + ", "
                + encode(keyModifiersText + OSUtils.keyEventGetKeyText(keyStroke.getKeyCode())) + finish(componentId);
    }

    public String getScriptCodeForWindow(WindowId windowId2) {
        if (windowId2.isFrame())
            return "with_frame(" + encode(windowId2.toString()) + ") {\n";
        return "with_window(" + encode(windowId2.toString()) + ") {\n";
    }

    public String getScriptCodeForWindowClose(WindowId windowId) {
        return "}\n";
    }

    public String getSuffix() {
        return ".rb";
    }

    public boolean isSourceFile(File f) {
        return f.getName().endsWith(".rb") && !f.getName().startsWith(".");
    }

    public String[] parseMessage(String msg) {
        Pattern p = Pattern.compile(".*\\((.*.rb):(.*)\\).*");
        Matcher matcher = p.matcher(msg);
        String[] elements = null;
        if (matcher.matches()) {
            elements = new String[2];
            elements[0] = matcher.group(1);
            elements[1] = matcher.group(2);
        }
        return elements;
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
        index = f.lastIndexOf('/');
        if (index == -1)
            return null;
        return f.substring(0, index);
    }

    public boolean isTestFile(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.matches(".*def.*test.*")) {
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
        return "window_closed(" + encode(id.toString()) + ")\n";
    }

    public String getScriptCodeForWindowState(WindowId id, WindowState state) {
        return "window_changed('" + state.toString() + "')\n";
    }

    public String getScriptCodeForInsertChecklist(String fileName) {
        return "accept_checklist(" + encode(fileName) + ")\n";
    }

    public String getScriptCodeForShowChecklist(String fileName) {
        return "show_checklist(" + encode(fileName) + ")\n";
    }

    public String getScriptCodeForCustom(ComponentId componentId, String fcall, Object[] objects) {
        return fcall + "(" + encode(componentId.getName()) + ", " + encodeParams(objects) + finish(componentId);
    }

    private String encodeParams(Object[] objects) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < objects.length; i++) {
            sb.append(encode(objects[i].toString()));
            if (i < objects.length - 1)
                sb.append(", ");
        }
        return sb.toString();
    }

    public String getScriptCodeForDrag(int modifiers, Point start, Point end, ComponentId componentId) {
        String positionText = ", " + start.x + ", " + start.y + ", " + end.x + ", " + end.y;
        boolean right = (modifiers & MouseEvent.BUTTON3_DOWN_MASK) != 0;
        if (right) // not yet supported
            return "rightdrag(" + encode(componentId.getName()) + positionText + /*
                                                                                  * modifiersText
                                                                                  * +
                                                                                  */finish(componentId);
        else
            return "drag(" + encode(componentId.getName()) + positionText + /*
                                                                             * modifiersText
                                                                             * +
                                                                             */finish(componentId);
    }

    public static String encode(String name) {
        if (name == null)
            name = "";
        return inspect(name);
    }

    public static String inspect(String string) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '"' || c == '\\') {
                sb.append("\\").append(c);
            } else if (c == '#' && chars[i + 1] == '{') {
                sb.append("\\").append(c);
            } else if (c == '\n') {
                sb.append("\\").append('n');
            } else if (c == '\r') {
                sb.append("\\").append('r');
            } else if (c == '\t') {
                sb.append("\\").append('t');
            } else if (c == '\f') {
                sb.append("\\").append('f');
            } else if (c == '\013') {
                sb.append("\\").append('v');
            } else if (c == '\010') {
                sb.append("\\").append('b');
            } else if (c == '\007') {
                sb.append("\\").append('a');
            } else if (c == '\033') {
                sb.append("\\").append('e');
            } else {
                sb.append(c);
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    public String[][] getCustomAssertions(IScript script, MComponent mcomponent) {
        return ((RubyScript) script).getCustomAssertions(mcomponent);
    }

    public int getLinePositionForInsertionModule() {
        return lastModuleInsertionPoint;
    }

    public String updateScriptWithImports(String text, HashSet<String> importStatements) {
        StringBuilder sb = new StringBuilder(text);
        int endOffset = sb.indexOf(RubyScriptModel.MARATHON_END_MARKER);
        if (endOffset == -1) {
            StringWriter sw = new StringWriter();
            PrintWriter ps = new PrintWriter(sw);
            ps.println(MARATHON_START_MARKER);
            for (String ims : importStatements) {
                ps.println(ims);
            }
            ps.println(MARATHON_END_MARKER);
            ps.close();
            sb.replace(0, 0, sw.toString());
        } else {
            int startOffset = sb.indexOf(MARATHON_START_MARKER);
            if (startOffset == -1)
                startOffset = 0;
            String header = text.substring(startOffset, endOffset);
            for (String ims : importStatements) {
                if (!header.contains(ims)) {
                    sb.replace(endOffset, endOffset, ims + EOL);
                    endOffset = sb.indexOf(MARATHON_END_MARKER);
                }
            }
        }
        return sb.toString();
    }

    public static Ruby getRubyInterpreter() {
        return ruby;
    }

    public String getJavaRecordedVersionTag() {
        return "$java_recorded_version=\"" + System.getProperty("java.version") + "\"";
    }

    public void fileUpdated(File file, SCRIPT_FILE_TYPE type) {
    }

    public String getMarathonStartMarker() {
        return MARATHON_START_MARKER;
    }

    public String getMarathonEndMarker() {
        return MARATHON_END_MARKER;
    }

    public String getPlaybackImportStatement() {
        return "";
    }

    private static final Pattern FIXTURE_IMPORT_MATCHER = Pattern.compile("\\s*require_fixture\\s\\s*['\"](.*)['\"].*");

    public Map<String, Object> getFixtureProperties(String script) {
        return new FixturePropertyHelper(this).getFixtureProperties(script, FIXTURE_IMPORT_MATCHER);
    }

    public Object eval(String script) {
        return ruby.evalScriptlet(script);
    }

    public String getAgentJar() {
        return MARATHON_RT_RUBY;
    }

}
