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

package net.sourceforge.marathon.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.Main;
import net.sourceforge.marathon.api.IConsole;
import net.sourceforge.marathon.api.ILogger;
import net.sourceforge.marathon.api.IPlaybackListener;
import net.sourceforge.marathon.api.IRuntimeLauncherModel;
import net.sourceforge.marathon.api.IScriptModelClientPart;
import net.sourceforge.marathon.api.IScriptModelClientPart.SCRIPT_FILE_TYPE;
import net.sourceforge.marathon.api.LogRecord;
import net.sourceforge.marathon.api.MarathonRuntimeException;
import net.sourceforge.marathon.api.PlaybackResult;
import net.sourceforge.marathon.api.RuntimeLogger;
import net.sourceforge.marathon.api.SourceLine;
import net.sourceforge.marathon.api.module.Module;
import net.sourceforge.marathon.checklist.CheckList;
import net.sourceforge.marathon.checklist.CheckListDialog;
import net.sourceforge.marathon.checklist.CheckListForm;
import net.sourceforge.marathon.checklist.CheckListForm.Mode;
import net.sourceforge.marathon.checklist.MarathonCheckList;
import net.sourceforge.marathon.display.ResultPane.IResultPaneSelectionListener;
import net.sourceforge.marathon.editor.IContentChangeListener;
import net.sourceforge.marathon.editor.IEditor;
import net.sourceforge.marathon.editor.IEditor.IGutterListener;
import net.sourceforge.marathon.editor.IEditorProvider;
import net.sourceforge.marathon.editor.IEditorProvider.EditorType;
import net.sourceforge.marathon.junit.MarathonAssertion;
import net.sourceforge.marathon.junit.MarathonResultReporter;
import net.sourceforge.marathon.junit.MarathonTestCase;
import net.sourceforge.marathon.junit.StdOutConsole;
import net.sourceforge.marathon.junit.swingui.IStackMessageProcessor;
import net.sourceforge.marathon.junit.swingui.ITestListener;
import net.sourceforge.marathon.junit.swingui.TestRunner;
import net.sourceforge.marathon.junit.textui.HTMLOutputter;
import net.sourceforge.marathon.junit.textui.TestLinkXMLOutputter;
import net.sourceforge.marathon.junit.textui.XMLOutputter;
import net.sourceforge.marathon.mpf.MPFConfigurationUI;
import net.sourceforge.marathon.navigator.IFileEventListener;
import net.sourceforge.marathon.navigator.Navigator;
import net.sourceforge.marathon.navigator.NavigatorFileAction;
import net.sourceforge.marathon.screencapture.AnnotateScreenCapture;
import net.sourceforge.marathon.util.AbstractSimpleAction;
import net.sourceforge.marathon.util.ExceptionUtil;
import net.sourceforge.marathon.util.FileHandler;
import net.sourceforge.marathon.util.INameValidateChecker;
import net.sourceforge.marathon.util.Indent;
import net.sourceforge.marathon.util.LauncherModelHelper;
import net.sourceforge.marathon.util.MPFUtils;
import net.sourceforge.marathon.util.OSUtils;
import net.sourceforge.marathon.util.PropertyEditor;
import net.sourceforge.marathon.util.UIUtils;
import net.sourceforge.marathon.util.osx.IOSXApplicationListener;

import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.vlsolutions.swing.docking.DockGroup;
import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockableActionCustomizer;
import com.vlsolutions.swing.docking.DockableResolver;
import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.DockingConstants;
import com.vlsolutions.swing.docking.DockingDesktop;
import com.vlsolutions.swing.docking.DockingUtilities;
import com.vlsolutions.swing.docking.TabbedContainerActions;
import com.vlsolutions.swing.docking.TabbedDockableContainer;
import com.vlsolutions.swing.docking.event.DockableSelectionEvent;
import com.vlsolutions.swing.docking.event.DockableSelectionListener;
import com.vlsolutions.swing.docking.event.DockableStateChangeEvent;
import com.vlsolutions.swing.docking.event.DockableStateChangeListener;
import com.vlsolutions.swing.docking.event.DockableStateWillChangeEvent;
import com.vlsolutions.swing.docking.event.DockableStateWillChangeListener;
import com.vlsolutions.swing.docking.ui.DockingUISettings;
import com.vlsolutions.swing.toolbars.ToolBarConstraints;
import com.vlsolutions.swing.toolbars.ToolBarContainer;
import com.vlsolutions.swing.toolbars.ToolBarPanel;
import com.vlsolutions.swing.toolbars.VLToolBar;

import edu.stanford.ejalbert.BrowserLauncher;

/**
 * DisplayWindow provides the main user interface for Marathon from which the
 * user selects various options for using Marathon.
 */
public class DisplayWindow extends JFrame implements IOSXApplicationListener, PreferenceChangeListener, INameValidateChecker {

    private static final String EOL = System.getProperty("line.separator");

    private static final Logger logger = Logger.getLogger(DisplayWindow.class.getCanonicalName());

