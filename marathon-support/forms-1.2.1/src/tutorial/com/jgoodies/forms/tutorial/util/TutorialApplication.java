/*
 * Copyright (c) 2002-2008 JGoodies Karsten Lentzsch. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jgoodies.forms.tutorial.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 * A base class for tutorial applications. It provides a light version
 * of the startup behavior from the JSR 296 "Swing Application Framework".
 *
 * @author  Karsten Lentzsch
 * @version $Revision: 1.1 $
 */
public abstract class TutorialApplication {

    private static final Logger LOGGER = Logger.getLogger(TutorialApplication.class.getName());


    // Instance Creation ******************************************************

    protected TutorialApplication() {
        // Just set the constructor visibility.
    }


    // Life Cycle *************************************************************

    /**
     * Instantiates the given TutorialApplication class, then invokes
     * {@code #startup} with the given arguments. Typically this method
     * is called from an application's #main method.
     *
     * @param appClass the class of the application to launch
     * @param args optional launch arguments, often the main method's arguments.
     */
    public static synchronized void launch(
            final Class/*<? extends TutorialApplication>*/ appClass,
            final String[] args) {
        Runnable runnable = new Runnable() {
            public void run() {
                TutorialApplication application = null;
                try {
                    application = (TutorialApplication) appClass.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    LOGGER.log(Level.SEVERE, "Can't instantiate " + appClass, e);
                    return;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    LOGGER.log(Level.SEVERE, "Illegal Access during launch of " + appClass, e);
                    return;
                }
                try {
                    application.initializeLookAndFeel();
                    application.startup(args);
                } catch (Exception e) {
                    String message = "Failed to launch " + appClass;
                    //String message = String.format("Failed to launch %s ", appClass);
                    LOGGER.log(Level.SEVERE, message, e);
                    throw new Error(message, e);
                }
            }
        };
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            EventQueue.invokeLater(runnable);
        }
    }


    /**
     * Starts this application when the application is launched.
     * A typical application creates and shows the GUI in this method.
     * This method runs on the event dispatching thread.<p>
     *
     * Called by the static {@code launch} method.
     *
     * @param args optional launch arguments, often the main method's arguments.
     *
     * @see #launch(Class, String[])
     */
    protected abstract void startup(String[] args);


    // Look & Feel ************************************************************

    protected void initializeLookAndFeel() {
        try {
            String osName = System.getProperty("os.name");
            if (osName.startsWith("Windows")) {
                UIManager.setLookAndFeel("com.jgoodies.looks.windows.WindowsLookAndFeel");
            } else  if (osName.startsWith("Mac")) {
                // do nothing, use the Mac Aqua L&f
            } else {
                UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
            }
        } catch (Exception e) {
            // Likely the Looks library is not in the class path; ignore.
        }
    }


    // Default Frame Configuration ********************************************

    protected JFrame createFrame(String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        return frame;
    }


    // Standard Open Behavior *************************************************

    protected final void packAndShowOnScreenCenter(JFrame frame) {
        frame.pack();
        locateOnOpticalScreenCenter(frame);
        frame.setVisible(true);
    }


    // Screen Position ********************************************************

    /**
     * Locates the given component on the screen's center.
     *
     * @param component   the component to be centered
     */
    protected final void locateOnOpticalScreenCenter(Component component) {
        Dimension paneSize = component.getSize();
        Dimension screenSize = component.getToolkit().getScreenSize();
        component.setLocation((screenSize.width  - paneSize.width)  / 2,
                              (int) ((screenSize.height - paneSize.height) * 0.45));
    }


}
