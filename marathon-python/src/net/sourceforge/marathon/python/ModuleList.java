package net.sourceforge.marathon.python;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.marathon.api.module.Argument;
import net.sourceforge.marathon.api.module.Argument.Type;
import net.sourceforge.marathon.api.module.Function;
import net.sourceforge.marathon.api.module.Module;

import org.python.antlr.PythonTree;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.Expr;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.If;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Num;
import org.python.antlr.ast.Str;
import org.python.antlr.ast.Tuple;
import org.python.antlr.ast.arguments;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;
import org.python.antlr.runtime.tree.CommonTree;
import org.python.core.AstList;
import org.python.core.CompileMode;
import org.python.core.CompilerFlags;
import org.python.core.ParserFacade;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 * Class that holds the module related information for Python.
 * 
 */
public class ModuleList {

    private final static class PyFileFilter implements FileFilter {
        public boolean accept(File path) {
            if (path.isDirectory() || path.getName().endsWith(".py"))
                return true;
            return false;
        }
    }

    /**
     * Root module which encloses all the modules.
     */
    Module rootModule;
    private final String[] moduleDirs;
    private final PythonInterpreter interpreter;

    public ModuleList(PythonInterpreter interpreter, String[] moduleDirs) {
        this.interpreter = interpreter;
        this.moduleDirs = moduleDirs;
        rootModule = importModules();
    }

    /**
     * Imports modules in all the module directories and adds them
     * correspondingly under a root module.
     * 
     * @return The root module.
     */
    private Module importModules() {
        Module root = loadModulesFromDirs();
        return root;
    }

    public void evaluateModulesFromRoot() {
        if (rootModule != null)
            evaluateModules(rootModule);
    }

    /**
     * Evaluates the given module and its child modules, so that these modules
     * are available for Python for execution while inserting.
     * 
     * @param module
     */
    private void evaluateModules(Module root) {
        List<Module> children = root.getChildren();
        for (Module childModule : children) {
            executeModule(childModule);
            evaluateModules(childModule);
        }
    }

    private void executeModule(Module module) {
        String importStatement = getImportStatement(module);
        interpreterExec(importStatement);
    }

    public void interpreterExec(String fName) {
        interpreter.exec(fName);
    }

    /**
     * Constructs the import statement for this module using its parents.
     * 
     * @param module
     * @return
     */
    private String getImportStatement(Module module) {
        String require = module.getName();
        Module parent = module.getParent();
        while (parent.getParent() != null) {
            require = parent.getName() + "." + require;
            parent = parent.getParent();
        }
        if (require.trim().equals(""))
            return "";
        return "import " + require;
    }

    /**
     * Loads the modules from all the Module directories.
     * 
     * @return
     */
    private Module loadModulesFromDirs() {
        Module root = new Module("Root", null);
        for (int i = 0; i < moduleDirs.length; i++) {
            File moduleDir = new File(moduleDirs[i]);
            Module moduleForDir = createModuleForDir(moduleDir, root);
            if (moduleForDir != null)
                root.addChild(moduleForDir);
        }
        return root;
    }

