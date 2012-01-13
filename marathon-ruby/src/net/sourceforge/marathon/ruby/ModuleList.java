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

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.sourceforge.marathon.api.module.Argument;
import net.sourceforge.marathon.api.module.Argument.Type;
import net.sourceforge.marathon.api.module.Function;
import net.sourceforge.marathon.api.module.Module;

import org.jruby.Ruby;
import org.jrubyparser.Parser;
import org.jrubyparser.ast.ArgsNode;
import org.jrubyparser.ast.ArrayNode;
import org.jrubyparser.ast.BignumNode;
import org.jrubyparser.ast.DefnNode;
import org.jrubyparser.ast.FCallNode;
import org.jrubyparser.ast.FalseNode;
import org.jrubyparser.ast.FixnumNode;
import org.jrubyparser.ast.FloatNode;
import org.jrubyparser.ast.INameNode;
import org.jrubyparser.ast.ListNode;
import org.jrubyparser.ast.LocalAsgnNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.RegexpNode;
import org.jrubyparser.ast.StrNode;
import org.jrubyparser.ast.TrueNode;
import org.jrubyparser.lexer.Lexer;
import org.jrubyparser.lexer.LexerSource;
import org.jrubyparser.parser.ParserConfiguration;
import org.jrubyparser.parser.Tokens;

public class ModuleList {
    private Module topModule;
    private final String[] moduleDirs;
    private final Ruby interpreter;

    public ModuleList(Ruby interpreter, String[] moduleDirs) {
        this.interpreter = interpreter;
        this.moduleDirs = moduleDirs;
        topModule = importModules();
    }

    public ModuleList(Ruby newInstance, String moduleDir) {
        this(newInstance, new String[] { moduleDir });
    }

    private Module importModules() {
        Module top = loadModulesFromFSMulti();
        top.setName("Functions");
        return top;
    }

