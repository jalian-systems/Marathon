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

package net.sourceforge.marathon.component;

import static org.junit.Assert.assertEquals;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;

import net.sourceforge.marathon.event.AWTSync;
import net.sourceforge.marathon.recorder.WindowMonitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestMFileChooser {

    private MFileChooser mChooser;
    private JFileChooser fileChooser;
    protected String lastCommand;

    @Before
    public void setUp() throws Exception {
        fileChooser = new JFileChooser();
        // fileChooser.setMultiSelectionEnabled(false);
        mChooser = new MFileChooser(fileChooser, getName(), null, WindowMonitor.getInstance());
    }

    private String getName() {
        return getClass().getName();
    }

    @After
    public void tearDown() throws Exception {
        mChooser = null;
    }

    @Test
    public void testGetTextReturnsSelectedFilesByFileChooser() {
        fileChooser.setSelectedFile(new File(System.getProperty("user.home"), "somefile"));
        fileChooser.approveSelection();
        assertEquals("#H/somefile", mChooser.getText());
    }

    @Test
    public void testGetTextReturnsEmptyWhenCancelled() {
        fileChooser.setSelectedFile(new File(System.getProperty("user.home"), "somefile"));
        fileChooser.cancelSelection();
        assertEquals("", mChooser.getText());
    }

    @Test
    public void testSetTextEmptyCancelsSelection() {
        fileChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                lastCommand = e.getActionCommand();
            }
        });
        mChooser.setText("");
        AWTSync.sync();
        assertEquals(JFileChooser.CANCEL_SELECTION, lastCommand);
    }

    @Test
    public void testSetTextFile() {
        fileChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                lastCommand = e.getActionCommand();
            }
        });
        mChooser.setText("#C/somefile");
        AWTSync.sync();
        assertEquals(JFileChooser.APPROVE_SELECTION, lastCommand);
        assertEquals(new File(System.getProperty("user.dir"), "somefile"), fileChooser.getSelectedFile());
    }
}
