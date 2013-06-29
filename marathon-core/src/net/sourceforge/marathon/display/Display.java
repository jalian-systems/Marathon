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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.Constants.MarathonMode;
import net.sourceforge.marathon.api.ApplicationLaunchException;
import net.sourceforge.marathon.api.IConsole;
import net.sourceforge.marathon.api.IMarathonRuntime;
import net.sourceforge.marathon.api.IPlaybackListener;
import net.sourceforge.marathon.api.IPlayer;
import net.sourceforge.marathon.api.IRecorder;
import net.sourceforge.marathon.api.IRuntimeFactory;
import net.sourceforge.marathon.api.IRuntimeLauncherModel;
import net.sourceforge.marathon.api.IScript;
import net.sourceforge.marathon.api.IScriptElement;
import net.sourceforge.marathon.api.MarathonRuntimeException;
import net.sourceforge.marathon.api.PlaybackResult;
import net.sourceforge.marathon.api.ScriptException;
import net.sourceforge.marathon.api.ScriptModelClientPart;
import net.sourceforge.marathon.api.SourceLine;
import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.api.module.Module;
import net.sourceforge.marathon.checklist.CheckList;
import net.sourceforge.marathon.junit.DDTestRunner;
import net.sourceforge.marathon.junit.MarathonTestCase;
import net.sourceforge.marathon.providers.PlaybackResultProvider;
import net.sourceforge.marathon.providers.RecorderProvider;
import net.sourceforge.marathon.recorder.IScriptListener;
import net.sourceforge.marathon.util.LauncherModelHelper;

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;

public class Display implements IPlaybackListener, IScriptListener, IExceptionReporter {

    public static final class DummyRecorder implements IRecorder {
        public void record(IScriptElement element) {
        }

        public void abortRecording() {
        }

        public void insertChecklist(String name) {
        }

        public String recordInsertScriptElement(WindowId windowId, String script) {
            return null;
        }

        public void recordInsertChecklistElement(WindowId windowId, String fileName) {
        }

        public void recordShowChecklistElement(WindowId windowId, String fileName) {
        }

        public boolean isCreatingObjectMap() {
            return true;
        }

        public void updateScript() {
        }
    }

    @Retention(RetentionPolicy.RUNTIME) @BindingAnnotation public @interface IDisplayProperties {
    }

    public static final int LINE_REACHED = 1;
    public static final int METHOD_RETURNED = 2;
    public static final int METHOD_CALLED = 3;
    private static final Logger logger = Logger.getLogger(Display.class.getName());

    private @Inject IRuntimeFactory runtimeFactory;
    private @Inject RecorderProvider recorderProvider;
    private @Inject PlaybackResultProvider playbackResultProvider;

    private IMarathonRuntime runtime;
    private IPlayer player;
    private IDisplayView displayView;
    private State state = State.STOPPED_WITH_APP_CLOSED;
    protected boolean shouldClose = true;
    private IRecorder recorder;
    private String fixture;
    private IScript script;
    private boolean acceptingChecklists;
    private DDTestRunner ddTestRunner;
    private boolean autShutdown = false;
    private boolean playbackStopped;
    private boolean reuseFixture;
    private boolean ignoreReuse = false;

    public Display() {
    }

    public void setView(IDisplayView pView) {
        recorderProvider.setScriptListener(this);
        this.displayView = pView;
        setState(State.STOPPED_WITH_APP_CLOSED);
    }

    public void destroy() {
    }

    public DDTestRunner getDDTestRunner() {
        return ddTestRunner;
    }

    public void play(IConsole console) {
        try {
            String scriptText = displayView.getScript();
            if (!validTestCase(scriptText)) {
                reportException(new Exception("No test() function or fixture found in the script"));
                return;
            }
            try {
                playbackStopped = false;
                ddTestRunner = new DDTestRunner(console, scriptText);
            } catch (Exception e) {
                reportException(new Exception(e.getMessage()));
                return;
            }
            if (ddTestRunner.hasNext()) {
                ddTestRunner.next();
                displayView.startTestRun();
                runTest();
                return;
            }
        } catch (Throwable t) {
            reportException(t);
            destroyRuntime();
            return;
        }
        reportException(new Exception("No test() function or fixture found in the script"));
    }

