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
package net.sourceforge.marathon.runtime;

import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.marathon.api.IMarathonRuntime;
import net.sourceforge.marathon.api.IRecorder;
import net.sourceforge.marathon.api.IScript;
import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.api.module.Module;

public class RuntimeStub implements IMarathonRuntime {
    public boolean scriptsFail;
    public String lastContent;
    public String lastFilename;
    public boolean isRecording;
    public ScriptStub scriptStub;
    public boolean destroyed;

    public IScript createScript(String content, String filename, boolean isRecording, boolean isDebugging) {
        this.lastContent = content;
        this.lastFilename = filename;
        scriptStub = new ScriptStub(scriptsFail);
        return scriptStub;
    }

    public IScript createScript(String content, String filename, boolean isDebugging) {
        return createScript(content, filename, false, isDebugging);
    }

    public void startRecording(IRecorder recorder) {
        isRecording = true;
    }

    public void stopRecording() {
        isRecording = false;
    }

    public void destroy() {
        destroyed = true;
    }

    public void startApplication() {
    }

    public void stopApplication() {
    }

    public Module getModuleFunctions() {
        return null;
    }

    public String[][] getArgumentsFor(DefaultMutableTreeNode node) {
        return null;
    }

    public String getFunctionDocumentation(DefaultMutableTreeNode node) {
        return null;
    }

    public void exec(String function) {
    }

    public void insertScript(String function) {
    }

    public void setRawRecording(boolean selected) {
    }

    public String evaluate(String code) {
        return null;
    }

    public WindowId getTopWindowId() {
        return null;
    }

    public File getScreenCapture() {
        return null;
    }

    public boolean isCustomAssertionsAvailable() {
        return false;
    }

    public String[][] getCustomAssertions(Object mcomponent) {
        return new String[0][0];
    }
}
