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
package net.sourceforge.marathon.junit.swingui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.runner.BaseTestRunner;
import junit.runner.TestRunListener;
import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.IConsole;
import net.sourceforge.marathon.display.FileEventHandler;
import net.sourceforge.marathon.display.OldSimpleAction;
import net.sourceforge.marathon.display.TextAreaOutput;
import net.sourceforge.marathon.junit.MarathonResultReporter;
import net.sourceforge.marathon.junit.MarathonTestCase;
import net.sourceforge.marathon.junit.TestCreator;
import net.sourceforge.marathon.junit.textui.HTMLOutputter;
import net.sourceforge.marathon.junit.textui.TestLinkXMLOutputter;
import net.sourceforge.marathon.junit.textui.TextOutputter;
import net.sourceforge.marathon.junit.textui.XMLOutputter;
import net.sourceforge.marathon.navigator.IFileEventListener;
import net.sourceforge.marathon.util.UIUtils;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.toolbars.VLToolBar;

import edu.stanford.ejalbert.BrowserLauncher;

public class TestRunner extends BaseTestRunner implements ITestRunContext, Dockable, IFileEventListener {
    
    private static final Logger logger = Logger.getLogger(TestRunner.class.getCanonicalName());
    
    private static final int GAP = 4;
    private static final Icon ICON_JUNIT = new ImageIcon(
            TextAreaOutput.class.getResource("/net/sourceforge/marathon/junit/swingui/icons/junit.gif"));

    private static final DockKey DOCK_KEY = new DockKey("JUnit", "JUnit", "Test navigator", ICON_JUNIT);

    private Thread runnerThread;
    private TestResult testResult;
    private ProgressBar progressBar;
    private DefaultListModel failures;
    private CounterPanel counterPanel;
    private int selectedFailure;
    private MarathonFailureDetailView failureView;
    private JTabbedPane testViewTab;
    private Vector<TestRunView> testRunViews = new Vector<TestRunView>();
    private EventListenerList testOpenListeners = new EventListenerList();