    private Module loadModulesFromFSMulti() {
        Module module = new Module("Functions", null);
        for (int i = 0; i < moduleDirs.length; i++) {
            File file = new File(moduleDirs[i]);
            File[] files = file.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    if (pathname.getName().startsWith("."))
                        return false;
                    return pathname.isDirectory() || pathname.getName().endsWith(".rb");
                }
            });
            for (File child : files) {
                Module m = loadModulesFromFS(child, module);
                if (m != null)
                    module.addChild(m);
            }
        }
        return module;
    }

    private Module loadModulesFromFS(File file, Module parent) {
        if (file.isDirectory()) {
            Module module = new Module(getModuleName(file), parent);
            File[] files = file.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    if (pathname.getName().startsWith("."))
                        return false;
                    return pathname.isDirectory() || pathname.getName().endsWith(".rb");
                }
            });
            for (File child : files) {
                Module m = loadModulesFromFS(child, module);
                if (m != null)
                    module.addChild(m);
            }
            if (module.getChildren().size() > 0)
                return module;
            return null;
        }
        return loadFunctionsFromFile(file, parent);
    }

    private Module loadFunctionsFromFile(File file, Module parent) {
        try {
            Properties docNodes = collectDocNodes(file);
            Module module = new Module(getModuleName(file), true, parent);
            Parser parser = new Parser();
            Node node = parser.parse(file.getName(), new FileReader(file), new ParserConfiguration());
            List<Node> defnNodes = findNodes(node, new INodeFilter() {

                public void visitStart(Node node) {
                }

                public void visitEnd(Node node) {
                }

                public boolean accept(Node node) {
                    return node instanceof DefnNode;
                }
            });
            for (Node defn : defnNodes) {
                addNodeToModule((DefnNode) defn, module, docNodes);
            }
            return module;
        } catch (Throwable t) {
            new Exception("Error processing: " + file, t).printStackTrace();
        }
        return null;
    }

    private Properties collectDocNodes(File file) throws IOException {
        Lexer lexer = new Lexer();
        lexer.setSource(LexerSource.getSource(file.getName(), new FileReader(file), new ParserConfiguration()));
        lexer.setPreserveSpaces(true);
        lexer.setWarnings(new Parser.NullWarnings());
        String doc;
        int token = -1;
        Properties props = new Properties();
        while (lexer.advance()) {
            if (token == -1)
                token = lexer.token();
            if (token == Tokens.tDOCUMENTATION) {
                doc = "=" + lexer.getTokenBuffer().toString();
                while (lexer.advance() && (token = lexer.token()) == Tokens.tWHITESPACE) {
                }
                if (token != Tokens.kDEF)
                    continue;
                while (lexer.advance() && (token = lexer.token()) == Tokens.tWHITESPACE) {
                }
                if (token != Tokens.tIDENTIFIER)
                    continue;
                props.setProperty(lexer.getTokenBuffer().toString(), doc);
            }
            token = -1;
        }
        return props;
    }

    private void addNodeToModule(DefnNode defn, Module module, Properties docNodes) {
        List<Argument> args = new ArrayList<Argument>();
        ArgsNode argsNode = defn.getArgsNode();
        if (argsNode.getBlock() != null)
            return;
        ListNode pre = argsNode.getPre();
        if (pre != null) {
            for (int i = 0; i < pre.size(); i++) {
                Node node = pre.get(i);
                if (node instanceof INameNode)
                    args.add(new Argument(((INameNode) node).getName()));
                else
                    return;
            }
        }
        ListNode optional = argsNode.getOptional();
        if (optional != null) {
            for (int i = 0; i < optional.size(); i++) {
                Node node = optional.get(i);
                if (!(node instanceof LocalAsgnNode))
                    return;
                LocalAsgnNode lan = (LocalAsgnNode) node;
                Node valueNode = lan.getValueNode();
                if ((valueNode instanceof StrNode))
                    args.add(new Argument(lan.getName(), argEncode(((StrNode) valueNode).getValue().toString()), Type.STRING));
                else if (valueNode instanceof RegexpNode)
                    args.add(new Argument(lan.getName(), ((RegexpNode) valueNode).getValue().toString(), Type.REGEX));
                else if (valueNode instanceof BignumNode)
                    args.add(new Argument(lan.getName(), "" + ((BignumNode) valueNode).getValue().toString(), Type.NUMBER));
                else if (valueNode instanceof FixnumNode)
                    args.add(new Argument(lan.getName(), ((FixnumNode) valueNode).getValue() + "", Type.NUMBER));
                else if (valueNode instanceof FloatNode)
                    args.add(new Argument(lan.getName(), ((FloatNode) valueNode).getValue() + "", Type.NUMBER));
                else if (valueNode instanceof TrueNode)
                    args.add(new Argument(lan.getName(), ((TrueNode) valueNode).getName(), Type.BOOLEAN));
                else if (valueNode instanceof FalseNode)
                    args.add(new Argument(lan.getName(), ((FalseNode) valueNode).getName(), Type.BOOLEAN));
                else if (valueNode instanceof ArrayNode && ((ArrayNode) valueNode).size() > 0) {
                    ArrayNode arrayNode = (ArrayNode) valueNode;
                    List<String> argValue = getListValue(arrayNode);
                    if (argValue != null) {
                        Node node2 = arrayNode.get(0);
                        Type type = Type.NONE;
                        if (node2 instanceof StrNode)
                            type = Type.STRING;
                        else if (node2 instanceof RegexpNode)
                            type = Type.REGEX;
                        else if (node instanceof BignumNode || node instanceof FixnumNode || node instanceof FloatNode)
                            type = Type.NUMBER;
                        args.add(new Argument(lan.getName(), argValue, type));
                    } else
                        return;
                } else {
                    return;
                }
            }
        }
        String name = defn.getName();
        Function f = module.addFunction(name, args, docNodes.getProperty(name));
        f.setWindow(getWindowName(defn));
        return;
    }

    private List<String> getListValue(ArrayNode arrayNode) {
        boolean isString = false;
        boolean isRegex = false;
        boolean isNumber = false;
        Node node = arrayNode.get(0);
        if (node instanceof StrNode)
            isString = true;
        else if (node instanceof RegexpNode)
            isRegex = true;
        else if (node instanceof BignumNode || node instanceof FixnumNode || node instanceof FloatNode)
            isNumber = true;
        else
            return null;

        List<String> l = new ArrayList<String>();
        for (int i = 0; i < arrayNode.size(); i++) {
            Node anode = arrayNode.get(i);
            if (isString && anode instanceof StrNode) {
                l.add(argEncode(((StrNode) anode).getValue().toString()));
            } else if (isRegex && anode instanceof RegexpNode) {
                l.add("/" + ((RegexpNode) anode).getValue().toString() + "/");
            } else if (isNumber && anode instanceof BignumNode) {
                l.add("" + ((BignumNode) anode).getValue().toString());
            } else if (isNumber && anode instanceof FixnumNode) {
                l.add("" + ((FixnumNode) anode).getValue());
            } else if (isNumber && anode instanceof FloatNode) {
                l.add("" + ((FloatNode) anode).getValue());
            } else
                return null;
        }
        return l;
    }

    private String getWindowName(DefnNode defn) {
        final FCallNode[] callNodes = { null };

        findNodes(defn, new INodeFilter() {

            public void visitStart(Node node) {
            }

            public void visitEnd(Node node) {
            }

            public boolean accept(Node node) {
                if (node instanceof FCallNode && ((FCallNode) node).getName().equals("with_window") && callNodes[0] == null) {
                    callNodes[0] = (FCallNode) node;
                }
                return false;
            }
        });
        if (callNodes[0] == null)
            return null;
        return validWithCallNode(callNodes[0]);
    }

    private String validWithCallNode(FCallNode node) {
        Node argsNode = node.getArgsNode();
        if (!(argsNode instanceof ArrayNode))
            return null;
        ArrayNode aNode = (ArrayNode) argsNode;
        if (aNode.size() != 1)
            return null;
        Node node2 = aNode.get(0);
        if (!(node2 instanceof StrNode))
            return null;
        return ((StrNode) node2).getValue();
    }

    private String argEncode(String string) {
        String s = interpreter.newString(string).inspect().toString();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\""))
            return s.substring(1, s.length() - 1);
        return s;
    }

    private String getModuleName(File file) {
        String name = file.getName();
        if (file.isDirectory())
            return name;
        return name.substring(0, name.length() - 3);
    }

    public Module getTop() {
        return topModule;
    }

    public interface INodeFilter {

        public boolean accept(Node node);

        public void visitStart(Node node);

        public void visitEnd(Node node);
    }

    public static List<Node> findNodes(Node root, INodeFilter filter) {
        ArrayList<Node> nodes = new ArrayList<Node>();
        return findNodes(root, nodes, filter);
    }

    private static List<Node> findNodes(Node root, ArrayList<Node> nodes, INodeFilter filter) {
        filter.visitStart(root);
        if (filter.accept(root))
            nodes.add(root);
        List<Node> childNodes = root.childNodes();
        for (Node child : childNodes) {
            findNodes(child, nodes, filter);
        }
        filter.visitEnd(root);
        return nodes;
    }

}
