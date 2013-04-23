package net.sourceforge.marathon.suite.editor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sourceforge.marathon.display.DisplayWindow;
import net.sourceforge.marathon.editor.IContentChangeListener;
import net.sourceforge.marathon.editor.IEditor;
import net.sourceforge.marathon.editor.ISearchDialog;
import net.sourceforge.marathon.editor.IStatusBar;
import net.sourceforge.marathon.junit.MarathonTestCase;
import net.sourceforge.marathon.junit.TestCreator;
import net.sourceforge.marathon.junit.swingui.TestSuitePanel;
import net.sourceforge.marathon.util.UIUtils;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class SuiteEditor implements IEditor {

    private final static class SuiteListCellRenderer extends DefaultListCellRenderer {
        private static final URL ICON_FILE = DisplayWindow.class.getResource("icons/enabled/file.gif");
        private static final URL ICON_FOLDER = DisplayWindow.class.getResource("icons/enabled/folder.gif");
        private static final long serialVersionUID = 1L;

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if(value instanceof MarathonTestCase && ((MarathonTestCase)value).getFullName() != null) {
                value = ((MarathonTestCase)value).getFullName();
            }
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            ImageIcon folderIcon = new ImageIcon(ICON_FOLDER);
            if (lbl.getText().startsWith("+")) {
                lbl.setIcon(folderIcon);
                lbl.setText(lbl.getText().substring(1));
            } else if (value instanceof TestSuite) {
                lbl.setIcon(folderIcon);
            } else
                lbl.setIcon(new ImageIcon(ICON_FILE));
            return lbl;
        }
    }

    private final class SuiteListModel extends DefaultListModel {
        private static final long serialVersionUID = 1L;

        public SuiteListModel() {
            super();
        }

    }

    private HashMap<String, Object> dataMap;
    private JPanel comp;
    private SuiteListModel testsInSuiteModel;
    private JList testsInSuite;
    private JTextField txtSuiteName;
    private boolean dirty;
    private TestCreator testCreator;
    private TestSuitePanel testsPanel;
    private TestSuitePanel testSuitesPanel;
    private EventListenerList listeners;

    public SuiteEditor() {
        dataMap = new HashMap<String, Object>();
        initialise();
    }

    public void setStatusBar(IStatusBar statusBar) {
        // TODO Auto-generated method stub

    }

    public void startInserting() {
        // TODO Auto-generated method stub

    }

    public void stopInserting() {
        // TODO Auto-generated method stub

    }

    public void insertScript(String script) {
        // TODO Auto-generated method stub

    }

    public void addKeyBinding(String keyBinding, ActionListener action) {
        // TODO Auto-generated method stub

    }

    public void highlightLine(int line) {
        // TODO Auto-generated method stub

    }

    public boolean isEditable() {
        // TODO Auto-generated method stub
        return false;
    }

    public int getSelectionStart() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getSelectionEnd() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void undo() {
        // TODO Auto-generated method stub

    }

    public void redo() {
        // TODO Auto-generated method stub

    }

    public void cut() {
        // TODO Auto-generated method stub

    }

    public void copy() {
        // TODO Auto-generated method stub

    }

    public void paste() {
        // TODO Auto-generated method stub

    }

    public boolean canUndo() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean canRedo() {
        // TODO Auto-generated method stub
        return false;
    }

    public void clearUndo() {
        // TODO Auto-generated method stub

    }

    public void setDirty(boolean b) {
        this.dirty = b;
        fireContentChangeEvent();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void addCaretListener(CaretListener listener) {
        // TODO Auto-generated method stub

    }

    public void refresh() {
        // TODO Auto-generated method stub

    }

    public void addContentChangeListener(IContentChangeListener l) {
        listeners.add(IContentChangeListener.class, l);
    }

    public int getCaretLine() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void setCaretLine(int line) {
        // TODO Auto-generated method stub

    }

    public Component getComponent() {
        return comp;
    }

    private JPanel createPanel() {
        FormLayout layout = new FormLayout("fill:pref, 3dlu, fill:pref:grow");
        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints constraints = new CellConstraints();
        CellConstraints lblConstraints = new CellConstraints();

        JPanel nameLabelPanel = new JPanel();
        nameLabelPanel.add(new JLabel("Suite Name:"));
        nameLabelPanel.add(txtSuiteName);

        builder.appendRow("fill:pref");
        builder.addLabel("Suite Name:", lblConstraints.rc(1, 1), txtSuiteName, constraints.xy(3, 1));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        splitPane.setLeftComponent(createNotInSuitePanel());

        JSplitPane rightPart = new JSplitPane();
        JPanel btnPanel = createButtonsPanel();
        btnPanel.setBorder(BorderFactory.createRaisedBevelBorder());

        rightPart.setLeftComponent(btnPanel);
        rightPart.setRightComponent(new JScrollPane(testsInSuite));
        rightPart.setBorder(null);

        splitPane.setRightComponent(rightPart);

        rightPart.setDividerLocation(0.4);
        rightPart.setDividerSize(0);
        splitPane.setDividerLocation(0.2);

        builder.appendRow("3dlu");
        builder.appendRow("fill:pref:grow");

        builder.add(splitPane, constraints.xyw(1, 3, 3));

        rightPart.setResizeWeight(0);
        splitPane.setResizeWeight(0.4);
        return builder.getPanel();
    }

    private Component createNotInSuitePanel() {
        JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        testsPanel = new TestSuitePanel();
        testsPanel.showTestTree(testCreator.getTest("AllTests"));
        pane.setTopComponent(testsPanel);

        testSuitesPanel = new TestSuitePanel();
        testSuitesPanel.showTestTree(testCreator.getAllSuites());

        testSuitesPanel.getTree().setRootVisible(false);

        testsPanel.getTree().addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                if (testsPanel.getTree().getSelectionCount() > 0)
                    testSuitesPanel.getTree().setSelectionRow(-1);
            }
        });

        testSuitesPanel.getTree().addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                if (testSuitesPanel.getTree().getSelectionCount() > 0)
                    testsPanel.getTree().setSelectionRow(-1);
            }
        });

        pane.setBottomComponent(testSuitesPanel);
        pane.setDividerLocation(0.5);
        pane.setResizeWeight(0.5);

        pane.setBorder(null);

        return pane;
    }

    private void initialise() {
        txtSuiteName = new JTextField();
        listeners = new EventListenerList();
        txtSuiteName.getDocument().addDocumentListener(new DocumentListener() {

            public void removeUpdate(DocumentEvent arg0) {
                setDirty(true);
            }

            public void insertUpdate(DocumentEvent arg0) {
                setDirty(true);
            }

            public void changedUpdate(DocumentEvent arg0) {
                setDirty(true);
            }
        });

        testsInSuiteModel = new SuiteListModel();

        testsInSuite = new JList(testsInSuiteModel);
        testsInSuite.setCellRenderer(new SuiteListCellRenderer());

        try {
            testCreator = new TestCreator(true, null);
            testCreator.setIgnoreDDTSuites(true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (comp == null)
            comp = createPanel();
    }

    private JPanel createButtonsPanel() {
        JButton btnAdd = UIUtils.createAddButton();
        btnAdd.setText("Add");
        btnAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addToSuite();
            }
        });
        JButton btnRemove = UIUtils.createRemoveButton();
        btnRemove.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                removeFromSuite();
            }
        });

        JButton btnUp = UIUtils.createUpButton();
        JButton btnDown = UIUtils.createDownButton();

        btnUp.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                moveUp();
            }
        });

        btnDown.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                moveDown();
            }
        });

        JButton btnRefresh = UIUtils.createRefreshButtonWithText();
        btnRefresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testsPanel.showTestTree(testCreator.getTest("AllTests"));
                testSuitesPanel.showTestTree(testCreator.getAllSuites());
            }
        });
        ButtonStackBuilder builder = new ButtonStackBuilder();
        ArrayList<JButton> buttons = new ArrayList<JButton>();
        buttons.add(btnAdd);
        buttons.add(btnRemove);
        buttons.add(btnUp);
        buttons.add(btnDown);
        buttons.add(btnRefresh);
        JButton[] aButtons = new JButton[buttons.size()];
        buttons.toArray(aButtons);
        builder.addButtons(aButtons);
        return builder.getPanel();

    }

    protected void moveDown() {
        int[] selectedIndices = testsInSuite.getSelectedIndices();
        if (selectedIndices.length <= 0)
            return;
        int indicesLength = selectedIndices.length;
        int highestSelIndexToBeSet = selectedIndices[indicesLength - 1] == testsInSuiteModel.getSize() - 1 ? testsInSuiteModel
                .getSize() - 1 : selectedIndices[indicesLength - 1] + 1;

        ArrayList<Object> selectedObjects = new ArrayList<Object>();
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            selectedObjects.add(testsInSuiteModel.remove(selectedIndices[i]));
        }

        for (Object object : selectedObjects) {
            testsInSuiteModel.add(highestSelIndexToBeSet + 1 - selectedObjects.size(), object);
        }
        int[] newSelIndices = new int[selectedObjects.size()];
        for (int i = newSelIndices.length - 1; i >= 0; i--) {
            newSelIndices[i] = highestSelIndexToBeSet + 1 - selectedObjects.size() + i;
        }

        testsInSuite.setSelectedIndices(newSelIndices);

        setDirty(true);
    }

    protected void moveUp() {
        int[] selectedIndices = testsInSuite.getSelectedIndices();
        if (selectedIndices.length <= 0)
            return;
        ArrayList<Object> list = new ArrayList<Object>();
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            list.add(testsInSuiteModel.remove(selectedIndices[i]));
        }
        int listSize = list.size();
        int lowestSelIndexToBeSet = selectedIndices[0] == 0 ? 0 : selectedIndices[0] - 1;
        for (int i = listSize - 1; i >= 0; i--) {
            testsInSuiteModel.add(lowestSelIndexToBeSet - (i + 1 - listSize), list.get(i));
        }

        int[] newSelIndices = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            newSelIndices[i] = lowestSelIndexToBeSet++;
        }

        testsInSuite.setSelectedIndices(newSelIndices);
        setDirty(true);
    }

    protected void removeFromSuite() {
        int[] selectedIndices = testsInSuite.getSelectedIndices();
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            testsInSuiteModel.remove(selectedIndices[i]);
        }
        setDirty(true);
    }

    protected void addToSuite() {
        addSelectedTestsToSuite();
        addSelectedSuitesToSuite();
        setDirty(true);
    }

    private void addSelectedSuitesToSuite() {
        Test selectedTest = testSuitesPanel.getSelectedTest();
        if (selectedTest != null)
            if (selectedTest instanceof TestSuite) {
                TestSuite suite = (TestSuite) selectedTest;
                for (int i = 0; i < suite.testCount(); i++) {
                    Test suiteAt = suite.testAt(i);
                    if (((TestSuite) suiteAt).getName().equals(dataMap.get("filename"))) {
                        JOptionPane.showMessageDialog(null, "A suite cannot be added to self.");
                        continue;
                    }
                    testsInSuiteModel.addElement(suiteAt);
                }
            } else
                testsInSuiteModel.addElement(selectedTest);
    }

    private void addSelectedTestsToSuite() {
        Test selectedTest = testsPanel.getSelectedTest();
        if (selectedTest != null)

            if (selectedTest instanceof TestSuite) {
                addSuiteToSelectedList((TestSuite) selectedTest);
            } else
                testsInSuiteModel.addElement(selectedTest);
    }

    private void addSuiteToSelectedList(TestSuite suite) {
        for (int i = 0; i < suite.testCount(); i++) {
            Test testAt = suite.testAt(i);
            if (testAt instanceof TestSuite)
                addSuiteToSelectedList((TestSuite) testAt);
            else
                testsInSuiteModel.addElement(testAt);
        }
    }

    public void closeSearch() {
        // TODO Auto-generated method stub

    }

    public int find(String searchText, boolean bForward, boolean bAllLines, boolean bCaseSensitive, boolean bWrapSearch,
            boolean bWholeWord, boolean bRegex) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int replaceFind(String searchText, String replaceText, boolean bForward, boolean bAllLines, boolean bCaseSensitive,
            boolean bWrapSearch, boolean bWholeWord, boolean bRegex) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void replace(String searchText, String replaceText, boolean bForward, boolean bAllLines, boolean bCaseSensitive,
            boolean bWrapSearch, boolean bWholeWord, boolean bRegex) {
        // TODO Auto-generated method stub

    }

    public void replaceAll(String searchText, String replaceText, boolean bCaseSensitive, boolean bWholeWord, boolean bRegex) {
        // TODO Auto-generated method stub

    }

    public void find(int findPrev) {
        // TODO Auto-generated method stub

    }

    public void showSearchDialog(ISearchDialog dialog) {
        // TODO Auto-generated method stub

    }

    public void addGutterListener(IGutterListener provider) {
        // TODO Auto-generated method stub

    }

    public Object getData(String key) {
        return dataMap.get(key);
    }

    public void setData(String key, Object o) {
        dataMap.put(key, o);
    }

    public void setCaretPosition(int position) {
        // TODO Auto-generated method stub

    }

    public int getCaretPosition() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getText() {
        StringBuilder sbr = new StringBuilder();
        String txtName = txtSuiteName.getText();
        if (txtName.length() != 0) {
            sbr.append("#" + txtName + "\n");
        }
        int size = testsInSuiteModel.getSize();
        for (int i = 0; i < size; i++) {
            Object test = testsInSuiteModel.get(i);
            if (test instanceof TestSuite) {
                sbr.append("+");
                sbr.append(test.toString().substring(0, test.toString().length() - 6) + "\n");
            } else {
                if (test instanceof MarathonTestCase && ((MarathonTestCase) test).getFullName() != null)
                    sbr.append(((MarathonTestCase) test).getFullName() + "\n");
                else
                    sbr.append(test + "\n");
            }
        }
        return sbr.toString();
    }

    public void setText(String code) {
        try {
            addToListFromText(code);
            testsInSuite.validate();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void addToListFromText(String code) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(code));
        String s = "";
        while ((s = reader.readLine()) != null && s.length() != 0) {
            if (s.charAt(0) == '#') {
                setName(s.substring(1));
                continue;
            }
            testsInSuiteModel.addElement(s);
        }
    }

    private void setName(String name) {
        txtSuiteName.setText(name);
    }

    public void setMode(String string) {
        // TODO Auto-generated method stub

    }

    public void setEnabled(boolean b) {
        // TODO Auto-generated method stub

    }

    public int getLineOfOffset(int selectionStart) throws BadLocationException {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getLineStartOffset(int startLine) throws BadLocationException {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getLineEndOffset(int endLine) throws BadLocationException {
        // TODO Auto-generated method stub
        return 0;
    }

    public void setFocus() {
        // TODO Auto-generated method stub

    }

    public void setMenuItems(JMenuItem[] menuItems) {
        // TODO Auto-generated method stub

    }

    public void toggleInsertMode() {
        // TODO Auto-generated method stub

    }

    public void setEditable(boolean b) {
        // TODO Auto-generated method stub

    }

    private void fireContentChangeEvent() {
        IContentChangeListener[] la = listeners.getListeners(IContentChangeListener.class);
        for (final IContentChangeListener l : la) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    l.contentChanged();
                }
            });
        }
    }
}