    private OldSimpleAction runAction = new OldSimpleAction("Run all tests", 'R', Icons.RUN, Icons.RUN_DISABLED) {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            runSuite();
        }
    };
    private OldSimpleAction runSelectedAction = new OldSimpleAction("Run selected test", 'e', Icons.RELAUNCH,
            Icons.RELAUNCH_DISABLED) {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            runSelected();
        }
    };
    private OldSimpleAction stopAction = new OldSimpleAction("Stop", 'S', Icons.STOP, Icons.STOP_DISABLED) {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            runSuite();
        }
    };
    private OldSimpleAction nextFailureAction = new OldSimpleAction("Next Failure", 'N', Icons.SELECT_NEXT,
            Icons.SELECT_NEXT_DISABLED) {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            gotoNextFailure();
        }
    };
    private OldSimpleAction prevFailureAction = new OldSimpleAction("Previous Failure", 'P', Icons.SELECT_PREV,
            Icons.SELECT_PREV_DISABLED) {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            gotoPrevFailure();
        }
    };
    private OldSimpleAction testViewAction = new OldSimpleAction("Test View", 'T', Icons.HIERARCHY) {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            resetTestView();
        }
    };
    private OldSimpleAction testReportViewAction = new OldSimpleAction("Test Report", 'R', Icons.REPORT, Icons.REPORT_DISABLED) {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            displayReport();
        }
    };
    private String suiteName;
    private JPanel panel;
    private MarathonResultReporter reporter;
    private boolean acceptChecklist;
    private File runReportDir;
    private File reportDir;
    private File resultReporterHTMLFile;
    private final IConsole console;
    private final FileEventHandler fileEventHandler;


    public TestRunner(IConsole console, FileEventHandler fileEventHandler) {
        this.console = console;
        this.fileEventHandler = fileEventHandler;
        reportDir = new File(new File(System.getProperty(Constants.PROP_PROJECT_DIR)), "TestReports");
        if (!reportDir.exists())
            if (!reportDir.mkdir()) {
                logger.warning("Unable to create report directory: " + reportDir + " - Marathon might not function properly");
            }
        panel = getPanel();
    }

    public void testFailed(final int status, final Test test, final Throwable t) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                switch (status) {
                case TestRunListener.STATUS_ERROR:
                    counterPanel.setErrorValue(testResult.errorCount());
                    appendFailure(test, t);
                    break;
                case TestRunListener.STATUS_FAILURE:
                    counterPanel.setFailureValue(testResult.failureCount());
                    appendFailure(test, t);
                    break;
                }
            }
        });
    }

    public void testEnded(String stringName) {
        synchUI();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (testResult != null) {
                    counterPanel.setRunValue(testResult.runCount());
                    progressBar.step(testResult.runCount(), testResult.wasSuccessful());
                }
            }
        });
    }

    public void addTestOpenListener(ITestListener l) {
        testOpenListeners.add(ITestListener.class, l);
    }

    private void appendFailure(Test test, Throwable t) {
        failures.addElement(new TestFailure(test, t));
    }

    private void revealFailure(Test test) {
        for (Enumeration<TestRunView> e = testRunViews.elements(); e.hasMoreElements();) {
            TestRunView v = e.nextElement();
            v.revealFailure(test);
        }
    }

    protected void aboutToStart(final Test testSuite) {
        for (Enumeration<TestRunView> e = testRunViews.elements(); e.hasMoreElements();) {
            TestRunView v = e.nextElement();
            v.aboutToStart(testSuite, testResult);
        }
    }

    protected void runFinished(final Test testSuite) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (Enumeration<TestRunView> e = testRunViews.elements(); e.hasMoreElements();) {
                    TestRunView v = e.nextElement();
                    v.runFinished(testSuite, testResult);
                }
            }
        });
        try {
            resultReporterHTMLFile = new File(runReportDir, "results.html");
            if (reporter != null)
                reporter.generateReport(new HTMLOutputter(), resultReporterHTMLFile.getCanonicalPath());
            File resultReporterXMLFile = new File(runReportDir, "results.xml");
            if (reporter != null)
                reporter.generateReport(new XMLOutputter(), resultReporterXMLFile.getCanonicalPath());
            File resultReporterTestLinkXMLFile = new File(runReportDir, "testlink-results.xml");
            if (reporter != null)
                reporter.generateReport(new TestLinkXMLOutputter(), resultReporterTestLinkXMLFile.getCanonicalPath());
            fileEventHandler.fireNewEvent(resultReporterHTMLFile, false);
            fileEventHandler.fireNewEvent(resultReporterXMLFile, false);
            fileEventHandler.fireNewEvent(resultReporterTestLinkXMLFile, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fireTestFinished();
    }

    protected CounterPanel createCounterPanel() {
        return new CounterPanel();
    }

    protected MarathonFailureDetailView createFailureDetailView() {
        return new MarathonFailureDetailView();
    }

    private VLToolBar createToolbar() {
        VLToolBar bar = new VLToolBar("JUnit");
        bar.add(nextFailureAction.getButton());
        bar.add(prevFailureAction.getButton());
        nextFailureAction.setEnabled(false);
        prevFailureAction.setEnabled(false);
        bar.add(testViewAction.getButton());
        bar.addSeparator();
        bar.add(runAction.getButton());
        bar.add(stopAction.getButton());
        stopAction.setEnabled(false);
        bar.add(runSelectedAction.getButton());
        runSelectedAction.setEnabled(false);
        bar.add(testReportViewAction.getButton());
        testReportViewAction.setEnabled(false);
        return bar;
    }

    public void resetTestView() {
        reset();
        for (Enumeration<TestRunView> e = testRunViews.elements(); e.hasMoreElements();) {
            TestRunView v = e.nextElement();
            v.reset(getTest(suiteName));
        }
    }

    private void displayReport() {
        if (resultReporterHTMLFile != null) {
            try {
                BrowserLauncher launcher = new BrowserLauncher();
                launcher.openURLinBrowser(resultReporterHTMLFile.toURI().toString());
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        displayTextReport();
    }

    private void displayTextReport() {
        File temp = null;
        try {
            temp = File.createTempFile("marathon", ".txt");
            temp.deleteOnExit();
            if (reporter != null)
                reporter.generateReport(new TextOutputter(), temp.getCanonicalPath());
            new ReportViewer(temp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ReportViewer extends JDialog {
        private static final long serialVersionUID = 1L;

        public ReportViewer(final File reportFile) {
            super((JFrame) (SwingUtilities.windowForComponent(testViewTab) instanceof JFrame ? SwingUtilities
                    .windowForComponent(testViewTab) : null));
            setTitle("Marathon Test Report");
            setModal(true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            JEditorPane editorPane = new JEditorPane("text/plain", "");
            editorPane.setEditable(false);
            try {
                editorPane.read(new FileReader(reportFile), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            JScrollPane pane = new JScrollPane(editorPane);
            pane.setBorder(Borders.DIALOG_BORDER);
            getContentPane().add(pane);
            JButton closeButton = UIUtils.createCloseButton();
            closeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            JButton saveButton = UIUtils.createSaveButton();
            saveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    int chosenOption = fileChooser.showSaveDialog(ReportViewer.this);
                    if (chosenOption == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        try {
                            copy(new FileInputStream(reportFile), new FileOutputStream(selectedFile));
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            });
            JPanel buttonPanel = ButtonBarFactory.buildRightAlignedBar(saveButton, closeButton);
            getContentPane().add(buttonPanel, BorderLayout.SOUTH);
            setSize(800, 600);
            setLocationRelativeTo(SwingUtilities.windowForComponent(testViewTab));
            setVisible(true);
        }

        void copy(InputStream in, OutputStream out) throws IOException {
            try {
                byte[] buffer = new byte[4096];
                int nrOfBytes = -1;
                while ((nrOfBytes = in.read(buffer)) != -1) {
                    out.write(buffer, 0, nrOfBytes);
                }
                out.flush();
            } finally {
                try {
                    in.close();
                    out.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    protected void gotoNextFailure() {
        selectedFailure++;
        revealFailure(((TestFailure) failures.elementAt(selectedFailure)).failedTest());
        if (selectedFailure == failures.getSize() - 1)
            setButtonState(nextFailureAction, false);
        if (selectedFailure != 0)
            setButtonState(prevFailureAction, true);
    }

    protected void gotoPrevFailure() {
        selectedFailure--;
        revealFailure(((TestFailure) failures.elementAt(selectedFailure)).failedTest());
        if (selectedFailure != failures.getSize() - 1)
            setButtonState(nextFailureAction, true);
        if (selectedFailure == 0)
            setButtonState(prevFailureAction, false);
    }

    protected JTabbedPane createTestRunViews() {
        JTabbedPane pane = new JTabbedPane(SwingConstants.TOP);
        pane.putClientProperty("jgoodies.noContentBorder", Boolean.TRUE);
        pane.putClientProperty("jgoodies.embeddedTabs", Boolean.TRUE);
        FailureRunView lv = new FailureRunView(this);
        testRunViews.addElement(lv);
        lv.addTab(pane);
        TestHierarchyRunView tv = new TestHierarchyRunView(this, getTest(suiteName));
        testRunViews.addElement(tv);
        tv.addTab(pane);
        pane.setSelectedIndex(1);
        pane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                testViewChanged();
            }
        });
        return pane;
    }

    public void testViewChanged() {
        TestRunView view = (TestRunView) testRunViews.elementAt(testViewTab.getSelectedIndex());
        view.activate();
    }

    protected TestResult createTestResult() {
        runReportDir = new File(reportDir, createTestReportDirName());
        if (runReportDir.mkdir()) {
            try {
                System.setProperty(Constants.PROP_REPORT_DIR, runReportDir.getCanonicalPath());
                System.setProperty(Constants.PROP_IMAGE_CAPTURE_DIR, runReportDir.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            logger.warning("Unable to create folder: " + runReportDir + " - Ignoring report option");
        }
        return new TestResult();
    }

    private String createTestReportDirName() {
        return "ju-" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
    }

    public Component getComponent() {
        return panel;
    }

    private JPanel getPanel() {
        suiteName = "AllTests";
        progressBar = new ProgressBar();
        counterPanel = createCounterPanel();
        failures = new DefaultListModel();
        testViewTab = createTestRunViews();
        failureView = createFailureDetailView();
        VLToolBar toolBar = createToolbar();
        JScrollPane tracePane = new JScrollPane(failureView.getComponent());
        JLabel traceLabel = new JLabel("Trace View", Icons.TRACE, SwingConstants.LEFT);
        JPanel junitPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = getGBC(0, GridBagConstraints.NONE);
        gbc.anchor = GridBagConstraints.EAST;
        junitPanel.add(toolBar, gbc);
        junitPanel.add(progressBar, getGBC(1, GridBagConstraints.HORIZONTAL));
        junitPanel.add(counterPanel, getGBC(2, GridBagConstraints.NONE));
        junitPanel.add(testViewTab, getGBC(3, GridBagConstraints.BOTH));
        GridBagConstraints traceLabelGBC = getGBC(4, GridBagConstraints.NONE);
        traceLabelGBC.anchor = GridBagConstraints.WEST;
        traceLabelGBC.insets = new Insets(10, 10, 10, 10);
        junitPanel.add(traceLabel, traceLabelGBC);
        junitPanel.add(tracePane, getGBC(5, GridBagConstraints.BOTH));
        return junitPanel;
    }

    private GridBagConstraints getGBC(int y, int fill) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.weightx = 100;
        c.fill = fill;
        if (fill == GridBagConstraints.BOTH)
            c.weighty = 100;
        c.insets = new Insets(y == 0 ? 10 : 0, 10, GAP, GAP);
        return c;
    }

    public ListModel getFailures() {
        return failures;
    }

    private void runSelected() {
        console.clear();
        TestRunView view = (TestRunView) testRunViews.elementAt(testViewTab.getSelectedIndex());
        Test selectedTest = view.getSelectedTest();
        if (selectedTest != null)
            runTest(selectedTest);
    }

    protected void reset() {
        counterPanel.reset();
        progressBar.reset();
        runSelectedAction.setEnabled(false);
        nextFailureAction.setEnabled(false);
        prevFailureAction.setEnabled(false);
        testReportViewAction.setEnabled(false);
        failureView.clear();
        failures.clear();
    }

    protected synchronized void runFailed(String message) {
        runAction.setEnabled(true);
        stopAction.setEnabled(false);
        testViewAction.setEnabled(true);
        testReportViewAction.setEnabled(true);
        runnerThread = null;
    }

    synchronized public void runSuite() {
        console.clear();
        if (runnerThread != null) {
            testResult.stop();
            runnerThread.interrupt();
        } else {
            reset();
            final String suiteName = this.suiteName;
            final Test testSuite = getTest(suiteName);
            if (testSuite != null) {
                doRunTest(testSuite);
            }
        }
    }

    synchronized protected void runTest(final Test testSuite) {
        if (runnerThread != null) {
            testResult.stop();
        } else {
            reset();
            if (testSuite != null) {
                doRunTest(testSuite);
            }
        }
    }

    private void doRunTest(final Test testSuite) {
        setButtonState(runAction, false);
        setButtonState(stopAction, true);
        setButtonState(testViewAction, false);
        setButtonState(testReportViewAction, false);
        reporter = new MarathonResultReporter(testSuite);
        runnerThread = new Thread("TestRunner-Thread") {
            public void run() {
                try {
                    TestRunner.this.start(testSuite);
                    testSuite.run(testResult);
                    runFinished(testSuite);
                } finally {
                    // always return control to UI
                    setButtonState(runAction, true);
                    setButtonState(stopAction, false);
                    setButtonState(testViewAction, true);
                    setButtonState(testReportViewAction, true);
                    if (failures.size() > 0) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                revealFailure(((TestFailure) failures.elementAt(0)).failedTest());
                            }
                        });
                        selectedFailure = 0;
                        if (failures.size() > 1) {
                            setButtonState(nextFailureAction, true);
                        }
                    }
                    MarathonTestCase.reset();
                    runnerThread = null;
                    System.gc();
                }
            }
        };
        // make sure that the test result is created before we start the
        // test runner thread so that listeners can register for it.
        testResult = createTestResult();
        testResult.addListener(TestRunner.this);
        testResult.addListener(reporter);
        aboutToStart(testSuite);
        runnerThread.start();
    }

    private void setButtonState(final OldSimpleAction action, final boolean state) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                action.setEnabled(state);
            }
        });
    }

    public void handleTestSelected(Test test) {
        runSelectedAction.setEnabled(test != null);
        showFailureDetail(test);
    }

    private void showFailureDetail(Test test) {
        if (test != null) {
            ListModel failures = getFailures();
            for (int i = 0; i < failures.getSize(); i++) {
                TestFailure failure = (TestFailure) failures.getElementAt(i);
                if (failure.failedTest() == test) {
                    failureView.showFailure(failure);
                    return;
                }
            }
        }
        failureView.clear();
    }

    private void start(final Test test) {
        fireTestStarted();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int total = test.countTestCases();
                progressBar.start(total);
                counterPanel.setTotal(total);
            }
        });
    }

    /**
     * Wait until all the events are processed in the event thread
     */
    private void synchUI() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                }
            });
        } catch (Exception e) {
        }
    }

    public void testStarted(String testName) {
    }

    public Test getTest(String suiteClassName) {
        try {
            return new TestCreator(acceptChecklist, console).getTest(suiteClassName);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void fireTestFinished() {
        ITestListener[] listeners = testOpenListeners.getListeners(ITestListener.class);
        for (ITestListener listener : listeners) {
            listener.testFinished();
        }
    }

    private void fireTestStarted() {
        ITestListener[] listeners = testOpenListeners.getListeners(ITestListener.class);
        for (ITestListener listener : listeners) {
            listener.testStarted();
        }
    }

    public void handleTestOpened(Test test) {
        ITestListener[] listeners = testOpenListeners.getListeners(ITestListener.class);
        for (ITestListener listener : listeners) {
            listener.openTest(test);
        }
    }

    public MarathonFailureDetailView getFailureView() {
        return failureView;
    }

    public void setAcceptChecklist(boolean selected) {
        acceptChecklist = selected;
        resetTestView();
    }

    public DockKey getDockKey() {
        return DOCK_KEY;
    }

    public void fileRenamed(File from, File to) {
        if (isTestFile(from) || isTestFile(to))
            resetTestView();
    }

    private boolean isTestFile(File file) {
        if (file.getPath().startsWith(System.getProperty(Constants.PROP_TEST_DIR)))
            return true;
        return false;
    }

    public void fileDeleted(File file) {
        if (isTestFile(file))
            resetTestView();
    }

    public void fileCopied(File from, File to) {
        if (isTestFile(from) || isTestFile(to))
            resetTestView();
    }

    public void fileMoved(File from, File to) {
        if (isTestFile(from) || isTestFile(to))
            resetTestView();
    }

    public void fileCreated(File file, boolean openInEditor) {
        if (isTestFile(file))
            resetTestView();
    }

    public void fileUpdated(File file) {
        if (isTestFile(file))
            resetTestView();
    }
}
