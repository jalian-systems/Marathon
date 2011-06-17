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

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.List;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.IConsole;
import net.sourceforge.marathon.api.IPlaybackListener;
import net.sourceforge.marathon.api.PlaybackResult;
import net.sourceforge.marathon.api.ScriptModelClientPart;
import net.sourceforge.marathon.api.SourceLine;
import net.sourceforge.marathon.editor.IEditor;
import net.sourceforge.marathon.editor.rsta.RSTAEditor;
import net.sourceforge.marathon.util.FileHandler;
import net.sourceforge.marathon.util.Indent;

public class MockDisplayView implements IDisplayView {
    boolean playEnabled = false;
    boolean recordEnabled = false;
    boolean stopEnabled = false;
    String error;
    String title;
    TextAreaOutput outputPane = new TextAreaOutput();
    State state = State.STOPPED_WITH_APP_CLOSED;
    private IEditor editor;

    public MockDisplayView() throws IOException {
        FileHandler fileHandler = new FileHandler(new MarathonFileFilter(".py", ScriptModelClientPart.getModel()), new File(
                System.getProperty(Constants.PROP_TEST_DIR)), new File(System.getProperty(Constants.PROP_FIXTURE_DIR)),
                Constants.getMarathonDirectories(Constants.PROP_MODULE_DIRS), null);
        editor = new RSTAEditor(true, 1);
        editor.setData("filehandler", fileHandler);
    }

    public IEditor getEditor() {
        return editor;
    }

    public void setError(Throwable exception, String message) {
        error = message;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setState(State state) {
        this.state = state;
    }

    public IStdOut getOutputPane() {
        return outputPane;
    }

    public void setResult(PlaybackResult result) {
    }

    public void goToFile(String file, int lineNumber) {
    }

    public void updateView() {
        title = "Marathon - " + editor.getData("filename");
    }

    public void newFile() {
        newFile(editor, getDefaultTestHeader(), new File(System.getProperty(Constants.PROP_TEST_DIR)));
        updateView();
    }

    public void newFile(IEditor editor, String script, File directory) {
        FileHandler fileHandler = (FileHandler) editor.getData("filehandler");
        fileHandler.setCurrentDirectory(directory);
        fileHandler.clearCurrentFile();
        String newFileName = "Untitled1";
        editor.setText(script);
        editor.setMode(fileHandler.getMode(newFileName));
        editor.setData("filename", newFileName);
        editor.clearUndo();
    }

    private String getDefaultTestHeader() {
        return "#{{{ Marathon\nfrom default import *\n#}}}\n\ndef test():\n";
    }

    public void openFile(File file) {
        try {
            openFile(editor, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateView();
    }

    public void openFile(IEditor editor, File file) throws IOException {
        FileHandler fileHandler = (FileHandler) editor.getData("filehandler");
        String script = fileHandler.readFile(file);
        if (script != null) {
            editor.setText(script);
            editor.setMode(fileHandler.getMode(fileHandler.getCurrentFile().getName()));
            editor.setData("filename", fileHandler.getCurrentFile().getName());
            editor.setCaretLine(0);
        }
        editor.clearUndo();
    }


    public File save() {
        try {
            FileHandler fileHandler = (FileHandler) editor.getData("filehandler");
            File file = fileHandler.save(editor.getText(), editor.getComponent(), "");
            if (file != null) {
                editor.clearUndo();
                editor.setData("filename", fileHandler.getCurrentFile().getName());
            }
            updateView();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public File saveAs() {
        File file = null;
        try {
            FileHandler fileHandler = (FileHandler) editor.getData("filehandler");
            file = fileHandler.saveAs(editor.getText(), editor.getComponent(), "");
            if (file != null) {
                editor.clearUndo();
                editor.setData("filename", fileHandler.getCurrentFile().getName());
            }
            updateView();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public void trackProgress(SourceLine line) {
        if (getFilePath().equals(line.fileName))
            editor.highlightLine(line.lineNumber);
    }

    public String getScript() {
        return editor.getText() + "\n" + Indent.getIndent() + "pass\n";
    }

    public String getFilePath() {
        FileHandler fileHandler = (FileHandler) editor.getData("filehandler");
        if (fileHandler.getCurrentFile() == null)
            return (String) editor.getData("filename");
        try {
            return fileHandler.getCurrentFile().getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void insertScript(String script) {
        editor.insertScript(script);
    }

    public void trackProgress() {
        editor.highlightLine(0);
    }

    public void startInserting() {
        editor.startInserting();
    }

    public void stopInserting() {
        editor.stopInserting();
    }

    public List<BreakPoint> getBreakpoints() {
        return null;
    }

    public int trackProgress(SourceLine line, int line_reached) {
        if (line.fileName.equals(getFilePath()))
            editor.highlightLine(line.lineNumber - 1);
        return IPlaybackListener.CONTINUE;
    }

    public boolean isDebugging() {
        return true;
    }

    public int acceptChecklist(String fileName) {
        return 0;
    }

    public int showChecklist(String fileName) {
        return 0;
    }

    public Component getWindow() {
        return null;
    }

    public void insertChecklistAction(String name) {
    }

    public void removeComponent(Component component) {
    }

    public void showSearchDialog() {
    }

    public IConsole getConsole() {
        return new EditorConsole(this);
    }

    public void endTestRun() {
        // TODO Auto-generated method stub
        
    }

    public void endTest(PlaybackResult result) {
        // TODO Auto-generated method stub
        
    }

    public void startTestRun() {
        // TODO Auto-generated method stub
        
    }

    public void startTest() {
        // TODO Auto-generated method stub
        
    }

    public void addImport(String ims) {
        // TODO Auto-generated method stub
        
    }

    public void updateOMapFile() {
        // TODO Auto-generated method stub
        
    }

}