    private void runTest() {
        if (ddTestRunner == null)
            return;
        displayView.startTest();
        createRuntime(ddTestRunner.getScriptText(), ddTestRunner.getConsole(), MarathonMode.OTHER);
        script = runtime.createScript(ddTestRunner.getScriptText(), displayView.getFilePath(), false, true);
        script.setDataVariables(ddTestRunner.getDataVariables());
        player = script.getPlayer(this, playbackResultProvider.get());
        player.setAcceptCheckList(acceptingChecklists);
        boolean shouldRunFixture = state.isStoppedWithAppClosed();
        setState(State.PLAYING);
        displayView.startInserting();
        player.play(shouldRunFixture);
    }

    private boolean validTestCase(String scriptText) {
        BufferedReader br = new BufferedReader(new StringReader(scriptText));
        String line;
        boolean testFound = false;
        boolean fixtureFound = false;
        try {
            while ((line = br.readLine()) != null) {
                if (line.matches("^def.*test.*().*"))
                    testFound = true;
                if (line.matches("^#\\{\\{\\{ Marathon"))
                    fixtureFound = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (testFound && fixtureFound)
            return true;
        return false;
    }

    public void showResult(PlaybackResult result) {
        if (result.failureCount() == 0) {
            shouldClose = !reuseFixture ;
            displayView.trackProgress();
            ignoreReuse = false ;
        } else {
            ignoreReuse = true ;
            shouldClose = false;
        }
        stopApplicationIfNecessary();
        showResults(result);
    }

    protected void showResults(PlaybackResult result) {
        displayView.setResult(result);
    }

    public void record(IConsole console) {
        String scriptText = displayView.getScript();
        if (!validTestCase(scriptText)) {
            scriptText = getFixtureHeader() + scriptText;
        }
        try {
            createRuntime(scriptText, console, MarathonMode.RECORDING);
            displayView.startInserting();
            runtime.createScript(scriptText, displayView.getFilePath(), false, true);
            recorder = recorderProvider.get();
            startApplicationIfNecessary();
            runtime.startRecording(recorder);
            setState(State.RECORDING);
        } catch (Throwable e) {
            setState(State.STOPPED_WITH_APP_CLOSED);
            destroyRuntime();
            displayView.stopInserting();
            stopApplicationIfNecessary();
            reportException(e);
        }
    }

    private String getFixtureHeader() {
        return ScriptModelClientPart.getModel().getFixtureHeader(fixture);
    }

    public void resume() {
        if (state.isRecordingPaused()) {
            runtime.startRecording(recorder);
            setState(State.RECORDING);
        } else {
            script.getDebugger().resume();
            setState(State.PLAYING);
        }
    }

    private void startApplicationIfNecessary() {
        if (state.isStoppedWithAppClosed()) {
            runtime.startApplication();
        }
    }

    public void stop() {
        if (state.isRecording()) {
            try {
                runtime.stopRecording();
            } catch (MarathonRuntimeException e) {
                setState(State.STOPPED_WITH_APP_CLOSED);
                destroyRuntime();
                throw e;
            } finally {
                displayView.stopInserting();
            }
            stopApplicationIfNecessary();
            displayView.updateOMapFile();
        } else if (state.isPlaying()) {
            try {
                player.halt();
            } catch (MarathonRuntimeException e) {
                reportException(e);
            } finally {
                playbackStopped = true;
                playbackFinished(playbackResultProvider.get(), false);
            }
        } else {
            throw new IllegalStateException("must be recording or playing to stop, not '" + state + "'");
        }
    }

    public void pauseRecording() {
        if (state.isRecording()) {
            try {
                runtime.stopRecording();
                setState(State.RECORDINGPAUSED);
            } catch (MarathonRuntimeException e) {
                setState(State.STOPPED_WITH_APP_CLOSED);
                destroyRuntime();
                throw e;
            }
        } else {
            throw new IllegalStateException("must be recording for the pause to happen");
        }
    }

    protected void stopApplicationIfNecessary() {
        boolean closeApplicationNeeded = state.isPlaying() == false;
        if (autShutdown) {
            setState(State.STOPPED_WITH_APP_CLOSED);
            runtime = null;
        } else
            setState(State.STOPPED_WITH_APP_OPEN);
        if (shouldClose)
            closeApplication(closeApplicationNeeded);
    }

    public void openApplication(IConsole console) {
        createRuntime(getFixtureHeader(), console, MarathonMode.RECORDING);
        runtime.createScript(getFixtureHeader(), "", false, false);
        startApplicationIfNecessary();
        setState(State.STOPPED_WITH_APP_OPEN);
        shouldClose = false;
    }

    public void closeApplication(boolean closeApplicationNeeded) throws RuntimeException {
        /*
         * We need to actually call the stopApplication that calls the teardown
         * on the fixture. However, the fixture that created the application (by
         * using setup) is already lost and we can't communicate with the app
         * using fixture when manually starting the application. For making this
         * work, we need changes in the semantics of the Script and Runtime
         */
        try {
            if (closeApplicationNeeded && runtime != null && !autShutdown)
                runtime.stopApplication();
        } catch (Exception e) {
            displayView.setError(e, "Application Under Test Aborted");
        } finally {
            destroyRuntime();
            shouldClose = true;
            setState(State.STOPPED_WITH_APP_CLOSED);
        }
    }

    private void destroyRuntime() {
        if (runtime != null) {
            logger.info("Destroying VM. autShutdown = " + autShutdown);
            try {
                if (!autShutdown)
                    runtime.destroy();
            } finally {
                runtime = null;
            }
        }
    }

    public State getState() {
        return state;
    }

    public void setState(State pState) {
        state = pState;
        displayView.setState(state);
    }

    private void createRuntime(String scriptText, IConsole console, MarathonMode mode) {
        IRuntimeFactory rf = getRuntimeFactory(scriptText);
        if (runtime == null || !reuseFixture || ignoreReuse) {
            if(runtime != null) {
                closeApplication(true);
            }
            runtime = rf.createRuntime(mode, scriptText, console);
        }
        assert (runtime != null);
        this.autShutdown = false;
    }

    public Module getModuleFuctions() {
        if (runtime == null)
            return null;
        return runtime.getModuleFunctions();
    }

    public String insertScript(String function) {
        WindowId topWindowId = runtime.getTopWindowId();
        runtime.insertScript(function);
        String s = recorder.recordInsertScriptElement(topWindowId, function);
        return s;
    }

    public boolean canOpenFile() {
        return (state == State.STOPPED_WITH_APP_CLOSED || state == State.STOPPED_WITH_APP_OPEN);
    }

    public void setDefaultFixture(String pFixture) {
        fixture = pFixture;
    }

    public void setRawRecording(boolean selected) {
        runtime.setRawRecording(selected);
    }

    public void pausePlay() {
        if (state.isPlaying()) {
            displayView.setState(State.PLAYINGPAUSED);
        }
    }

    public String evaluateScript(String code) {
        if (state.isRecordingPaused()) {
            return runtime.evaluate(code);
        } else {
            return script.getDebugger().evaluateScriptWhenPaused(code);
        }
    }

    public void insertChecklist(String name) {
        recorder.recordInsertChecklistElement(runtime.getTopWindowId(), name);
    }

    public File getScreenCapture() {
        return runtime.getScreenCapture();
    }

    public void setAcceptChecklist(boolean selected) {
        this.acceptingChecklists = selected;
    }

    public void recordShowChecklist(String fileName) {
        recorder.recordShowChecklistElement(runtime.getTopWindowId(), fileName);
    }

    public String getTopWindowName() {
        if (runtime == null || runtime.getTopWindowId() == null)
            return null;
        return runtime.getTopWindowId().getTitle();
    }

    /* Implementation of IPlaybackListener * */
    public void playbackFinished(final PlaybackResult result, boolean shutdown) {
        this.autShutdown = shutdown;
        displayView.endTest(result);
        if (ddTestRunner != null && ddTestRunner.hasNext() && !playbackStopped) {
            ddTestRunner.next();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (result.failureCount() == 0) {
                        shouldClose = !reuseFixture ;
                        displayView.trackProgress();
                        ignoreReuse = false ;
                    } else {
                        ignoreReuse = true ;
                        shouldClose = false;
                    }
                    stopApplicationIfNecessary();
                    runTest();
                }
            });
            return;
        }
        displayView.endTestRun();
        displayView.stopInserting();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showResult(result);
            }
        });
        ddTestRunner = null;
    }

    public int lineReached(SourceLine line) {
        return displayView.trackProgress(line, LINE_REACHED);
    }

    public int methodReturned(SourceLine line) {
        return displayView.trackProgress(line, METHOD_RETURNED);
    }

    public int methodCalled(SourceLine line) {
        return displayView.trackProgress(line, METHOD_CALLED);
    }

    public int acceptChecklist(String fileName) {
        return displayView.acceptChecklist(fileName);
    }

    public int showChecklist(String fileName) {
        return displayView.showChecklist(fileName);
    }

    /** Implementation of IScriptListener **/
    public void setScript(String script) {
        displayView.insertScript(script);
    }

    public void abortRecording() {
        if (state.isRecording()) {
            displayView.stopInserting();
        }
        shouldClose = true;
        runtime = null;
        displayView.updateOMapFile();
        setState(State.STOPPED_WITH_APP_CLOSED);
    }

    public void insertChecklistAction(String name) {
        displayView.insertChecklistAction(name);
    }

    /** Implementation IExceptionReporter **/
    public void reportException(Throwable e) {
        if (e instanceof ApplicationLaunchException)
            destroyRuntime();
        displayView.setError(e,
                e.getClass().getName().substring(e.getClass().getName().lastIndexOf('.') + 1) + " : " + e.getMessage());
    }

    public void addImportStatement(String ims) {
        displayView.addImport(ims);
    }

    public IRuntimeFactory getRuntimeFactory(String scriptText) {
        Map<String, Object> fixtureProperties = ScriptModelClientPart.getModel().getFixtureProperties(scriptText);
        if (fixtureProperties == null || fixtureProperties.size() == 0)
            return runtimeFactory;
        reuseFixture = Boolean.valueOf((String) fixtureProperties.get(Constants.FIXTURE_REUSE));
        String launcherModel = (String) fixtureProperties.get(Constants.PROP_PROJECT_LAUNCHER_MODEL);
        IRuntimeLauncherModel lm = LauncherModelHelper.getLauncherModel(launcherModel);
        if (lm == null)
            return runtimeFactory;
        return lm.getRuntimeFactory();
    }

    public CheckList fillUpChecklist(MarathonTestCase testCase, File file, JFrame view) {
        return testCase.showAndEnterChecklist(file, runtime, view);
    }

    public void omapCreate(IConsole console) {
        try {
            createRuntime(getFixtureHeader(), console, MarathonMode.RECORDING);
            runtime.createScript(getFixtureHeader(), "Objectmap Creation", false, true);
            startApplicationIfNecessary();
            runtime.startRecording(new DummyRecorder());
            setState(State.STOPPED_WITH_APP_OPEN);
        } catch (ScriptException e) {
            setState(State.STOPPED_WITH_APP_CLOSED);
            destroyRuntime();
            stopApplicationIfNecessary();
            reportException(e);
        }
    }

}