    private class NavigatorListener implements IFileEventListener {
        public void fileRenamed(File from, File to) {
            EditorDockable dockable = findEditorDockable(from);
            if (dockable != null) {
                FileHandler fileHandler = (FileHandler) dockable.getEditor().getData("filehandler");
                try {
                    fileHandler.readFile(to);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dockable.getEditor().setData("filename", fileHandler.getCurrentFile().getName());
                dockable.updateKey();
                updateDockName(dockable.getEditor());
            }
        }

        public void fileDeleted(File file) {
            EditorDockable dockable = findEditorDockable(file);
            if (dockable != null) {
                dockable.getEditor().setDirty(false);
                workspace.close(dockable);
            }
            removeModDirFromProjFile(file.getAbsolutePath());
        }

        public void fileCopied(File from, File to) {
        }

        public void fileMoved(File from, File to) {
            fileRenamed(from, to);
        }

        public void fileCreated(File file, boolean openInEditor) {
            if (file.isFile()) {
                navigator.refresh(new File[] { file });
                if (openInEditor)
                    openFile(file);
            }
        }

        public void fileUpdated(File file) {
            String selectedFileName = file.getAbsolutePath();
            navigator.refresh(new File[] { new File(selectedFileName) });
            EditorDockable dockable = findEditorDockable(new File(selectedFileName));
            if (dockable != null) {
                FileHandler fileHandler = (FileHandler) dockable.getEditor().getData("filehandler");
                try {
                    String script = fileHandler.readFile(new File(selectedFileName));
                    dockable.getEditor().setText(script);
                    dockable.getEditor().setDirty(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private NavigatorListener navigatorListener;

    private class CaretListenerImpl implements CaretListener {
        public void caretUpdate(CaretEvent e) {
            updateEditActions();
        }
    }

    private CaretListenerImpl caretListener = new CaretListenerImpl();

    private class DockingListener implements DockableSelectionListener, DockableStateWillChangeListener,
            DockableStateChangeListener {

        public void selectionChanged(DockableSelectionEvent e) {
            Dockable selectedDockable = e.getSelectedDockable();
            if (selectedDockable instanceof EditorDockable) {
                setCurrentEditorDockable((EditorDockable) selectedDockable);
            }
        }

        public void dockableStateWillChange(DockableStateWillChangeEvent event) {
            if (resetWorkspaceOperation)
                return;
            DockableState dockableState = event.getFutureState();
            Dockable dockable = dockableState.getDockable();
            if (dockableState.isClosed() && !canCloseComponent(dockable))
                event.cancel();
            else {
                if (dockable instanceof EditorDockable) {
                    lastClosedEditorDockableContainer = DockingUtilities.findTabbedDockableContainer(dockable);
                }
            }
        }

        public void dockableStateChanged(DockableStateChangeEvent event) {
            DockableState dockableState = event.getNewState();
            Dockable dockable = dockableState.getDockable();
            if (dockableState.isClosed() && dockable instanceof EditorDockable) {
                workspace.unregisterDockable(dockable);
                Dockable selectedDockable = null;
                if (lastClosedEditorDockableContainer != null) {
                    selectedDockable = lastClosedEditorDockableContainer.getSelectedDockable();
                    lastClosedEditorDockableContainer = null;
                    if (selectedDockable == null) {
                        selectedDockable = workspace.getSelectedDockable();
                        if (!(selectedDockable instanceof EditorDockable)) {
                            selectedDockable = null;
                        }
                    }
                }
                setCurrentEditorDockable((EditorDockable) selectedDockable);
            }
            if (dockableState.isMaximized())
                maximizedDockable = dockable;
        }

    }

    private DockingListener dockingListener = new DockingListener();

    private class ContentChangeListener implements IContentChangeListener {
        public void contentChanged() {
            updateView();
        }

    }

    private ContentChangeListener contentChangeListener = new ContentChangeListener();

    private class GutterListener implements IGutterListener {
        public ImageIcon getIconAtLine(int line) {
            if (isBreakPointAtLine(line))
                return DisplayWindow.BREAKPOINT;
            return null;
        }

        public void gutterDoubleClickedAt(int caretLine) {
            toggleBreakPoint(caretLine);
        }

    }

    private GutterListener gutterListener = new GutterListener();

    private class ResultPaneSelectionListener implements IResultPaneSelectionListener {
        public void resultSelected(SourceLine line) {
            goToFile(line.fileName, line.lineNumber - 1);
        }

    }

    ResultPaneSelectionListener resultPaneSelectionListener = new ResultPaneSelectionListener();

    private class ScriptConsoleListener implements IScriptConsoleListener {
        public String evaluateScript(String command) {
            return display.evaluateScript(command);
        }

        public void sessionClosed() {
            closeScriptConsole();
            setState();
            if (state.isRecordingPaused()) {
                display.resume();
            }
        }

    }

    private ScriptConsoleListener scriptConsoleListener = new ScriptConsoleListener();

    private class TestListener implements ITestListener {
        public void openTest(Test suite) {
            Test test;
            if (suite instanceof TestSuite) {
                test = ((TestSuite) suite).testAt(0);
                if (test != null && test instanceof MarathonTestCase) {
                    openFile(((MarathonTestCase) test).getFile());
                }
            }
        }

        public void testFinished() {
            navigator.refresh();
        }

        public void testStarted() {
        }
    }

    private TestListener testListener = new TestListener();

    private class StackMessageProcessor implements IStackMessageProcessor {
        public void processMessage(String msg) {
            String[] elements = null;
            elements = scriptModel.parseMessage(msg);
            if (elements == null || elements.length == 0)
                return;
            String projectDir = System.getProperty(Constants.PROP_PROJECT_DIR);
            File file = new File(elements[0]);
            if (!file.isAbsolute())
                file = new File(projectDir, elements[0]);
            openFile(file);
            editor.setCaretLine(Integer.parseInt(elements[1]) - 1);
        }

    }

    private StackMessageProcessor stackMessageProcessor = new StackMessageProcessor();

    public class DisplayView implements IDisplayView {

        public void setError(Throwable exception, String message) {
            RuntimeLogger.getRuntimeLogger().error("Marathon", exception.getMessage(), ExceptionUtil.getTrace(exception));
            if (exception instanceof MarathonRuntimeException) {
                if (!"true".equals(System.getProperty("marathon.unittests")))
                    JOptionPane.showMessageDialog(DisplayWindow.this, "Application Under Test Aborted!!", "Error",
                            JOptionPane.ERROR_MESSAGE);
            } else {
                if (!"true".equals(System.getProperty("marathon.unittests")))
                    JOptionPane.showMessageDialog(DisplayWindow.this, message, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        public void setState(final State newState) {
            if (EventQueue.isDispatchThread())
                _setState(newState);
            else {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            _setState(newState);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        public void _setState(State newState) {
            State oldState = state;
            state = newState;
            playAction.setEnabled((state.isStoppedWithAppClosed() || state.isStoppedWithAppOpen()) && isTestFile());
            debugAction.setEnabled((state.isStoppedWithAppClosed() || state.isStoppedWithAppOpen()) && isTestFile());
            slowPlayAction.setEnabled((state.isStoppedWithAppClosed() || state.isStoppedWithAppOpen()) && isTestFile());
            recordAction.setEnabled(state.isStopped() && isProjectFile());
            etAction.setEnabled(state.isStopped());
            toggleBreakpointAction.setEnabled(isProjectFile());
            clearAllBreakpointsAction.setEnabled(breakpoints != null && breakpoints.size() > 0);
            newTestcaseAction.setEnabled(state.isStopped());
            newModuleAction.setEnabled(state.isStopped());
            newFixtureAction.setEnabled(state.isStopped());
            insertScriptAction.setEnabled(state.isRecording()
                    && (getModuleFunctions() != null && getModuleFunctions().getChildren() != null && getModuleFunctions()
                            .getChildren().size() > 0));
            insertChecklistAction.setEnabled(state.isRecording());
            pauseAction.setEnabled(state.isRecording());
            resumeRecordingAction.setEnabled(state.isRecordingPaused());
            resumePlayingAction.setEnabled(state.isPlayingPaused());
            stopAction.setEnabled(!state.isStopped() && (state.isRecording() || state.isPlaying() || state.isPlayingPaused()));
            saveAction.setEnabled(state.isStopped() && editor != null && editor.isDirty());
            saveAsAction.setEnabled(state.isStopped() && editor != null && editor.getComponent().isEnabled());
            saveAllAction.setEnabled(state.isStopped() && nDirty() > 0);
            searchAction.setEnabled(state.isStopped() && editor != null && editor.getComponent().isEnabled());
            openApplicationAction.setEnabled(state.isStoppedWithAppClosed() && isProjectFile());
            closeApplicationAction.setEnabled(state.isStoppedWithAppOpen());
            // update msg on status bar
            statusPanel.setApplicationState(state.toString());
            if (oldState.isRecording() && !state.isRecording() && scriptConsole == null)
                endController();
            if (!oldState.isRecording() && state.isRecording())
                startController();
            stepIntoAction.setEnabled(state.isPlayingPaused());
            stepOverAction.setEnabled(state.isPlayingPaused());
            stepReturnAction.setEnabled(state.isPlayingPaused());
            playerConsoleAction.setEnabled(state.isPlayingPaused() && scriptConsole == null);
            recorderConsoleAction.setEnabled((state.isRecording() || state.isRecordingPaused()) && scriptConsole == null);
            showReportAction.setEnabled(resultReporterHTMLFile != null);
        }

        public IStdOut getOutputPane() {
            if (scriptConsole != null)
                return scriptConsole;
            return outputPane;
        }

        public void setResult(PlaybackResult result) {
        }

        public int trackProgress(final SourceLine line, int type) {
            if (scriptConsole != null)
                return IPlaybackListener.CONTINUE;
            if (getFilePath().equals(line.fileName)) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        editor.highlightLine(line.lineNumber - 1);
                    }
                });
            }
            if (debugging == false)
                return IPlaybackListener.CONTINUE;
            callStack.update(type, line);
            BreakPoint bp = new BreakPoint(line.fileName, line.lineNumber - 1);
            if (type == Display.LINE_REACHED
                    && (stepIntoActive || breakpoints.contains(bp) || (breakStackDepth != -1 && callStack.getStackDepth() <= breakStackDepth))) {
                if (!getFilePath().equals(line.fileName)) {
                    try {
                        File file = new File(line.fileName);
                        if (file.exists()) {
                            SwingUtilities.invokeAndWait(new Runnable() {
                                public void run() {
                                    goToFile(line.fileName, line.lineNumber - 1);
                                }
                            });
                        } else
                            return IPlaybackListener.CONTINUE;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
                stepIntoActive = false;
                breakStackDepth = -1;
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            display.pausePlay();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                return IPlaybackListener.PAUSE;
            }
            return IPlaybackListener.CONTINUE;
        }

        public String getScript() {
            return editor.getText();
        }

        public String getFilePath() {
            if (editor == null)
                return null;
            if (getFileHandler(editor).getCurrentFile() == null)
                return (String) editor.getData("filename");
            try {
                return getFileHandler(editor).getCurrentFile().getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        public void insertScript(String script) {
            if (controller.isVisible())
                controller.insertScript(script);
            editor.insertScript(script);
        }

        public void trackProgress() {
            editor.highlightLine(-1);
        }

        public void startInserting() {
            editor.startInserting();
        }

        public void stopInserting() {
            editor.stopInserting();
            if (exploratoryTest) {
                displayView.endTest(null);
                displayView.endTestRun();
                File file = save();
                if (file != null)
                    navigator.makeVisible(file);
                exploratoryTest = false;
            }
            if (importStatements != null && importStatements.size() > 0) {
                String text = scriptModel.updateScriptWithImports(editor.getText(), importStatements);
                editor.setText(text);
                importStatements = null;
            }
        }

        public boolean isDebugging() {
            return debugging;
        }

        public int acceptChecklist(final String fileName) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    DisplayWindow.this.setState(JFrame.ICONIFIED);
                    fillUpChecklist(fileName);
                    DisplayWindow.this.setState(JFrame.NORMAL);
                    display.resume();
                }

            });
            return 0;
        }

        public int showChecklist(final String fileName) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    final File file = new File(reportDir, fileName);
                    final CheckList checklist;
                    try {
                        checklist = CheckList.read(new FileInputStream(file));
                        CheckListForm checklistForm = new CheckListForm(checklist, Mode.DISPLAY);
                        final CheckListDialog dialog = new CheckListDialog((JFrame) null, checklistForm);

                        JButton screenCapture = null;
                        if (checklist.getCaptureFile() != null) {
                            screenCapture = UIUtils.createScreenCaptureButton();

                            screenCapture.addActionListener(new ActionListener() {
                                File captureFile = new File(file.getParent(), checklist.getCaptureFile());

                                public void actionPerformed(ActionEvent e) {
                                    try {
                                        AnnotateScreenCapture annotate = new AnnotateScreenCapture(captureFile, false);
                                        annotate.showDialog();
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            });
                        }
                        JButton doneButton = UIUtils.createDoneButton();
                        doneButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                dialog.dispose();
                            }
                        });
                        if (screenCapture != null)
                            dialog.setActionButtons(new JButton[] { screenCapture, doneButton });
                        else
                            dialog.setActionButtons(new JButton[] { doneButton });
                        dialog.setVisible(true);
                    } catch (Exception e1) {
                        JOptionPane.showMessageDialog(null, "Unable to read the checklist file");
                    }
                    display.resume();
                }
            });
            return IPlaybackListener.PAUSE;
        }

        public void insertChecklistAction(final String name) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    insertChecklist(name);
                }
            });
        }

        private boolean needReports() {
            return exploratoryTest || generateReportsMenuItem.isSelected();
        }

        public void endTest(final PlaybackResult result) {
            if (!exploratoryTest) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        resultPane.addResult(result);
                    }
                });
            }
            if (!needReports())
                return;
            if (!exploratoryTest && result.hasFailure()) {
                MarathonAssertion assertion = new MarathonAssertion(result.failures(), getFilePath());
                resultReporter.addFailure(testCase, assertion);
            }
            resultReporter.endTest(testCase);
            addScreenCaptures();
        }

        private void addScreenCaptures() {
            String dirName = System.getProperty(Constants.PROP_IMAGE_CAPTURE_DIR);
            if (dirName != null) {
                File dir = new File(dirName);
                String testPath = dirName + File.separator + testCase.getName();
                File[] files = dir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.matches("error[0-9]*.png");
                    }
                });
                for (int i = 0; i < files.length; i++) {
                    String name = files[i].getName();
                    File newFile = new File(testPath + "-" + name);
                    if (files[i].renameTo(newFile))
                        testCase.addScreenCapture(newFile);
                    else {
                        logger.warning("Unable to rename file: " + files[i] + " to " + newFile);
                    }
                }
            }
        }

        public void endTestRun() {
            // Disable slowplay if set
            System.setProperty(Constants.PROP_RUNTIME_DELAY, "");
            if (!needReports())
                return;
            try {
                resultReporterHTMLFile = new File(runReportDir, "results.html");
				resultReporter.generateReport(new HTMLOutputter(),
						resultReporterHTMLFile.getCanonicalPath());
				File resultReporterXMLFile = new File(runReportDir,
						"results.xml");
				resultReporter.generateReport(new XMLOutputter(),
						resultReporterXMLFile.getCanonicalPath());
                File resultReporterTestLinkXMLFile = new File(runReportDir,
						"testlink-results.xml");
                resultReporter.generateReport(new TestLinkXMLOutputter(),
						resultReporterTestLinkXMLFile.getCanonicalPath());

                if (exploratoryTest) {
                    ArrayList<CheckList> checklists = testCase.getChecklists();
                    for (CheckList checkList : checklists) {
                        File dataFile = checkList.xgetDataFile();
                        if (dataFile != null)
                            checkList.save(new FileOutputStream(dataFile));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            navigator.refresh(new File[] { reportDir });
            DisplayWindow.this.setState();
        }

        public void startTest() {
            if (!needReports())
                return;
            testCase = new MarathonTestCase(new File(getFilePath()), false, new StdOutConsole()) {
                String t_suffix = display.getDDTestRunner() == null ? "" : display.getDDTestRunner().getName();
                String name = exploratoryTest ? runReportDir.getName() : super.getName() + t_suffix;

                public String getName() {
                    return name;
                };
            };
            testSuite.addTest(testCase);

            resultReporter.startTest(testCase);
            resultReporterHTMLFile = null;
        }

        public void startTestRun() {
            if (!needReports())
                return;
            runReportDir = new File(reportDir, createTestReportDirName());
            if (runReportDir.mkdir()) {
                try {
                    System.setProperty(Constants.PROP_REPORT_DIR, runReportDir.getCanonicalPath());
                    System.setProperty(Constants.PROP_IMAGE_CAPTURE_DIR, runReportDir.getCanonicalPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                logger.warning("Unable to create report directory: " + runReportDir + " - Ignoring report option");
            }
            testSuite = new TestSuite("Marathon Test");
            resultReporter = new MarathonResultReporter(testSuite);
        }

        public void addImport(String ims) {
            importStatements.add(ims);
        }

        public void updateOMapFile() {
            File omapFile = new File(System.getProperty(Constants.PROP_PROJECT_DIR), System.getProperty(Constants.PROP_OMAP_FILE,
                    Constants.FILE_OMAP));
            fileUpdated(omapFile);
        }

        public LogView getLogView() {
            return logView;
        }

    }

    public DisplayView displayView = new DisplayView();

    /**
     * Added so that the compiler does not give warnings.
     */
    private static final long serialVersionUID = 1L;

    private static final Icon EMPTY_ICON = new ImageIcon(DisplayWindow.class.getResource("icons/enabled/empty.gif"));

    private static final String MODULE = "Marathon";

    @Inject private Display display;
    @Inject private BrowserLauncher browserLauncher;
    @Inject private IScriptModelClientPart scriptModel;
    @Inject private FixtureSelector fixtureSelector;
    @Inject private TextAreaOutput outputPane;
    @Inject private ResultPane resultPane;
    @Inject private LogView logView;
    @Inject private StatusBar statusPanel;
    @Inject private CallStack callStack;
    @Inject private IEditorProvider editorProvider;
    @Inject(optional = true) private IActionProvider actionProvider;
    @Inject(optional = true) private AboutDialog aboutDialog;

    /**
     * Editor panel
     */
    private IEditor editor;
    /**
     * Line number dialog to accept a line number
     */
    private LineNumberDialog lineNumberDialog = new LineNumberDialog(this, "Goto");
    /**
     * Default fixture to be used for new test cases
     */
    private String fixture;
    /**
     * Is current recording in raw mode?
     */
    private boolean isRawRecording = false;
    private boolean debugging = false;
    protected boolean exploratoryTest = false;

    private List<BreakPoint> breakpoints;
    private MarathonTestCase testCase;
    private File reportDir;
    private File runReportDir;

    private JButton rawRecordButton;

    private static final class FnFDockable implements Dockable {
        private DockKey dockKey;
        private Component c = new JTextField();

        public FnFDockable(String keyName) {
            dockKey = new DockKey(keyName);
        }

        public DockKey getDockKey() {
            return dockKey;
        }

        public Component getComponent() {
            return c;
        }
    }

    public static final class EditorDockable implements Dockable {
        private DockKey dockKey;
        private final IEditor dockableEditor;
        private final DockGroup dockGroup;
        private DockableActionCustomizer actionCustomizer = new DockableActionCustomizer() {
            public void visitTabSelectorPopUp(javax.swing.JPopupMenu popUpMenu, Dockable tabbedDockable) {
                popUpMenu.add(TabbedContainerActions.createCloseAllAction(tabbedDockable, workspace));
                popUpMenu.add(TabbedContainerActions.createCloseAllOtherAction(tabbedDockable, workspace));
            };
        };
        private final DockingDesktop workspace;

        public EditorDockable(IEditor e, DockGroup dockGroup, DockingDesktop workspace) {
            this.dockableEditor = e;
            this.dockGroup = dockGroup;
            this.workspace = workspace;
        }

        public Component getComponent() {
            return dockableEditor.getComponent();
        }

        public DockKey getDockKey() {
            if (dockKey == null) {
                FileHandler fileHandler = (FileHandler) dockableEditor.getData("filehandler");
                File currentFile = fileHandler.getCurrentFile();
                String key;
                if (currentFile != null)
                    key = currentFile.getAbsolutePath();
                else
                    key = (String) dockableEditor.getData("filename");
                dockKey = new DockKey(key, (String) dockableEditor.getData("filename"));
                dockKey.setDockGroup(dockGroup);
            }
            if (dockKey.getActionCustomizer() == null) {
                actionCustomizer.setTabSelectorPopUpCustomizer(true);
                dockKey.setActionCustomizer(actionCustomizer);
            }
            return dockKey;
        }

        public IEditor getEditor() {
            return dockableEditor;
        }

        @Override public String toString() {
            return super.toString() + getDockKey();
        }

        public void updateKey() {
            FileHandler fileHandler = (FileHandler) dockableEditor.getData("filehandler");
            File currentFile = fileHandler.getCurrentFile();
            String key;
            if (currentFile != null)
                key = currentFile.getAbsolutePath();
            else
                key = (String) dockableEditor.getData("filename");
            dockKey.setKey(key);
        }
    }

    private enum TOOLBAR_OPTIONS {
        ONLY_ICONS, ICONS_AND_TEXT
    }

    private TOOLBAR_OPTIONS toolbarView = TOOLBAR_OPTIONS.ONLY_ICONS;

    private TestSuite testSuite;

    /**
     * Create JMenu with new file/folder options
     * 
     * @return
     */
    private JMenu createNewMenu() {
        JMenu newMenu = new JMenu("New");
        newMenu.add(getMenuItemWithAccelKey(newTestcaseAction, "^+N"));
        newMenu.add(etAction);
        newMenu.add(newModuleAction);
        newMenu.add(newFixtureAction);
        newMenu.add(newModuleDirAction);
        newMenu.add(newSuiteFileAction);
        return newMenu;
    }

    /**
     * Removes the given directory name from the module directories in the
     * project file.
     * 
     * @param removeDir
     */
    public void removeModDirFromProjFile(String removeDir) {
        String[] moduleDirs = Constants.getMarathonDirectoriesAsStringArray(Constants.PROP_MODULE_DIRS);
        StringBuilder sbr = new StringBuilder();
        for (int i = 0; i < moduleDirs.length; i++) {
            if (moduleDirs[i].equals(removeDir))
                continue;
            sbr.append(getProjectRelativeName(moduleDirs[i]) + ";");
        }
        try {
            updateProjectFile(Constants.PROP_MODULE_DIRS, sbr.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String createTestReportDirName() {
        return (exploratoryTest ? "et-" : "tr-") + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
    }

    public void onShowReport() {
        final String url = resultReporterHTMLFile.toURI().toString();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    browserLauncher.openURLinBrowser(url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void insertChecklist(String name) {
        if (exploratoryTest) {
            CheckList checklist = fillUpChecklist(name);
            if (checklist == null)
                return;
            try {
                File file = File.createTempFile(name, ".data", runReportDir);
                checklist.xsetDataFile(file);
                display.recordShowChecklist(runReportDir.getName() + "/" + file.getName());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Unable to create a checklist data file");
                e.printStackTrace();
                return;
            }
        } else
            display.insertChecklist(name);
    }

    private void resumePlay() {
        closeScriptConsole();
        display.resume();
    }

    private void closeScriptConsole() {
        if (scriptConsole != null) {
            scriptConsole.dispose();
            scriptConsole = null;
        }
    }

    private void saveBreakPoints() {
        String projectDir = System.getProperty(Constants.PROP_PROJECT_DIR);
        ObjectOutputStream os;
        try {
            os = new ObjectOutputStream(new FileOutputStream(new File(projectDir, ".breakpoints")));
            List<BreakPoint> bps = new ArrayList<BreakPoint>();
            for (BreakPoint breakPoint : breakpoints) {
                if (breakPoint.shouldSave())
                    bps.add(breakPoint);
            }
            os.writeObject(bps);
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked") private void loadBreakPoints() {
        String projectDir = System.getProperty(Constants.PROP_PROJECT_DIR);
        ObjectInputStream is = null;
        try {
            is = new ObjectInputStream(new FileInputStream(new File(projectDir, ".breakpoints")));
            breakpoints = (List<BreakPoint>) is.readObject();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
            }
        }
    }

    public void toggleBreakPoint(int line) {
        if (editor == null)
            return;
        editor.setFocus();
        if (getFileHandler(editor).isProjectFile()) {
            BreakPoint bp = new BreakPoint(displayView.getFilePath(), line);
            if (breakpoints.contains(bp)) {
                breakpoints.remove(bp);
            } else {
                breakpoints.add(bp);
            }
            setState();
            editor.refresh();
        }
    }

    /**
     * The Navigator panel.
     */
    private Navigator navigator;
    /**
     * The TestView panel is obtained from the testRunner.
     */
    private TestRunner testRunner;
    /**
     * The current state of Marathon.
     */
    private State state = State.STOPPED_WITH_APP_CLOSED;

    private int windowState;

    /**
     * Controller provides a small Window for controlling Marathon while
     * recording
     */
    public class Controller extends JFrame implements WindowStateListener, IErrorListener {
        private static final long serialVersionUID = 1L;
        private JToolBar toolBar = null;
        private JTextArea textArea = new JTextArea(5, 40);
        private int pressX;
        private int pressY;
        private JLabel msgLabel;

        /**
         * Construct a controller
         */
        public Controller() {
            setName("ControllerWindow");
            createToolbar();
            textArea.setBackground(Color.BLACK);
            textArea.setForeground(Color.GREEN);
            textArea.setEditable(false);
            textArea.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    pressX = e.getX();
                    pressY = e.getY();
                }
            });
            textArea.addMouseMotionListener(new MouseMotionListener() {
                public void mouseDragged(MouseEvent e) {
                    setLocation(getLocation().x + (e.getX() - pressX), getLocation().y + (e.getY() - pressY));
                }

                public void mouseMoved(MouseEvent e) {
                }
            });
            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(textArea, BorderLayout.CENTER);
            getContentPane().add(toolBar, BorderLayout.WEST);
            msgLabel = new JLabel("   ");
            msgLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
            getContentPane().add(msgLabel, BorderLayout.SOUTH);
            setResizable(false);
            pack();
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation(size.width - getWidth(), 0);
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            setAlwaysOnTopIfAvailable();
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    DisplayWindow.this.setState(NORMAL);
                }
            });
            DisplayWindow.this.addWindowStateListener(this);
        }

        public void clear() {
            textArea.setText("");
            isRawRecording = false;
            rawRecordButton.setSelected(false);
            msgLabel.setText("   ");
            msgLabel.setIcon(null);
        }

        /**
         * Create the controller toolbar
         */
        private void createToolbar() {
            toolBar = new JToolBar("miniController", JToolBar.VERTICAL);
            setTitle("Marathon Control Center");
            toolBar.add(getActionButton(pauseAction));
            toolBar.add(getActionButton(resumeRecordingAction));
            toolBar.add(getActionButton(insertScriptAction));
            toolBar.add(getActionButton(insertChecklistAction));
            toolBar.add(getActionButton(stopAction));
            rawRecordButton = getActionButton(rawRecordAction);
            rawRecordButton.setSelectedIcon(rawRecordButton.getDisabledIcon());
            toolBar.add(rawRecordButton);
            toolBar.setFloatable(false);
            toolBar.add(getActionButton(recorderConsoleAction));
        }

        /**
         * Set AlwayOnTop property - available only from Java 5 - hence the
         * reflection API
         */
        private void setAlwaysOnTopIfAvailable() {
            Method method = null;
            try {
                method = this.getClass().getMethod("setAlwaysOnTop", new Class[] { Boolean.class });
                method.invoke(this, new Object[] { Boolean.TRUE });
            } catch (Throwable e) {
                return;
            }
        }

        public void windowStateChanged(WindowEvent e) {
            if (e.getNewState() == NORMAL)
                Controller.this.setVisible(false);
            else if (e.getNewState() == ICONIFIED && DisplayWindow.this.state.isRecording()) {
                Controller.this.setVisible(true);
            }
        }

        public void insertScript(String script) {
            BufferedReader reader = new BufferedReader(new StringReader(script));
            String line;
            String[] lines = new String[5];
            int index = 0;
            try {
                while ((line = reader.readLine()) != null) {
                    lines[index++] = line;
                    if (index == 5)
                        index = 0;
                }
            } catch (IOException e) {
            }
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                if (lines[index] != null)
                    text.append(lines[index].trim()).append('\n');
                if (++index == 5)
                    index = 0;
            }
            textArea.setText(text.toString());
        }

        public void addError(final LogRecord result) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    msgLabel.setIcon(LogView.ICON_ERROR);
                    msgLabel.setText(result.getMessage());
                }
            });
        }
    }

    void endController() {
        controller.setVisible(false);
        setExtendedState(windowState);
    }

    void startController() {
        controller.setVisible(true);
        windowState = getExtendedState();
        setExtendedState(ICONIFIED);
    }

    private Controller controller;
    private boolean stepIntoActive;
    private ScriptConsole scriptConsole;
    protected int breakStackDepth = -1;
    private JMenuItem enableChecklistMenuItem;
    private File resultReporterHTMLFile;
    private JCheckBoxMenuItem generateReportsMenuItem;
    private JButton recordActionButton;

    private DockingDesktop workspace;

    private SearchDialog searchDialog;

    /**
     * Constructs a DisplayWindow object.
     */
    public DisplayWindow() {
        DockingUISettings.getInstance().installUI();
        new ActionInjector(DisplayWindow.this).injectActions();
        controller = new Controller();
        reportDir = new File(new File(System.getProperty(Constants.PROP_PROJECT_DIR)), "TestReports");
        if (!reportDir.exists())
            if (!reportDir.mkdir()) {
                logger.warning("Unable to create report directory: " + reportDir + " - Marathon might not function properly");
            }
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        if (isMac()) {
            setupMacMenuItems();
        }
        breakpoints = new ArrayList<BreakPoint>();
        fileEventHandler = new FileEventHandler();
        navigatorListener = new NavigatorListener();
        fileEventHandler.addFileEventListener(navigatorListener);
    }

    @Inject public void setDisplay() {
        initUI();
        display.setView(displayView);
        setDefaultFixture(getDefaultFixture());
        Indent.setDefaultIndent(editorProvider.getTabConversion(), editorProvider.getTabSize());
        logViewLogger = new LogViewLogger(logView);
        RuntimeLogger.setRuntimeLogger(logViewLogger);
        logView.setErrorListener(controller);
    }

    public void dispose() {
        super.dispose();
        if (controller != null)
            controller.dispose();
    }

    private void setupMacMenuItems() {
        try {
            Class<?> utilOSX = Class.forName("net.sourceforge.marathon.util.osx.OSXUtil");
            Constructor<?> constructor = utilOSX.getConstructor(new Class<?>[] { IOSXApplicationListener.class });
            constructor.newInstance(new Object[] { this });
        } catch (Throwable e) {
            logger.info(e.getMessage());
        }
    }

    /**
     * Open and display a file in the editor
     * 
     * @param directory
     *            , the directory
     * @param fileName
     *            , name of the file
     * @return true, if the file given is accessible and shown
     */
    private boolean showFile(String directory, String fileName) {
        File file;
        file = new File(directory, fileName);
        if (file.canRead()) {
            openFile(file);
            return true;
        }
        return false;
    }

    /**
     * Sets up the default fixture. Note: The given fixture is saved into the
     * preferrences and selected when Marathon is restarted.
     * 
     * @param fixture
     *            , the fixture
     */
    public void setDefaultFixture(String fixture) {
        this.fixture = fixture;
        statusPanel.setFixture(" " + fixture + " ");
        Preferences p = Preferences.userNodeForPackage(this.getClass());
        p.put(getPrefKey("fixture.", System.getProperty(Constants.PROP_PROJECT_DIR, "NoProject")), fixture);
        try {
            p.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        display.setDefaultFixture(fixture);
    }

    public static String getPrefKey(String prefix, String dir) {
        String key = prefix + dir;
        if (key.length() <= Preferences.MAX_KEY_LENGTH)
            return key;
        int len = Preferences.MAX_KEY_LENGTH - prefix.length();
        String suffix = dir.substring(dir.length() - len);
        return prefix + suffix;
    }

    /**
     * Get the default fixture from user preferrences.
     * 
     * @return
     */
    public String getDefaultFixture() {
        Preferences p = Preferences.userNodeForPackage(this.getClass());
        return p.get(getPrefKey("fixture.", System.getProperty(Constants.PROP_PROJECT_DIR, "NoProject")), "default");
    }

    /**
     * Initialize the UI for the Main window.
     */
    private void initUI() {
        setName("DisplayWindow");

        Preferences p = Preferences.userNodeForPackage(this.getClass());
        int x = p.getInt(getPrefKey("window.x", System.getProperty(Constants.PROP_PROJECT_DIR)), -1);
        int y = p.getInt(getPrefKey("window.y", System.getProperty(Constants.PROP_PROJECT_DIR)), -1);
        int w = p.getInt(getPrefKey("window.w", System.getProperty(Constants.PROP_PROJECT_DIR)), -1);
        int h = p.getInt(getPrefKey("window.h", System.getProperty(Constants.PROP_PROJECT_DIR)), -1);
        if (x == -1)
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        else {
            setSize(w, h);
            setLocation(x, y);
        }

        setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(
                this.getClass().getClassLoader().getResource("net/sourceforge/marathon/display/images/logo16.gif")));
        setJMenuBar(createMenuBar());
        workspace = new DockingDesktop("Marathon");
        workspace.addDockableSelectionListener(dockingListener);
        workspace.addDockableStateWillChangeListener(dockingListener);
        workspace.addDockableStateChangeListener(dockingListener);
        ToolBarContainer container = ToolBarContainer.createDefaultContainer(true, true, true, true);
        createToolBar(container);
        container.add(workspace, BorderLayout.CENTER);
        this.getContentPane().add(container, BorderLayout.CENTER);
        this.getContentPane().add(statusPanel, BorderLayout.SOUTH);
        initStatusBar();
        initDesktop();
        setExitHook();
    }

    /**
     * Initialize the desktop. If available, the previous desktop configuration
     * is restored.
     */
    public void initDesktop() {
        loadBreakPoints();
        createNavigatorPanel();
        createJUnitPanel();
        resultPane.addSelectionListener(resultPaneSelectionListener);

        editorDockGroup = new DockGroup("Editors");
        workspace.getContext().setDockableResolver(new DockableResolver() {
            public Dockable resolveDockable(final String keyName) {
                if (keyName.equals("Navigator"))
                    return navigator;
                else if (keyName.equals("JUnit"))
                    return testRunner;
                else if (keyName.equals("Output"))
                    return outputPane;
                else if (keyName.equals("Results"))
                    return resultPane;
                else if (keyName.equals("Log"))
                    return logView;
                else {
                    File file = new File(keyName);
                    IEditor e;
                    if (!file.exists())
                        return new FnFDockable(keyName);
                    e = createEditor(file);
                    Dockable dockable = (Dockable) e.getData("dockable");
                    return dockable;
                }
            }
        });
        try {
            Preferences preferences = Preferences.userNodeForPackage(DisplayWindow.class);
            String bytes = preferences.get(getPrefKey("workspace.", System.getProperty(Constants.PROP_PROJECT_DIR)), null);
            if (bytes != null) {
                workspace.readXML(new ByteArrayInputStream(bytes.getBytes()));
                DockableState[] dockables = workspace.getDockables();
                for (final DockableState dockableState : dockables) {
                    if (dockableState.getDockable() instanceof FnFDockable || !dockableState.isDocked()) {
                        workspace.unregisterDockable(dockableState.getDockable());
                    }
                }
                dockables = workspace.getDockables();
                for (DockableState ds : dockables) {
                    if (ds.getDockable() instanceof EditorDockable) {
                        editor = ((EditorDockable) ds.getDockable()).getEditor();
                        break;
                    }
                }
                return;
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (ParserConfigurationException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (SAXException e1) {
            e1.printStackTrace();
        }

        EditorDockable[] editors = new EditorDockable[] {};
        IEditor readmeEditor = null;
        try {
            readmeEditor = getReadmeEditor();
            if (readmeEditor != null)
                editors = new EditorDockable[] { (EditorDockable) readmeEditor.getData("dockable") };
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        createDefaultWorkspace(editors);
    }

    @Override public void setVisible(boolean b) {
        super.setVisible(b);
        if (editor != null && b)
            editor.setFocus();
    }

    private void createDefaultWorkspace(EditorDockable[] editorDockables) {
        DockableState[] dockableStates = workspace.getDockables();
        for (DockableState dockableState : dockableStates) {
            workspace.close(dockableState.getDockable());
        }
        workspace.addDockable(resultPane);
        workspace.split(resultPane, navigator, DockingConstants.SPLIT_LEFT, 0.2);
        workspace.createTab(navigator, testRunner, 1, false);
        workspace.createTab(resultPane, outputPane, 0, false);
        workspace.createTab(resultPane, logView, 1, false);
        for (EditorDockable e : editorDockables) {
            setCurrentEditorDockable(e);
        }
    }

    /**
     * Returns the contents of readme file in a Component from either the
     * project directory or Marathon README.txt
     * 
     * @return Component containing the contents of readme file.
     */
    private IEditor getReadmeEditor() throws IOException {
        File readmeFile = new File(System.getProperty(Constants.PROP_HOME) + "/README.txt");
        if (readmeFile.exists()) {
            return createEditor(readmeFile);
        }
        return null;
    }

    /**
     * Initialize the status bar
     */
    private void initStatusBar() {
        statusPanel.getFixtureLabel().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                onSelectFixture();
            }
        });
        statusPanel.getRowLabel().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                gotoLine();
            }
        });
        statusPanel.getInsertLabel().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (editor != null)
                    editor.toggleInsertMode();
            }
        });
    }

    /**
     * Initialize the JUnit panel
     * 
     * @return junitpanel, a Dockable
     */
    private TestRunner createJUnitPanel() {
        testRunner = new TestRunner(taConsole, fileEventHandler);
        testRunner.setAcceptChecklist(true);
        testRunner.addTestOpenListener(testListener);
        testRunner.getFailureView().addMessageProcessor(stackMessageProcessor);
        fileEventHandler.addFileEventListener(testRunner);
        return testRunner;
    }

    /**
     * Set the accelerator keys. Note: the accelerator keys need to be added
     * while MenuItems are created as well as set them in the editor.
     * 
     * @param editor
     */
    private void setAcceleratorKeys(IEditor editor) {
        editor.addKeyBinding("^+g", new GotoLineAction());
        editor.addKeyBinding("^+s", saveAction);
        editor.addKeyBinding("^S+a", saveAsAction);
        editor.addKeyBinding("^+p", playAction);
        editor.addKeyBinding("^+r", recordAction);
        editor.addKeyBinding("^+b", toggleBreakpointAction);
        editor.addKeyBinding("^+n", newTestcaseAction);
    }

    /**
     * Set the exit hook for the Main window.
     */
    private void setExitHook() {
        this.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                windowActivated(e);
            }

            public void windowActivated(WindowEvent e) {
            }

            public void windowClosing(WindowEvent e) {
                exitAction.actionPerformed(null);
            }
        });
    }

    private int commonParentDirectory(File directory, List<String> Directories) {
        for (int i = 0; i < Directories.size(); i++) {
            File rootDirectory = new File(Directories.get(i));
            try {
                if (directory != null && rootDirectory != null) {
                    String dirPath = directory.getCanonicalPath();
                    String rootPath = rootDirectory.getCanonicalPath();
                    if (dirPath.startsWith(rootPath)) {
                        return (i + 1);
                    }
                    if (rootPath.startsWith(dirPath)) {
                        return -(i + 1);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private String[] getProjectDirectories() {
        List<String> Directories = new ArrayList<String>();
        Directories.add(System.getProperty(Constants.PROP_PROJECT_DIR));
        String[] dirs = Constants.getMarathonDirectoriesAsStringArray(Constants.PROP_MODULE_DIRS);
        for (int j = 0; j < dirs.length; j++) {
            File dir = new File(dirs[j]);
            int common = commonParentDirectory(dir, Directories);
            if (common == 0) {
                Directories.add(dirs[j]);
            } else if (common < 0) {
                Directories.set(Math.abs(common) - 1, dirs[j]);
            }
        }
        return Directories.toArray(new String[0]);
    }

    /**
     * Create the navigator panel.
     * 
     * 
     * @return navigatorPanel, new Navigator panel.
     */
    private Navigator createNavigatorPanel() {
        String[] dirs = getProjectDirectories();
        String[] rootDirs = getProjectDirectories();
        dirs[0] = System.getProperty(Constants.PROP_PROJECT_NAME);
        for (int i = 1; i < dirs.length; i++) {
            dirs[i] = new File(dirs[i]).getName();
        }
        String[] rootDesc = dirs;
        try {
            NavigatorFileAction openAction = new NavigatorFileAction() {
                public void actionPerformed(File file, boolean useSystemApplication) {
                    if (file.isFile()) {
                        if (useSystemApplication)
                            desktopOpen(file);
                        else
                            openFile(file);
                    }
                }
            };
            navigator = new Navigator(rootDirs, null, rootDesc, fileEventHandler, this);
            navigator.setInitialExpansion(Constants.getAllMarathonDirectoriesAsStringArray());
            navigator.setActions(openAction, null);
            FileHandler fileHandler = new FileHandler(new MarathonFileFilter(scriptModel.getSuffix(), scriptModel), new File(
                    System.getProperty(Constants.PROP_TEST_DIR)), new File(System.getProperty(Constants.PROP_FIXTURE_DIR)),
                    Constants.getMarathonDirectories(Constants.PROP_MODULE_DIRS), this);
            DisplayWindowNavigatorActions actions = new DisplayWindowNavigatorActions(this, navigator, fileHandler);
            navigator.setMenuItems(actions.getMenuItems());
            navigator.setToolbar(actions.getToolBar());
            navigator.addNavigatorListener(navigatorListener);
            fileEventHandler.addFileEventListener(navigator);
            return navigator;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void desktopOpen(File file) {
        try {
            Class<?> desktopKlass = Class.forName("java.awt.Desktop");
            Method gdMethod = desktopKlass.getMethod("getDesktop");
            Object desktop = gdMethod.invoke(null);
            Method openMethod = desktopKlass.getMethod("open", File.class);
            openMethod.invoke(desktop, file);
        } catch (Throwable e) {
            logger.info(e.getMessage());
        }
    }

    /**
     * Create the menu bar for the Main window.
     * 
     * @return menubar
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menu.setMnemonic('f');
        menu.add(createNewMenu());
        menu.add(getMenuItemWithAccelKey(saveAction, "^+S"));
        menu.add(getMenuItemWithAccelKey(saveAsAction, "^S+A"));
        menu.add(getMenuItemWithAccelKey(saveAllAction, "^S+S"));
        if (!isMac()) {
            menu.addSeparator();
            menu.add(getMenuItemWithAccelKey(exitAction, "A+F4"));
        }
        menuBar.add(menu);
        menu = new JMenu("Edit");
        menu.setMnemonic('e');
        menu.add(getMenuItemWithAccelKey(undoAction, "^+Z"));
        menu.add(getMenuItemWithAccelKey(redoAction, "^S+Z"));
        menu.addSeparator();
        menu.add(getMenuItemWithAccelKey(cutAction, "^+X"));
        menu.add(getMenuItemWithAccelKey(copyAction, "^+C"));
        menu.add(getMenuItemWithAccelKey(pasteAction, "^+V"));
        menu.addSeparator();
        menu.add(getMenuItemWithAccelKey(searchAction, "^+F"));
        menu.add(getMenuItemWithAccelKey(findNextAction, "^+K"));
        menu.add(getMenuItemWithAccelKey(findPreviousAction, "^S+K"));
        menu.addSeparator();
        menu.add(getMenuItemWithAccelKey(refreshAction, "F5"));
        menu.add(getMenuItemWithAccelKey(new GotoLineAction(), "^+L"));
        if (!isMac()) {
            menu.addSeparator();
            menu.add(preferencesAction);
        }
        menuBar.add(menu);
        menu = new JMenu("Marathon");
        menu.setMnemonic('m');
        menu.add(getMenuItemWithAccelKey(playAction, "^+P"));
        menu.add(getMenuItemWithAccelKey(slowPlayAction, "^S+P"));
        menu.add(getMenuItemWithAccelKey(debugAction, "^A+P"));
        menu.addSeparator();
        menu.add(getMenuItemWithAccelKey(recordAction, "^+R"));
        menu.add(getMenuItemWithAccelKey(etAction, "^S+R"));
        menu.add(stopAction);
        menu.addSeparator();
        menu.add(openApplicationAction);
        menu.add(closeApplicationAction);
        menu.addSeparator();
        menu.add(getMenuItemWithAccelKey(selectFixtureAction, "^+F5"));
        menu.addSeparator();
        menu.add(getMenuItemWithAccelKey(toggleBreakpointAction, "^S+B"));
        menu.add(clearAllBreakpointsAction);
        menu.addSeparator();
        menu.add(getMenuItemWithAccelKey(showReportAction, "^+F6"));
        menu.addSeparator();
        menu.add(projectSettingsAction);
        if (editorProvider.isEditorSettingsAvailable())
            menu.add(editorProvider.getEditorSettingsMenuItem(this));
        menu.add(scriptConsoleSettingsAction);
        if (editorProvider.isEditorShortcutKeysAvailable())
            menu.add(editorProvider.getEditorShortcutMenuItem(this));
        menu.addSeparator();
        menu.add(manageChecklistsAction);
        enableChecklistMenuItem = new JCheckBoxMenuItem("Enable Checklists", EMPTY_ICON);
        enableChecklistMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                display.setAcceptChecklist(enableChecklistMenuItem.isSelected());
                testRunner.setAcceptChecklist(enableChecklistMenuItem.isSelected());
                if (enableChecklistMenuItem.isSelected() && !generateReportsMenuItem.isSelected()) {
                    JOptionPane.showMessageDialog(DisplayWindow.this, "Enabling generate reports (needed for checklists)");
                    generateReportsMenuItem.setSelected(true);
                }
            }
        });
        enableChecklistMenuItem.setSelected(false);
        display.setAcceptChecklist(false);
        if (testRunner != null)
            testRunner.setAcceptChecklist(false);
        menu.add(enableChecklistMenuItem);
        menu.addSeparator();
        generateReportsMenuItem = new JCheckBoxMenuItem("Generate Reports", EMPTY_ICON);
        generateReportsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resultReporterHTMLFile = null;
                Properties props = System.getProperties();
                props.remove(Constants.PROP_IMAGE_CAPTURE_DIR);
                if (enableChecklistMenuItem.isSelected() && !generateReportsMenuItem.isSelected()) {
                    JOptionPane.showMessageDialog(DisplayWindow.this, "Disabling checklists (Generate reports required)");
                    enableChecklistMenuItem.setSelected(false);
                }
                setState();
            }
        });
        generateReportsMenuItem.setSelected(false);
        menu.add(generateReportsMenuItem);
        menuBar.add(menu);
        if (actionProvider != null) {
            IMarathonAction[] actions = actionProvider.getActions();
            for (IMarathonAction action : actions) {
                if (!action.isMenuBarAction())
                    continue;
                String menuName = action.getMenuName();
                if (menuName == null)
                    continue;
                JMenu menux = findMenu(menuBar, menuName);
                if (action.isSeperator()) {
                    menux.addSeparator();
                    continue;
                }
                menux.setMnemonic(action.getMenuMnemonic());
                String accelKey = action.getAccelKey();
                if (accelKey != null) {
                    menux.add(getMenuItemWithAccelKey(createAction(action), accelKey));
                } else {
                    if (action.getButtonGroup() != null) {
                        ButtonGroup group = action.getButtonGroup();
                        JRadioButtonMenuItem radio = new JRadioButtonMenuItem(createAction(action));
                        group.add(radio);
                        menux.add(radio);
                        if(action.isSelected())
                            radio.doClick();
                    } else
                        menux.add(createAction(action));
                }
            }
        }
        menu = new JMenu("Window");
        menu.setMnemonic('W');
        menu.add(resetWorkspaceAction);
        menuBar.add(menu);
        menu = new JMenu("Help");
        menu.setMnemonic('h');
        menu.add(releaseNotes);
        menu.add(changeLog);
        menu.add(visitWebsite);
        menu.add(helpAboutAction);
        menuBar.add(menu);
        return menuBar;
    }

    private JMenu findMenu(JMenuBar menuBar, String menuName) {
        int n = menuBar.getMenuCount();
        for (int i = 0; i < n; i++) {
            JMenu menu = menuBar.getMenu(i);
            if (menu.getText().equals(menuName))
                return menu;
        }
        JMenu menu = new JMenu(menuName);
        menuBar.add(menu);
        return menu;
    }

    /**
     * Given a SimpleAction and a keySequence return the menu item with the
     * accelerator key.
     * 
     * @param action
     * @param keySequence
     * @return item, a JMenuItem
     */
    protected JMenuItem getMenuItemWithAccelKey(Action action, String keySequence) {
        JMenuItem item = new JMenuItem(action);
        KeyStroke keyStroke = OSUtils.parseKeyStroke(keySequence);
        if (keyStroke != null)
            item.setAccelerator(keyStroke);
        return item;
    }

    /**
     * Create a toolbar the Main window. Since we use VLDocking framework the
     * ToolBarContainer provides the panel where to attach the toolbar.
     * 
     * @param container
     */
    private void createToolBar(ToolBarContainer container) {
        ToolBarPanel toolBarPanel = container.getToolBarPanelAt(BorderLayout.NORTH);
        VLToolBar toolBar = new VLToolBar();
        toolBar.add(getActionButton(newTestcaseAction));
        toolBar.add(getActionButton(etAction));
        toolBar.add(getActionButton(newModuleAction));
        toolBar.add(getActionButton(saveAction));
        toolBar.add(getActionButton(saveAsAction));
        toolBar.add(getActionButton(saveAllAction));
        toolBarPanel.add(toolBar, new ToolBarConstraints(0, 0));
        toolBar = new VLToolBar();
        toolBar.add(getActionButton(undoAction));
        toolBar.add(getActionButton(redoAction));
        toolBar.add(getActionButton(cutAction));
        toolBar.add(getActionButton(copyAction));
        toolBar.add(getActionButton(pasteAction));
        toolBar.add(getActionButton(searchAction));
        toolBarPanel.add(toolBar, new ToolBarConstraints(0, 1));
        toolBar = new VLToolBar();
        recordActionButton = getActionButton(recordAction);
        toolBar.add(recordActionButton);
        toolBar.add(getActionButton(pauseAction));
        toolBar.add(getActionButton(resumeRecordingAction));
        toolBar.add(getActionButton(insertScriptAction));
        toolBar.add(getActionButton(insertChecklistAction));
        toolBar.add(getActionButton(stopAction));
        toolBar.add(getActionButton(recorderConsoleAction));
        toolBarPanel.add(toolBar, new ToolBarConstraints(0, 2));
        toolBar = new VLToolBar();
        toolBar.add(getActionButton(openApplicationAction));
        toolBar.add(getActionButton(closeApplicationAction));
        toolBarPanel.add(toolBar, new ToolBarConstraints(0, 3));
        toolBar = new VLToolBar();
        toolBar.add(getActionButton(playAction));
        toolBar.add(getActionButton(slowPlayAction));
        toolBar.add(getActionButton(debugAction));
        toolBar.add(getActionButton(toggleBreakpointAction));
        toolBar.add(getActionButton(resumePlayingAction));
        toolBar.add(getActionButton(stepIntoAction));
        toolBar.add(getActionButton(stepOverAction));
        toolBar.add(getActionButton(stepReturnAction));
        toolBar.add(getActionButton(playerConsoleAction));
        toolBar.add(getActionButton(showReportAction));
        toolBarPanel.add(toolBar, new ToolBarConstraints(0, 4));
        if (actionProvider != null) {
            toolBar = new VLToolBar();
            IMarathonAction[] actions = actionProvider.getActions();
            for (int i = 0; i < actions.length; i++) {
                final IMarathonAction action = actions[i];
                if (!action.isToolBarAction())
                    continue;
                if (action.isSeperator()) {
                    toolBarPanel.add(toolBar, new ToolBarConstraints(0, 5));
                    toolBar = new VLToolBar();
                } else {
                    toolBar.add(getActionButton(createAction(action)));
                }
            }
            toolBarPanel.add(toolBar, new ToolBarConstraints(0, 5));
        }
        showReportAction.setEnabled(false);
        return;
    }

    private AbstractSimpleAction createAction(final IMarathonAction action) {
        return new AbstractSimpleAction(action.getName(), action.getDescription(), action.getMneumonic(), action.getEnabledIcon(),
                action.getDisabledIcon()) {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent arg0) {
                try {
                    int selectionStart = -1;
                    int selectionEnd = -1;
                    int startLine = -1;
                    int startOffsetOfStartLine = -1;
                    int startOffsetOfEndLine = -1;
                    String text = null;
                    int endOffsetOfEndLine = -1;
                    int endLine = -1;

                    if (editor != null) {
                        selectionStart = editor.getSelectionStart();
                        selectionEnd = editor.getSelectionEnd();
                        startLine = editor.getLineOfOffset(selectionStart);
                        endLine = editor.getLineOfOffset(selectionEnd);
                        startOffsetOfStartLine = editor.getLineStartOffset(startLine);
                        startOffsetOfEndLine = editor.getLineStartOffset(endLine);
                        text = editor.getText();
                        if (selectionEnd == startOffsetOfEndLine && selectionStart != selectionEnd)
                            endLine = endLine - 1;
                        endOffsetOfEndLine = editor.getLineEndOffset(endLine);
                    }

                    action.actionPerformed(DisplayWindow.this, scriptModel, text, startOffsetOfStartLine, endOffsetOfEndLine,
                            startLine);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };
    }

    private JButton getActionButton(Action action) {
        if (action instanceof AbstractSimpleAction)
            return ((AbstractSimpleAction) action).getButton();
        JButton button = UIUtils.createActionButton(action);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        if (action.getValue(Action.SMALL_ICON) != null && toolbarView == TOOLBAR_OPTIONS.ONLY_ICONS)
            button.setText(null);
        return button;
    }

    private int nDirty() {
        int n = 0;
        DockableState[] dockables = workspace.getDockables();
        for (DockableState dockableState : dockables) {
            Dockable dockable = dockableState.getDockable();
            if (dockable instanceof EditorDockable) {
                IEditor editor = ((EditorDockable) dockable).getEditor();
                if (editor.isDirty()) {
                    n++;
                }
            }
        }
        return n;
    }

    private void setState() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                displayView.setState(state);
            }
        });
    }

    /**
     * Save any unsaved buffers from the editor.
     * 
     * @return
     */
    private boolean saveFileIfNeeded() {
        DockableState[] dockables = workspace.getDockables();
        for (DockableState dockableState : dockables) {
            Dockable dockable = dockableState.getDockable();
            if (dockable instanceof EditorDockable) {
                IEditor editor = ((EditorDockable) dockable).getEditor();
                if (editor.isDirty()) {
                    if (!closeEditor(editor))
                        return false;
                }
            }
        }
        updateView();
        return true;
    }

    /**
     * Goto a given line. This action is attached to the row number of the
     * status bar.
     */
    public class GotoLineAction extends AbstractAction implements ActionListener {
        private static final long serialVersionUID = 1L;

        public GotoLineAction() {
            super("Go to line...", EMPTY_ICON);
        }

        public void actionPerformed(ActionEvent evt) {
            gotoLine();
        }
    }

    /**
     * Get the insertAction - for testing purpose.
     * 
     * @return
     */
    public Action getInsertAction() {
        return insertScriptAction;
    }

    /**
     * Get the currently available fixtures.
     * 
     * @return
     */
    private String[] getFixtures() {
        return scriptModel.getFixtures();
    }

    /**
     * Select a fixture from the available fixtures.
     */
    public void onSelectFixture() {
        setDefaultFixture(fixtureSelector.selectFixture(this, getFixtures(), fixture));
    }

    /**
     * Goto a line
     */
    private void gotoLine() {
        if (editor == null)
            return;
        int lastOffset = editor.getText().length();
        int lastLine;
        try {
            lastLine = editor.getLineOfOffset(lastOffset);
        } catch (BadLocationException e) {
            e.printStackTrace();
            return;
        }
        lineNumberDialog.setMaxLineNumber(lastLine + 1);
        lineNumberDialog.setLine(editor.getCaretLine() + 1);
        lineNumberDialog.setLocationRelativeTo(DisplayWindow.this);
        lineNumberDialog.setVisible(true);
        if (lineNumberDialog.getLineNumber() != -1) {
            editor.setCaretLine(lineNumberDialog.getLineNumber() - 1);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.sourceforge.marathon.display.DisplayView#goToFile(java.lang.String,
     * int)
     */
    public void goToFile(String fileName, int lineNumber) {
        File file = new File(fileName);
        if (file.exists()) {
            openFile(file);
            editor.setCaretLine(lineNumber - 1);
            editor.highlightLine(lineNumber);
        } else if (fileName.startsWith(NEW_FILE)) {
            EditorDockable editorDockable = findEditorDockable(fileName);
            if (editorDockable != null) {
                selectDockable(editorDockable);
                editor.setCaretLine(lineNumber - 1);
                editor.highlightLine(lineNumber);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.marathon.display.DisplayView#updateView()
     */
    public void updateView() {
        String projectName = System.getProperty(Constants.PROP_PROJECT_NAME, "");
        if (projectName.equals(""))
            projectName = "Marathon";
        String suffix = "";
        if (editor != null && editor.isDirty()) {
            suffix = "*";
        }
        if (editor != null && editor.getData("filename") != null) {
            setTitle(projectName + " - " + (editor.getData("filename") + suffix));
            updateDockName(editor);
        } else
            setTitle(projectName);
        updateEditActions();
        setState();
    }

    /**
     * Update the Editor related actions in Menu and Toolbars.
     */
    private void updateEditActions() {
        Clipboard clipBoard = getToolkit().getSystemClipboard();
        try {
            clipBoard.getContents(this).getTransferData(DataFlavor.stringFlavor);
            pasteAction.setEnabled(true && displayView.getFilePath() != null);
        } catch (Exception exception) {
            pasteAction.setEnabled(false);
        }
        boolean contentSelected = editor != null && editor.getSelectionStart() != editor.getSelectionEnd();
        cutAction.setEnabled(contentSelected);
        copyAction.setEnabled(contentSelected);
        undoAction.setEnabled(editor != null && editor.canUndo());
        redoAction.setEnabled(editor != null && editor.canRedo());
    }

    /**
     * Check whether the current file in the editor is a file belonging to
     * Marathon project.
     * 
     * @return true, if the file belongs to Marathon project.
     */
    private boolean isProjectFile() {
        return editor != null && getFileHandler(editor).isProjectFile();
    }

    /**
     * Check whether the current file in the editor is a test file.
     * 
     * @return true, if the current file is a test file.
     */
    public boolean isTestFile() {
        return editor != null && getFileHandler(editor).isTestFile();
    }

    /**
     * Create a new module in the file specified.
     * 
     * @return true, if the module is created.
     */
    private boolean newModuleFile() {
        MarathonModuleDialog moduleDialog = new MarathonModuleDialog(this, "New Module Function", scriptModel.getSuffix());
        moduleDialog.setVisible(true);

        if (moduleDialog.isOk()) {
            File moduleFile = new File(moduleDialog.getModuleDirectory(), moduleDialog.getFileName());
            int offset = 0;
            try {
                boolean fileExists = moduleFile.exists();
                addImportStatement(moduleFile, fileExists);
                if (fileExists && canAppend(moduleFile)) {
                    offset = (int) moduleFile.length();
                }
                FileWriter writer = new FileWriter(moduleFile, true);
                writer.write((offset > 0 ? EOL : "")
                        + getModuleHeader(moduleDialog.getFunctionName(), moduleDialog.getDescription()));
                writer.close();
                fileUpdated(moduleFile);
                openFile(moduleFile);
                final int o = offset;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        editor.setCaretPosition(scriptModel.getLinePositionForInsertionModule() + o);
                    }
                });
                resetModuleFunctions();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(DisplayWindow.this, "IOError: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return false;
    }

    /**
     * Adds the playback import statement to the module file.
     * 
     * @param moduleFile
     * @param fileExists
     * @throws IOException
     */
    private void addImportStatement(File moduleFile, boolean fileExists) throws IOException {
        String importStatement = scriptModel.getPlaybackImportStatement();
        if (importStatement == null || importStatement.trim().length() == 0)
            return;
        String startMarker = scriptModel.getMarathonStartMarker();
        String endMarker = scriptModel.getMarathonEndMarker();
        String defaultMarkersImportStmt = startMarker + "\n" + importStatement + "\n" + endMarker + "\n";

        if (fileExists) {
            String moduleContents = "";
            moduleContents = readFile(moduleFile);
            int startMarkerIndex = moduleContents.indexOf(startMarker);
            int endMarkerIndex = moduleContents.indexOf(endMarker, startMarkerIndex);
            boolean importStatementFound = false;
            if (startMarkerIndex != -1 && endMarkerIndex != -1) {
                importStatementFound = importStatementExists(importStatement,
                        moduleContents.substring(startMarkerIndex + startMarker.length(), endMarkerIndex));
            }
            if (!importStatementFound) {
                int insertIndex;
                String insertContents;
                if (startMarkerIndex == -1 || endMarkerIndex == -1) {
                    insertIndex = 0;
                    insertContents = defaultMarkersImportStmt;
                } else {
                    insertIndex = startMarkerIndex + startMarker.length();
                    insertContents = "\n" + scriptModel.getPlaybackImportStatement() + "\n";
                }
                StringBuilder sbr = new StringBuilder(moduleContents);
                sbr.insert(insertIndex, insertContents);
                writeToFile(sbr.toString(), moduleFile);
            }
        } else {
            writeToFile(defaultMarkersImportStmt, moduleFile);
        }

    }

    /**
     * Writes the given contents to the file. Previous contents are lost.
     * 
     * @param string
     * @param moduleFile
     * @throws IOException
     */
    private void writeToFile(String string, File moduleFile) throws IOException {
        Writer writer = new FileWriter(moduleFile);
        writer.write(string);
        writer.flush();
        writer.close();
    }

    /**
     * Checks whether the given statement exists and exists in a single line.
     * 
     * @param statement
     * @param contents
     * @return
     */
    private boolean importStatementExists(String statement, String contents) {
        String[] lines = contents.split("\n");
        boolean importStatementFound = false;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].equals(statement)) {
                importStatementFound = true;
                break;
            }
        }
        return importStatementFound;
    }

    /**
     * Reads the given file and returns the contents of the file as a string.
     * 
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private String readFile(File file) throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = "";
        StringBuilder moduleContents = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            moduleContents.append(line + "\n");
        }
        reader.close();
        return moduleContents.toString();
    }

    @SuppressWarnings("serial")
    private void newModuleDir() {
        try {
            MarathonInputDialog mid = new MarathonInputDialog(this, "New Module Directory") {

                @Override
                protected String validateInput(String moduleDirName) {
                    String errorMessage = null;
                    if (moduleDirName.length() == 0 || moduleDirName.trim().isEmpty())
                        errorMessage = "Enter a valid folder name";
                    else if (moduleDirName.charAt(0) == ' ' || moduleDirName.charAt(moduleDirName.length() - 1) == ' ') {
                        errorMessage = "Module Directory Name cannot begin/end with a whitespace.";
                    }
                    return errorMessage;
                }

                @Override
                protected JButton createOKButton() {
                    return UIUtils.createOKButton();
                }

                @Override
                protected JButton createCancelButton() {
                    return UIUtils.createCancelButton();
                }

                @Override
                protected String getFieldLabel() {
                    return "&Module Directory: ";
                }
            };
            mid.setVisible(true);
            if (!mid.isOk())
                return;
            String moduleDirName = mid.getValue();
            if (moduleDirName == null || moduleDirName.trim().equals(""))
                return;
            File moduleDir = new File(new File(System.getProperty(Constants.PROP_PROJECT_DIR)), moduleDirName);
            if (moduleDir.exists()) {
                JOptionPane.showMessageDialog(this, "A directory with the given name already exits", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!moduleDir.mkdir())
                throw new IOException("Unable to create module folder: " + moduleDir);
            fileEventHandler.fireNewEvent(moduleDir, false);
            addModuleDirToMPF(moduleDirName);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Could not complete creation of module directory.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not complete creation of module directory.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    @SuppressWarnings("serial")
    private void newSuiteFile() {
        try {
            MarathonInputDialog mid = new MarathonInputDialog(this, "New Suite File") {

                @Override
                protected String validateInput(String inputText) {
                    return inputText.length() == 0 ? "Enter a valid suite file name" : null;
                }

                @Override
                protected JButton createOKButton() {
                    return UIUtils.createOKButton();
                }

                @Override
                protected JButton createCancelButton() {
                    return UIUtils.createCancelButton();
                }

                @Override
                protected String getFieldLabel() {
                    return "&Suite File(without extension): ";
                }
            };
            mid.setVisible(true);
            if (!mid.isOk())
                return;
            String suiteFileName = mid.getValue();
            if (suiteFileName == null || suiteFileName.trim().equals(""))
                return;
            File suiteFile = new File(new File(System.getProperty(Constants.PROP_SUITE_DIR)), suiteFileName + ".suite");
            if (suiteFile.exists()) {
                JOptionPane.showMessageDialog(this, "A suite file with the given name already exists", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!suiteFile.createNewFile())
                throw new IOException("Unable to create suite file: " + suiteFile);
            navigatorListener.fileCreated(suiteFile, true);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Could not complete creation of module directory.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not complete creation of module directory.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Adds the given directory as a module directory to Marathon Project File.
     * 
     * @param moduleDirName
     * @throws IOException
     */
    private void addModuleDirToMPF(String moduleDirName) throws IOException {
        String[] currentModuleDirs = Constants.getMarathonDirectoriesAsStringArray(Constants.PROP_MODULE_DIRS);
        StringBuilder sbr = new StringBuilder();
        for (int i = 0; i < currentModuleDirs.length; i++) {
            sbr.append(getProjectRelativeName(currentModuleDirs[i]) + ";");
        }
        sbr.append("%" + Constants.PROP_PROJECT_DIR + "%/" + moduleDirName);
        updateProjectFile(Constants.PROP_MODULE_DIRS, sbr.toString());

    }

    /**
     * Returns the name substituting the Marathon Project Directory in the given
     * string by appropriate global variable.
     * 
     * @param path
     * @return
     */
    private String getProjectRelativeName(String path) {
        String projDirPath = System.getProperty(Constants.PROP_PROJECT_DIR);
        int index = path.indexOf(projDirPath);
        if (index != 0)
            return path;
        String relativePath = path.replace(projDirPath, "%" + Constants.PROP_PROJECT_DIR + "%");
        return relativePath;
    }

    private void updateProjectFile(String property, String value) throws IOException {
        File projectFile = new File(System.getProperty(Constants.PROP_PROJECT_DIR), Constants.PROJECT_FILE);
        FileInputStream input = new FileInputStream(projectFile);
        Properties mpfProps = new Properties();
        mpfProps.load(input);
        input.close();
        mpfProps.put(property, value);
        FileOutputStream out = new FileOutputStream(projectFile);
        mpfProps.store(out, "Marathon Project File");
        out.close();
        MPFUtils.replaceEnviron(mpfProps);
        String sysModDirs = mpfProps.getProperty(property).replaceAll(";", File.pathSeparator);
        sysModDirs = sysModDirs.replaceAll("/", File.separator);

        System.setProperty(property, sysModDirs);
    }

    private String getModuleHeader(String functionName, String description) {
        return scriptModel.getModuleHeader(functionName, description);
    }

    public IEditor newFile(String script, File directory) {
        IEditor newEditor = createEditor(IEditorProvider.EditorType.OTHER);
        getFileHandler(newEditor).setCurrentDirectory(directory);
        getFileHandler(newEditor).clearCurrentFile();
        String newFileName = newFileNameGenerator.getNewFileName();
        newEditor.setText(script);
        newEditor.setMode(getFileHandler(newEditor).getMode(newFileName));
        newEditor.setData("filename", newFileName);
        newEditor.clearUndo();
        newEditor.setDirty(false);
        setCurrentEditorDockable((EditorDockable) newEditor.getData("dockable"));
        newEditor.setFocus();
        return newEditor;
    }

    private void setCurrentEditorDockable(EditorDockable editorDockable) {
        if (editorDockable == null) {
            editor = null;
        } else if (editor != null) {
            Dockable dockable = (Dockable) editor.getData("dockable");
            TabbedDockableContainer dockableContainer = DockingUtilities.findTabbedDockableContainer(dockable);
            int order = 1;
            if (dockableContainer != null) {
                order = dockableContainer.indexOfDockable(dockable) + 1;
            }
            DockableState dockableState = workspace.getDockableState(editorDockable);
            if (dockableState == null) {
                workspace.createTab(dockable, editorDockable, order, true);
            }
        } else {
            if (maximizedDockable != null) {
                DockableState dockableState = workspace.getDockableState(maximizedDockable);
                if (dockableState != null && dockableState.isMaximized())
                    workspace.restore(maximizedDockable);
            }
            DockableState dockableState = workspace.getDockableState(editorDockable);
            DockableState OPDockableState = workspace.getDockableState(outputPane);
            if (dockableState == null && OPDockableState != null && OPDockableState.isDocked())
                workspace.split(outputPane, editorDockable, DockingConstants.SPLIT_TOP, 0.8);
            else if (dockableState == null) {
                DockableState RDockableState = workspace.getDockableState(resultPane);
                if (RDockableState != null && RDockableState.isDocked())
                    workspace.split(resultPane, editorDockable, DockingConstants.SPLIT_TOP, 0.8);
                else
                    workspace.addDockable(editorDockable);
            }
        }
        if (editorDockable != null) {
            this.editor = editorDockable.getEditor();
        }
        updateView();
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.marathon.display.DisplayView#newFile()
     */
    public void newTestCaseFile() {
        String testHeader = getDefaultTestHeader();
        if (testHeader == null)
            return;
        newFile(testHeader, new File(System.getProperty(Constants.PROP_TEST_DIR)));
        final int line = scriptModel.getLinePositionForInsertion();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                editor.setCaretLine(line);
            }
        });
    }

    /**
     * Get the default test header.
     * 
     * @return header, the test header
     */
    private String getDefaultTestHeader() {
        File fixtureFile = new File(System.getProperty(Constants.PROP_FIXTURE_DIR), fixture + scriptModel.getSuffix());
        if (!fixtureFile.exists()) {
            JOptionPane.showMessageDialog(this, "Selected Fixture does not exists", "Invalid Fixture", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return scriptModel.getDefaultTestHeader(fixture);
    }

    /**
     * Create a new Fixture file.
     */
    public void newFixtureFile() {
        FixtureDialog fixtureDialog = new FixtureDialog(this, getFixtures());
        fixtureDialog.setVisible(true);

        if (fixtureDialog.isOk()) {
            newFile(getFixtureHeader(fixtureDialog.getProperties(), fixtureDialog.getSelectedLauncher()),
                    new File(System.getProperty(Constants.PROP_FIXTURE_DIR)));
            editor.setDirty(true);
            File fixtureFile = new File(System.getProperty(Constants.PROP_FIXTURE_DIR), fixtureDialog.getFixtureName()
                    + scriptModel.getSuffix());
            try {
                saveTo(fixtureFile);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Unable to save the fixture: " + e.getMessage(), "Invalid File",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            String ns = fixtureDialog.getProperties().getProperty(Constants.PROP_RECORDER_NAMINGSTRATEGY);
            if (ns != null && !ns.equals("")) {
                try {
                    Class<?> forName = Class.forName(ns + "Init");
                    Method method = forName.getMethod("initialize");
                    if (method != null)
                        method.invoke(null);
                } catch (Exception e) {
                }
            }
            setDefaultFixture(fixtureDialog.getFixtureName());
            navigator.refresh(new File[] { fixtureFile });
        }
    }

    /**
     * Get the fixture header for the current fixture.
     * 
     * @param props
     * @param launcher
     * 
     * @param arguments
     * @param className
     * @param string
     * 
     * @return
     */
    private String getFixtureHeader(Properties props, String launcher) {
        IRuntimeLauncherModel launcherModel = LauncherModelHelper.getLauncherModel(launcher);
        if (launcherModel == null)
            return "";
        return scriptModel.getDefaultFixtureHeader(props, launcher, launcherModel.getPropertyKeys());
    }

    void openFile(File file) {
        final EditorDockable dockable = findEditorDockable(file);
        if (dockable != null) {
            selectDockable(dockable);
            return;
        }
        IEditor openEditor = createEditor(file);
        if (openEditor != null) {
            setCurrentEditorDockable((EditorDockable) openEditor.getData("dockable"));
            openEditor.setFocus();
        }
    }

    private void selectDockable(final EditorDockable dockable) {
        TabbedDockableContainer container = DockingUtilities.findTabbedDockableContainer(dockable);
        if (container != null) {
            container.setSelectedDockable(dockable);
        }
        dockable.getEditor().setFocus();
        return;
    }

    private EditorDockable findEditorDockable(File file) {
        DockableState[] dockables = workspace.getDockables();
        for (DockableState dockableState : dockables) {
            if (dockableState.getDockable() instanceof EditorDockable) {
                EditorDockable editorDockable = (EditorDockable) dockableState.getDockable();
                File editorFile = ((FileHandler) editorDockable.getEditor().getData("filehandler")).getCurrentFile();
                if (editorFile != null && file.equals(editorFile))
                    return editorDockable;
            }
        }
        return null;
    }

    private EditorDockable findEditorDockable(String fileName) {
        DockableState[] dockables = workspace.getDockables();
        for (DockableState dockableState : dockables) {
            if (dockableState.getDockable() instanceof EditorDockable) {
                EditorDockable editorDockable = (EditorDockable) dockableState.getDockable();
                String name = (String) editorDockable.getEditor().getData("filename");
                if (fileName.equals(name))
                    return editorDockable;
            }
        }
        return null;
    }

    /**
     * Report the exception to the user.
     * 
     * @param e
     *            , exception to be reported.
     */
    private void reportException(Throwable e) {
        RuntimeLogger.getRuntimeLogger().error(MODULE, e.getMessage(), ExceptionUtil.getTrace(e));
        outputPane.append(e.getMessage(), IStdOut.STD_ERR);
    }

    private File saveAs() {
        File file = null;
        try {
            file = getFileHandler(editor).saveAs(editor.getText(), this, (String) editor.getData("filename"));
            if (file != null) {
                editor.setData("filename", getFileHandler(editor).getCurrentFile().getName());
                editor.setDirty(false);
                EditorDockable dockable = (EditorDockable) editor.getData("dockable");
                dockable.updateKey();
            }
            updateView();
        } catch (IOException e) {
            reportException(e);
        }
        return file;
    }

    /**
     * Save all unsaved buffers in the editor.
     */
    public void saveAll() {
        DockableState[] dockables = workspace.getDockables();
        for (DockableState dockableState : dockables) {
            Dockable dockable = dockableState.getDockable();
            if (dockable instanceof EditorDockable) {
                IEditor editor = ((EditorDockable) dockable).getEditor();
                if (editor.isDirty()) {
                    save(editor);
                }
            }
        }
        updateView();
    }

    private File save() {
        if (exploratoryTest) {
            String testName = runReportDir.getName() + scriptModel.getSuffix();
            File testDir = new File(System.getProperty(Constants.PROP_TEST_DIR), "ExploratoryTests");
            if (!testDir.exists()) {
                if (!testDir.mkdir())
                    throw new RuntimeException("Unable to create the test directory: " + testDir);
            }
            File file = new File(testDir, testName);
            try {
                saveTo(file);
                updateView();
            } catch (IOException e) {
                reportException(e);
            }
            return file;
        }
        return save(editor);
    }

    private File save(IEditor e) {
        File file = null;
        try {
            FileHandler fileHandler = getFileHandler(e);
            file = fileHandler.save(e.getText(), this, (String) e.getData("filename"));
            if (file != null) {
                e.setData("filename", fileHandler.getCurrentFile().getName());
                e.setDirty(false);
                if (isModuleFile()) {
                    scriptModel.fileUpdated(file, SCRIPT_FILE_TYPE.MODULE);
                }
                navigator.refresh(new File[] { file });
            }
            updateDockName(e);
            updateView();
            ((EditorDockable) e.getData("dockable")).updateKey();
            if (fileHandler.isModuleFile())
                resetModuleFunctions();
        } catch (IOException e1) {
            reportException(e1);
        }
        return file;
    }

    private void updateDockName(IEditor e) {
        String suffix = "";
        if (e.isDirty()) {
            suffix = "*";
        }
        String title = "";
        title = e.getData("filename") + suffix;
        Dockable dockable = (Dockable) e.getData("dockable");
        if (dockable != null) {
            DockKey dk = dockable.getDockKey();
            dk.setName(title);
        }
    }

    public void saveTo(File file) throws IOException {
        getFileHandler(editor).saveTo(file, editor.getText());
        if (file != null) {
            String name = getFileHandler(editor).getCurrentFile().getName();
            editor.setData("filename", name);
            editor.setDirty(false);
        }
    }

    /**
     * Save the current desktop preferences (layout) to user preferences.
     */

    public void handleAbout() {
        helpAboutAction.actionPerformed(null);
    }

    public void handlePreferences() {
        preferencesAction.actionPerformed(null);
    }

    public void showSearchDialog() {
        if (searchDialog == null) {
            searchDialog = new SearchDialog(editor, this);
        } else {
            searchDialog.setEditor(editor);
        }
        editor.showSearchDialog(searchDialog);
    }

    private boolean isMac() {
        return Boolean.getBoolean("marathon.useAppleMenuBar") && OSUtils.isMac();
    }

    private CheckList fillUpChecklist(final String fileName) {
        File file = new File(System.getProperty(Constants.PROP_CHECKLIST_DIR), fileName);
        return display.fillUpChecklist(testCase, file, this);
    }

    private boolean canCloseComponent(Dockable dockable) {
        if (dockable instanceof EditorDockable) {
            return closeEditor(((EditorDockable) dockable).getEditor());
        }
        return true;
    }

    private boolean closeEditor(IEditor e) {
        if (e == null)
            return true;
        if (e.isDirty()) {
            int shouldSaveFile = JOptionPane.showConfirmDialog(this, "File \"" + e.getData("filename")
                    + "\" Modified. Do you want to save the changes ", "File \"" + e.getData("filename") + "\" Modified",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (shouldSaveFile == JOptionPane.CANCEL_OPTION)
                return false;
            if (shouldSaveFile == JOptionPane.YES_OPTION) {
                File file = save(e);
                if (file == null) {
                    return false;
                }
                EditorDockable dockable = (EditorDockable) e.getData("dockable");
                dockable.updateKey();
            }
        }
        return true;
    }

    /**
     * Marathon Actions available from Menu/Toolbars
     */

    @ISimpleAction(mneumonic = 'p', description = "Play the testcase") AbstractSimpleAction playAction;

    @ISimpleAction(mneumonic = 'r', description = "Show report for last test run") Action showReportAction;

    @ISimpleAction(mneumonic = 'u', description = "Debug the testcase") AbstractSimpleAction debugAction;

    @ISimpleAction(mneumonic = 'y', description = "Play the testcase with a delay") AbstractSimpleAction slowPlayAction;

    @ISimpleAction(mneumonic = 'a', description = "Pause recording") Action pauseAction;

    @ISimpleAction(mneumonic = 'm', description = "Resume recording") Action resumeRecordingAction;

    @ISimpleAction(mneumonic = 'm', description = "Resume playing") Action resumePlayingAction;

    @ISimpleAction(mneumonic = 'f') Action selectFixtureAction;

    @ISimpleAction(mneumonic = 'r', description = "Start recording") Action recordAction;

    @ISimpleAction(value = "Exploratory Test", mneumonic = 'R', description = "Record an exploratory test") Action etAction;

    @ISimpleAction(mneumonic = 's', description = "Stop recording") Action stopAction;

    @ISimpleAction(mneumonic = 'r', description = "Start raw recording") Action rawRecordAction;

    @ISimpleAction(mneumonic = 'o', description = "Open Application") Action openApplicationAction;

    @ISimpleAction(mneumonic = 'c', description = "Close Application") Action closeApplicationAction;

    @ISimpleAction(mneumonic = 'i', description = "Insert a module method") Action insertScriptAction;

    @ISimpleAction(mneumonic = 'l', description = "Insert a checklist") Action insertChecklistAction;

    @ISimpleAction(mneumonic = 'e', description = "Change project settings", value = "Project Settings...") Action projectSettingsAction;

    @ISimpleAction(mneumonic = 'l', description = "Mangage checklists", value = "Manage Checklists...") Action manageChecklistsAction;

    @ISimpleAction(mneumonic = 'e', description = "Change settings for script console", value = "Script Console Settings...") Action scriptConsoleSettingsAction;

    @ISimpleAction(mneumonic = 'n', description = "Create a new testcase") Action newTestcaseAction;

    @ISimpleAction(mneumonic = 'c', description = "Create a new module method") Action newModuleAction;

    @ISimpleAction(mneumonic = 'f', description = "Create a new fixture") Action newFixtureAction;

    @ISimpleAction(mneumonic = 's', description = "Save current file") Action saveAction;

    @ISimpleAction(mneumonic = 'a', description = "Save as") Action saveAsAction;

    @ISimpleAction(mneumonic = 'l', description = "Save all modifications") Action saveAllAction;

    @ISimpleAction(mneumonic = 'x', description = "Exit Marathon") Action exitAction;

    @ISimpleAction(mneumonic = 'b', description = "About Marathon", value = "About...") Action helpAboutAction;

    @ISimpleAction(mneumonic = 'r', description = "Show release notes", value = "Read me") Action releaseNotes;

    @ISimpleAction(mneumonic = 'c', description = "Show change log") Action changeLog;

    @ISimpleAction(mneumonic = 'w', description = "Show marathon website", value = "Marathon on web") Action visitWebsite;

    @ISimpleAction(mneumonic = 'u', description = "Undo last edit") Action undoAction;

    @ISimpleAction(mneumonic = 'r', description = "Redo last undo") Action redoAction;

    @ISimpleAction(mneumonic = 't', description = "Cut selected text") Action cutAction;

    @ISimpleAction(mneumonic = 'c', description = "Copy selected text") Action copyAction;

    @ISimpleAction(mneumonic = 'p', description = "Paste") Action pasteAction;

    @ISimpleAction(mneumonic = 'f', description = "Search and replace", value = "Find & Replace...") Action searchAction;

    @ISimpleAction(mneumonic = 'n', description = "Find next") Action findNextAction;

    @ISimpleAction(mneumonic = 'p', description = "Find previous") Action findPreviousAction;

    @ISimpleAction(mneumonic = 'e', description = "Change preferences", value = "Preferences...") Action preferencesAction;

    @ISimpleAction(mneumonic = 'r', description = "Reset workspace to default") Action resetWorkspaceAction;

    @ISimpleAction(mneumonic = 'b', description = "Toggle breakpoint at the current line") Action toggleBreakpointAction;

    @ISimpleAction(mneumonic = 't', description = "Remove all breakpoints", value = "Remove all breakpoints") Action clearAllBreakpointsAction;

    @ISimpleAction(mneumonic = 't', description = "Step into the method") Action stepIntoAction;

    @ISimpleAction(mneumonic = 'o', description = "Step over the method") Action stepOverAction;

    @ISimpleAction(mneumonic = 'u', description = "Return from current method") Action stepReturnAction;

    @ISimpleAction(mneumonic = 'u', description = "Player console") Action playerConsoleAction;

    @ISimpleAction(mneumonic = 'u', description = "Recorder console") Action recorderConsoleAction;

    @ISimpleAction(mneumonic = 'u', description = "Create a new Module directory", value = "New Module Directory") Action newModuleDirAction;

    @ISimpleAction(mneumonic = 's', description = "Create a new Suite file", value = "New Suite File") Action newSuiteFileAction;

    private DockGroup editorDockGroup;

    private boolean resetWorkspaceOperation;

    private transient IConsole taConsole = new EditorConsole(displayView);

    public transient ILogger logViewLogger;

    private HashSet<String> importStatements;

    public static final ImageIcon BREAKPOINT = new ImageIcon(DisplayWindow.class.getClassLoader().getResource(
            "net/sourceforge/marathon/display/icons/enabled/togglebreakpoint.gif"));

    public void onPlay() {
        // createNewResultReporter();
        resultPane.clear();
        outputPane.clear();
        debugging = false;
        callStack.clear();
        displayView.getOutputPane().clear();
        display.play(taConsole);
    }

    public void onDebug() {
        resultPane.clear();
        outputPane.clear();
        breakStackDepth = -1;
        stepIntoActive = false;
        debugging = true;
        displayView.getOutputPane().clear();
        display.play(taConsole);
    }

    public void onSlowPlay() {
        String delay = System.getProperty(Constants.PROP_RUNTIME_DEFAULT_DELAY, "1000");
        if (delay.equals(""))
            delay = "1000";
        System.setProperty(Constants.PROP_RUNTIME_DELAY, delay);
        resultPane.clear();
        outputPane.clear();
        debugging = false;
        callStack.clear();
        displayView.getOutputPane().clear();
        display.play(taConsole);
    }

    public void onPause() {
        System.setProperty(Constants.PROP_RUNTIME_DELAY, "0");
        display.pauseRecording();
    }

    public void onResumeRecording() {
        resumePlay();
    }

    public void onResumePlaying() {
        resumePlay();
    }

    public void onRecord() {
        importStatements = new HashSet<String>();
        resultPane.clear();
        outputPane.clear();
        controller.clear();
        display.record(taConsole);
    }

    public void onEt() {
        exploratoryTest = true;
        etAction.setEnabled(false);
        newTestCaseFile();
        displayView.startTestRun();
        displayView.startTest();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                recordActionButton.doClick();
            }
        });
    }

    public void onStop() {
        display.stop();
        if (exploratoryTest) {
            displayView.endTest(null);
            displayView.endTestRun();
            File file = save();
            if (file != null)
                navigator.makeVisible(file);
            exploratoryTest = false;
        }
    }

    public void onRawRecord() {
        isRawRecording = !isRawRecording;
        rawRecordButton.setSelected(isRawRecording);
        display.setRawRecording(isRawRecording);
    }

    public void onOpenApplication() {
        resultPane.clear();
        outputPane.clear();
        display.openApplication(taConsole);
    }

    public void onCloseApplication() {
        display.closeApplication(true);
    }

    public void onInsertScript() {
        Module root = getModuleFunctions();
        FunctionDialog dialog = new FunctionDialog(DisplayWindow.this, root, display.getTopWindowName());
        dialog.setVisible(true);
    }

    public void onInsertChecklist() {
        String checklistDir = System.getProperty(Constants.PROP_CHECKLIST_DIR);
        File dir = new File(checklistDir);
        MarathonCheckList dialog;
        if (controller != null && controller.isShowing())
            dialog = new MarathonCheckList(controller, dir, true);
        else
            dialog = new MarathonCheckList(DisplayWindow.this, dir, true);
        dialog.setVisible(true);
        navigator.refresh(new File[] { dir });
        if (dialog.isOK()) {
            File selectedChecklist = dialog.getSelectedChecklist();
            insertChecklist(selectedChecklist.getName());
        }
    }

    public void onProjectSettings() {
        String projectDir = System.getProperty(Constants.PROP_PROJECT_DIR);
        MPFConfigurationUI ui = new MPFConfigurationUI(projectDir, DisplayWindow.this);
        ui.setVisible(true);
        Main.processMPF(projectDir);
        RuntimeLogger.setRuntimeLogger(logViewLogger);
    }

    public void onManageChecklists() {
        String projectDir = System.getProperty(Constants.PROP_CHECKLIST_DIR);
        File dir = new File(projectDir);
        MarathonCheckList dialog = new MarathonCheckList(DisplayWindow.this, dir, false);
        dialog.setVisible(true);
        navigator.refresh(new File[] { dir });
    }

    public void onScriptConsoleSettings() {
        URL defaultProperties = ScriptConsole.class.getResource("scriptconsole.props");
        PropertyEditor propertyEditor = new PropertyEditor(this, ScriptConsole.class, defaultProperties, "Script Console Settings");
        propertyEditor.setVisible(true);
    }

    public void onNewTestcase() {
        newTestCaseFile();
    }

    public void onNewModule() {
        newModuleFile();
    }

    public void onNewModuleDir() {
        newModuleDir();
    }

    public void onNewSuiteFile() {
        newSuiteFile();
    }
    
    public void onNewFixture() {
        newFixtureFile();
    }

    public void onSave() {
        File file = save();
        if (file != null) {
            navigator.makeVisible(file);
            testRunner.resetTestView();
        }
    }

    public void onSaveAs() {
        File file = saveAs();
        if (file != null) {
            if (isModuleFile()) {
                scriptModel.fileUpdated(file, SCRIPT_FILE_TYPE.MODULE);
            }
            navigator.makeVisible(file);
            testRunner.resetTestView();
        }
    }

    public void onSaveAll() {
        saveAll();
        navigator.refresh();
        testRunner.resetTestView();
    }

    public boolean handleQuit() {
        if (!saveFileIfNeeded())
            return false;
        saveBreakPoints();
        Preferences preferences = Preferences.userNodeForPackage(DisplayWindow.class);
        saveWorkspaceLayout(preferences);
        saveWindowState(preferences);
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void saveWindowState(Preferences preferences) {
        int extendedState = getExtendedState();
        int x = -1, y = -1, w = -1, h = -1;
        if ((extendedState & MAXIMIZED_BOTH) == 0) {
            Dimension size = getSize();
            Point location = getLocationOnScreen();
            x = location.x;
            y = location.y;
            w = size.width;
            h = size.height;
        }
        preferences.putInt(getPrefKey("window.x", System.getProperty(Constants.PROP_PROJECT_DIR)), x);
        preferences.putInt(getPrefKey("window.y", System.getProperty(Constants.PROP_PROJECT_DIR)), y);
        preferences.putInt(getPrefKey("window.w", System.getProperty(Constants.PROP_PROJECT_DIR)), w);
        preferences.putInt(getPrefKey("window.h", System.getProperty(Constants.PROP_PROJECT_DIR)), h);
    }

    private void saveWorkspaceLayout(Preferences preferences) {
        try {
            DockableState[] dockables = workspace.getDockables();
            for (DockableState dockableState : dockables) {
                if (dockableState.getDockable() instanceof EditorDockable && !dockableState.isDocked())
                    workspace.unregisterDockable(dockableState.getDockable());
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workspace.writeXML(baos);
            preferences.put(getPrefKey("workspace.", System.getProperty(Constants.PROP_PROJECT_DIR)),
                    new String(baos.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onExit() {
        if (handleQuit()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    System.exit(0);
                }
            });
        }
    }

    public void onHelpAbout() {
        aboutDialog.display();
    }

    public void onReleaseNotes() {
        showFile(System.getProperty(Constants.PROP_HOME), "README.txt");
    }

    public void onChangeLog() {
        showFile(System.getProperty(Constants.PROP_HOME), "ChangeLog");
    }

    public void onVisitWebsite() {
        try {
            browserLauncher.openURLinBrowser("http://www.marathontesting.com/");
        } catch (Exception e1) {
            displayView.setError(e1, "Could not launch the browser");
        }
    }

    public void onUndo() {
        editor.undo();
    }

    public void onRedo() {
        editor.redo();
    }

    public void onCut() {
        editor.cut();
    }

    public void onCopy() {
        editor.copy();
    }

    public void onPaste() {
        editor.paste();
    }

    public void onSearch() {
        showSearchDialog();
    }

    public void onFindNext() {
        editor.find(IEditor.FIND_NEXT);
    }

    public void onFindPrevious() {
        editor.find(IEditor.FIND_PREV);
    }

    public void onPreferences() {
        PreferencesDialog dialog = new PreferencesDialog(DisplayWindow.this);
        dialog.setVisible(true);
        if (!dialog.isNeedRefresh())
            return;
        navigator.refresh();
        testRunner.resetTestView();
        editor.refresh();
    }

    public void onResetWorkspace() {
        DockableState[] dockableStates = workspace.getDockables();
        List<EditorDockable> editorDockables = new ArrayList<DisplayWindow.EditorDockable>();
        for (DockableState dockableState : dockableStates) {
            if (dockableState.getDockable() instanceof EditorDockable)
                editorDockables.add((EditorDockable) dockableState.getDockable());
        }
        resetWorkspaceOperation = true;
        createDefaultWorkspace(editorDockables.toArray(new EditorDockable[editorDockables.size()]));
        resetWorkspaceOperation = false;
    }

    public void onToggleBreakpoint() {
        if (isProjectFile() && editor != null)
            toggleBreakPoint(editor.getCaretLine());
    }

    public void onClearAllBreakpoints() {
        breakpoints.clear();
        setState();
        editor.refresh();
    }

    public void onStepInto() {
        stepIntoActive = true;
        resumePlay();
    }

    public void onStepOver() {
        breakStackDepth = callStack.getStackDepth();
        resumePlay();
    }

    public void onStepReturn() {
        breakStackDepth = callStack.getStackDepth() - 1;
        if (breakStackDepth < 0)
            breakStackDepth = 0;
        resumePlay();
    }

    public void onPlayerConsole() {
        scriptConsole = new ScriptConsole(DisplayWindow.this, ((Component) editor).getFont(), scriptConsoleListener,
                scriptModel.getSuffix());
        scriptConsole.setVisible(true);
        setState();
    }

    public void onRecorderConsole() {
        scriptConsole = new ScriptConsole(controller.isVisible() ? null : DisplayWindow.this, ((Component) editor).getFont(),
                scriptConsoleListener, scriptModel.getSuffix());
        scriptConsole.setVisible(true);
        System.setProperty(Constants.PROP_RUNTIME_DELAY, "0");
        if (!state.isRecordingPaused())
            display.pauseRecording();
        setState();
    }

    public void setResultReporterHTMLFile(File resultReporterHTMLFile) {
        this.resultReporterHTMLFile = resultReporterHTMLFile;
    }

    public String getFixture() {
        return fixture;
    }

    public void setDebugging(boolean debugging) {
        this.debugging = debugging;
    }

    private boolean isBreakPointAtLine(int line) {
        BreakPoint bp = new BreakPoint(displayView.getFilePath(), line);
        return breakpoints != null && breakpoints.contains(bp);
    }

    private IEditor createEditor(EditorType editorType) {
        try {
            FileHandler fileHandler = new FileHandler(new MarathonFileFilter(scriptModel.getSuffix(), scriptModel), new File(
                    System.getProperty(Constants.PROP_TEST_DIR)), new File(System.getProperty(Constants.PROP_FIXTURE_DIR)),
                    Constants.getMarathonDirectories(Constants.PROP_MODULE_DIRS), this);
            IEditor e = editorProvider.get(true, 1, editorType);
            e.setData("filehandler", fileHandler);
            e.addGutterListener(gutterListener);
            e.addContentChangeListener(contentChangeListener);
            e.addCaretListener(caretListener);
            setAcceleratorKeys(e);
            setMenuItems(e);
            e.setStatusBar(statusPanel);
            Dockable editorDockable = new EditorDockable(e, editorDockGroup, workspace);
            e.setData("dockable", editorDockable);
            return e;
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    Action refreshAction = new AbstractAction("Refresh") {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
            if(editor != null) {
                File currentFile = getFileHandler(editor).getCurrentFile();
                try {
                    String script = getFileHandler(editor).readFile(currentFile);
                    editor.setText(script);
                    editor.setDirty(false);
                } catch (IOException e1) {
                }
            }
        }
    };;

    private void setMenuItems(IEditor e) {
        e.setMenuItems(new JMenuItem[] { getMenuItemWithAccelKey(undoAction, "^+Z"), getMenuItemWithAccelKey(redoAction, "^S+Z"),
                null, getMenuItemWithAccelKey(playAction, "^+P"), getMenuItemWithAccelKey(slowPlayAction, "^S+P"),
                getMenuItemWithAccelKey(debugAction, "^A+P"), null, getMenuItemWithAccelKey(cutAction, "^+X"),
                getMenuItemWithAccelKey(copyAction, "^+C"), getMenuItemWithAccelKey(pasteAction, "^+V"),
                getMenuItemWithAccelKey(refreshAction, "F5")});
    }

    private IEditor createEditor(File file) {
        try {
            EditorType editorType;
            if (file.getName().endsWith(".csv")) {
                editorType = IEditorProvider.EditorType.CSV;
            } else if (file.getName().endsWith(".suite")) {
                editorType = IEditorProvider.EditorType.SUITE;
            } else {
                editorType = IEditorProvider.EditorType.OTHER;
            }
            IEditor e = createEditor(editorType);
            String script = getFileHandler(e).readFile(file);
            if (script != null) {
                String name = getFileHandler(e).getCurrentFile().getName();
                e.setText(script);
                e.setMode(getFileHandler(e).getMode(name));
                e.setData("filename", name);
                e.setCaretLine(0);
                e.setDirty(false);
            }
            e.clearUndo();
            return e;
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }

    public void setResultReporter(MarathonResultReporter resultReporter) {
        this.resultReporter = resultReporter;
    }

    public MarathonResultReporter getResultReporter() {
        return resultReporter;
    }

    public void setGenerateReports(boolean b) {
        generateReportsMenuItem.setSelected(true);
    }

    private FileHandler getFileHandler(IEditor editor) {
        return (FileHandler) editor.getData("filehandler");
    }

    /**
     * Title for the new files created.
     */
    public static final String NEW_FILE = "Untitled";

    private static class NewFileNameGenerator {
        public int newFileCount = 0;

        public String getNewFileName() {
            if (newFileCount == 0) {
                newFileCount++;
                return NEW_FILE;
            }
            return NEW_FILE + newFileCount++;
        }

    }

    private NewFileNameGenerator newFileNameGenerator = new NewFileNameGenerator();

    private TabbedDockableContainer lastClosedEditorDockableContainer;

    private Dockable maximizedDockable;

    private MarathonResultReporter resultReporter;

    private Module moduleFunctions;

    private FileEventHandler fileEventHandler;

    /** Caret listener **/
    public void caretUpdate(CaretEvent e) {
        updateEditActions();
    }

    /** Listener for test runner */
    public void testFinished() {
        navigator.refresh();
    }

    public void testStarted() {
    }

    public void updateScript(String script) {
        editor.setText(script);
    }

    public void insertScript(String function) {
        String s = display.insertScript(function);
        importStatements.add(s);
    }

    public void fileUpdated(File selectedFile) {
        navigatorListener.fileUpdated(selectedFile);
    }

    public IEditorProvider getEditorProvider() {
        return editorProvider;
    }

    private Module getModuleFunctions() {
        if (moduleFunctions == null || (moduleFunctions.getChildren().size() == 0 && moduleFunctions.getFunctions().size() == 0))
            moduleFunctions = display.getModuleFuctions();
        return moduleFunctions;
    }

    public Module refreshModuleFunctions() {
        moduleFunctions = null;
        return getModuleFunctions();
    }

    public void resetModuleFunctions() {
        moduleFunctions = null;
    }

    public void preferenceChange(PreferenceChangeEvent evt) {
        String key = evt.getKey();
        if ("editor.tabconversion".equals(key) || "editor.tabsize".equals(key))
            Indent.setDefaultIndent(editorProvider.getTabConversion(), editorProvider.getTabSize());
    }

    public boolean canAppend(File file) {
        EditorDockable dockable = findEditorDockable(file);
        if (dockable == null)
            return true;
        if (!dockable.getEditor().isDirty())
            return true;
        int option = JOptionPane.showConfirmDialog(this, "File " + file.getName() + " being edited. Do you want to save the file?",
                "Save Module", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            save(dockable.getEditor());
            return true;
        }
        return false;
    }

    public boolean okToOverwrite(File file) {
        EditorDockable dockable = findEditorDockable(file);
        if (dockable == null)
            return true;
        JOptionPane.showMessageDialog(this, "The selected file is being edited in another editor. Close it and try");
        return false;
    }

    public boolean isModuleFile() {
        return editor != null && getFileHandler(editor).isModuleFile();
    }

    public FileEventHandler getFileEventHandler() {
        return fileEventHandler;
    }

    public void onOMapCreation() {
        display.omapCreate(taConsole);
    }
    
    public StatusBar getStatusPanel() {
        return statusPanel;
    }
}
