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
package net.sourceforge.marathon.api;

import java.io.File;

import net.sourceforge.marathon.api.module.Module;

/**
 * The operating environment of the application under test. In java terms this
 * corresponds roughly to the a java virtual machine
 */
public interface IMarathonRuntime {
    /**
     * creates a byte-compiled script inside the runtime. The result is
     * guaranteed to have been at least syntax level validated
     * 
     * @param content
     *            - the contents of the script to create
     * @param filename
     *            - filename of the script, used for generating debug output and
     *            stack traces.
     * @return - a Script which
     */
    IScript createScript(String scriptText, String filePath, boolean isRecording, boolean isDebugging);

    /**
     * begin capturing system events that are happening inside this runtime, and
     * send them to the recorder
     * 
     * @param recorder
     *            - the objects to which system events will be sent.
     */
    void startRecording(IRecorder recorder);

    /**
     * if events were being recorded, they will no longer be recorded after this
     * method is called.
     */
    void stopRecording();

    /**
     * Start the application
     */
    void startApplication();

    /**
     * Stop the application
     */
    void stopApplication();

    /**
     * kill this runtime. it will no longer be available to create scripts after
     * this method has been called
     */
    void destroy();

    /**
     * Get the available Module functions
     * 
     * @return the list of Module functions in a tree structure
     */
    Module getModuleFunctions();

    /**
     * Execute the given function. The function is expected to be a valid
     * command string
     * 
     * @param function
     *            the command to execute
     */
    void exec(String function);

    /**
     * Set the raw recording mode
     * 
     * @param selected
     */
    void setRawRecording(boolean selected);

    /**
     * Evaluate the expression and return the result
     * 
     * @param code
     * @return evaluated expression value
     */
    String evaluate(String code);

    WindowId getTopWindowId();

    File getScreenCapture();

    boolean isCustomAssertionsAvailable();

    String[][] getCustomAssertions(Object mcomponent);

    void insertScript(String function);

}