    /**
     * Creates a module for the given directory.
     * 
     * Recurses the directory for sub directories and files creating modules
     * corresponding to them.
     * 
     * @param moduleDir
     *            The directory which has to scanned through for modules.
     * @param parent
     *            Parent Module for this directory module.
     * @return
     */
    private Module createModuleForDir(File moduleDir, Module parent) {
        Module moduleForDir = new Module(moduleDir.getName(), parent);
        File[] files = moduleDir.listFiles(new PyFileFilter());
        if (files == null || files.length == 0)
            return null;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                Module moduleForSubDir = createModuleForDir(files[i], moduleForDir);
                if (moduleForSubDir != null)
                    moduleForDir.addChild(moduleForSubDir);
            } else {
                Module moduleForFile = createModuleFromFile(files[i], moduleForDir);
                if (moduleForFile != null)
                    moduleForDir.addChild(moduleForFile);
            }
        }
        if (moduleForDir.getChildren().size() == 0)
            return null;
        return moduleForDir;
    }

    /**
     * Creates a Module for the file. Finds the method definitions in the file
     * and adds them to the module created
     * 
     * @param file
     * @param moduleForDir
     */
    private Module createModuleFromFile(File file, Module moduleForDir) {
        FileInputStream stream;
        try {
            stream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        // Parsing the file.
        PythonTree tree = ParserFacade.parse(stream, CompileMode.exec, file.getName(), new CompilerFlags());

        // Find the definition nodes.
        List<PythonTree> defnNodes = findNodes(tree, new IPythonNodeFilter() {
            public boolean accept(PythonTree node) {
                if (node instanceof FunctionDef)
                    return true;
                return false;
            }
        });

        // If there are nod definitions in the file then do not create a module
        // for this file.
        if (defnNodes.size() == 0)
            return null;

        // Create a module for this file.
        Module moduleForThisFile = new Module(getModuleName(file), true, moduleForDir);
        for (PythonTree defn : defnNodes) {
            addNodeToModule((FunctionDef) defn, moduleForThisFile);
        }

        return moduleForThisFile;
    }

    /**
     * Finds the name, documentation and arguments of the given FunctionDef node
     * and adds it to the module.
     * 
     * @param defn
     * @param module
     */
    private void addNodeToModule(FunctionDef defn, Module module) {
        // Get the function name from definition.
        String functionName = defn.getName().toString();
        // Get all the arguments.
        List<expr> args = ((arguments) defn.getArgs()).getInternalArgs();

        // Get the default values for the arguments. According to python syntax,
        // an argument with default value cannot be followed by an argument
        // without a default value.
        List<expr> defaults = ((arguments) defn.getArgs()).getInternalDefaults();

        // Get the index of the argument from which args have default values.
        int defaultStartsFromIndex = args.size();
        if (defaults != null)
            defaultStartsFromIndex = defaultStartsFromIndex - defaults.size();

        List<Argument> argsList = new ArrayList<Argument>();
        if (args.size() > 0) {
            for (int i = 0; i < args.size(); i++) {
                expr argNode = args.get(i);
                expr defaultValue = null;
                if (i >= defaultStartsFromIndex) {
                    defaultValue = defaults.get(i - defaultStartsFromIndex);
                }
                Argument arg = makeArgFromNode(argNode, defaultValue);
                argsList.add(arg);
            }
        }
        AstList body = (AstList) defn.getBody();
        Object firstChildNode = body.get(0);
        String docNode = "";
        if (firstChildNode instanceof Expr) {
            CommonTree firstChild = ((Expr) firstChildNode).getNode();
            docNode = firstChild.toString();
        }
        String doc = "";
        if (docNode.startsWith("'''") && docNode.endsWith("'''")) {
            doc = docNode.substring(3, docNode.length() - 3);
        }

        Function function = module.addFunction(functionName, argsList, doc);
        // Get the window name for this function.
        function.setWindow(getWindowName(defn));
    }

    /**
     * Gets the top level window name in this definition.
     * 
     * A window name is considered only if the first statement is a if window
     * call.
     * 
     * @param defn
     * @return
     */
    private String getWindowName(FunctionDef defn) {
        final PythonTree[] callNodes = new PythonTree[] { null };
        findNodes(defn, new IPythonNodeFilter() {

            public boolean accept(PythonTree node) {
                if (callNodes[0] == null && isValidNode(node)) {
                    callNodes[0] = node;
                    return true;
                }
                return false;

            }

            private boolean isValidNode(PythonTree node) {
                return !(node instanceof FunctionDef) && (node instanceof stmt || node instanceof expr)
                        && !(node.getNode().toString().startsWith("'''"));
            }
        });

        if (callNodes[0] instanceof If) {
            PyObject testObject = ((If) callNodes[0]).getTest();
            if (testObject instanceof Call) {
                Call test = (Call) testObject;
                PyObject functionObject = test.getFunc();
                if (functionObject instanceof Name) {
                    Name funcName = (Name) functionObject;
                    if (funcName.getInternalId().equals("window")) {
                        expr windowNameObject = test.getInternalArgs().get(0);
                        if (windowNameObject instanceof Str) {
                            String windowName = ((Str) windowNameObject).getS().toString();
                            return windowName;
                        }
                    }
                }
            }

        }

        return null;
    }

    /**
     * Determines the type, name and other details of the argument and
     * constructs an argument object using the given argument node.
     * 
     * @param argNode
     * @param defaultValue
     * @return
     */
    private Argument makeArgFromNode(expr argNode, expr defaultValue) {
        String name = "";
        Argument arg;
        if (argNode instanceof Name) {
            name = ((Name) argNode).getId().toString();
        }

        if (defaultValue instanceof org.python.antlr.ast.List || defaultValue instanceof Tuple) {
            arg = makeArgumentWithDefaultListValue(name, defaultValue);
        } else {
            arg = makeArgumentWithDefaultStringValue(name, defaultValue);
        }

        return arg;
    }

    /**
     * Creates an object of Argument with the given name and default value. The
     * default value is found using the given defaultValue node.
     * 
     * @param name
     * @param defaultValue
     * @return
     */
    private Argument makeArgumentWithDefaultStringValue(String name, expr defaultValue) {
        String def = "";
        Type type = Type.NONE;
        type = findType(defaultValue);
        def = findStringValueFromNode(defaultValue);
        return new Argument(name, def, type);
    }

    /**
     * Finds the type of the value.
     * 
     * @param node
     * @return
     */
    private Type findType(expr node) {
        Type type = Type.NONE;
        if (node instanceof Str) {
            type = Type.STRING;
        } else if (node instanceof Num) {
            type = Type.NUMBER;
        } else if (node instanceof Name) {
            String value = ((Name) node).getInternalId();
            if (value.equals("true") || value.equals("false")) {
                type = Type.BOOLEAN;
            }
        } else if (node != null) {
            System.out.println("ModuleList.makeArgumentWithDefaultStringValue():" + node.getClass());
        }
        return type;
    }

    /**
     * Extracts the string value of the given node.
     * 
     * @param node
     * @param type
     * @return
     */
    private String findStringValueFromNode(expr node) {
        String def = "";
        if (node instanceof Str) {
            def = ((Str) node).getS().toString();
        } else if (node instanceof Num) {
            def = ((Num) node).getN().toString();
        } else if (node instanceof Name) {
            String value = ((Name) node).getInternalId();
            def = value;
        } else if (node != null) {
            System.out.println("ModuleList.makeArgumentWithDefaultStringValue():" + node.getClass());
        }
        String encodedValue = getEncodedValue(def);
        return encodedValue;
    }

    /**
     * Creates an encoded string by replacing newlines with corresponding escape
     * sequences.
     * 
     * @param def
     * @return
     */
    private String getEncodedValue(String def) {
        String enc = PythonEscape.encode(def);
        if (isEnclosedWith(enc, "'") || isEnclosedWith(enc, "\""))
            enc = enc.substring(1, enc.length() - 1);
        return enc;
    }

    /**
     * Checks whether the given string is enclosed with the given symbol. That
     * is, if the beginning and ending characters are equal to the symbol
     * specified, true is returned.
     * 
     * If the string contains only the beginning and ending symbols and nothing
     * is enclosed with in them, false is returned.
     * 
     * Ex: If the given string is (name( with symbol as ( true is returned, and
     * if the string is (( false is returned.
     * 
     * @param string
     * @param symbol
     * @return true, if the string is enclosed with the given symbol.
     */
    private boolean isEnclosedWith(String string, String symbol) {
        return !(string.equals(symbol + symbol)) && string.startsWith(symbol) && string.endsWith(symbol);
    }

    /**
     * Creates an argument which has a list of string as default value.
     * 
     * @param name
     * @param node
     * @return
     */
    private Argument makeArgumentWithDefaultListValue(String name, expr node) {
        List<expr> elements = getElements(node);
        Type type = Type.NONE;
        List<String> defaultValues = new ArrayList<String>();
        for (expr element : elements) {
            String str = findStringValueFromNode(element);
            defaultValues.add(str);
        }
        type = findType(elements.get(0));
        Argument arg = new Argument(name, defaultValues, type);
        return arg;
    }

    /**
     * Gets the list elements from List or Tuple.
     * 
     * @param node
     * @return
     */
    private List<expr> getElements(expr node) {
        List<expr> elements;
        if (node instanceof Tuple) {
            elements = ((Tuple) node).getInternalElts();
        } else {
            org.python.antlr.ast.List listNode = (org.python.antlr.ast.List) node;
            elements = listNode.getInternalElts();
        }
        return elements;
    }

    private String getModuleName(File file) {
        String name = file.getName();
        if (file.isDirectory())
            return name;
        return name.substring(0, name.length() - 3);
    }

    public Module getRoot() {
        return rootModule;
    }

    public interface IPythonNodeFilter {
        public boolean accept(PythonTree node);
    }

    public List<PythonTree> findNodes(PythonTree root, IPythonNodeFilter filter) {
        List<PythonTree> matchedNodes = new ArrayList<PythonTree>();
        findNAddNodesToList(root, filter, matchedNodes);
        return matchedNodes;
    }

    private void findNAddNodesToList(PythonTree node, IPythonNodeFilter filter, List<PythonTree> matchedNodes) {
        if (filter.accept(node)) {
            matchedNodes.add(node);
        }
        List<PythonTree> children = node.getChildren();
        if (children != null) {
            for (PythonTree child : children) {
                findNAddNodesToList(child, filter, matchedNodes);
            }
        }
    }

}
