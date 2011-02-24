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
package net.sourceforge.marathon.screencapture;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JApplet;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

public class MarathonAnnotateApplet extends JApplet {

    private static final long serialVersionUID = 1L;
    private ImagePanel imagePanel;
    private JSplitPane splitPane;

    @Override
    public void init() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    createGUI();
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void createGUI() {
        String parameter = getParameter("IMAGE");
        try {
            imagePanel = new ImagePanel(this.getClass().getResourceAsStream(parameter), false);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        final JSplitPane splitPane = createSplitPane();
        splitPane.addComponentListener(new ComponentAdapter() {
            private boolean firstTime = true;

            @Override
            public void componentResized(ComponentEvent e) {
                if (firstTime) {
                    Dimension imgSize = imagePanel.getPreferredSize();
                    splitPane.setDividerLocation(imgSize.width);
                    firstTime = false;
                }
                splitPane.revalidate();
            }
        });
        getContentPane().add(new JScrollPane(splitPane));
    }

    private JSplitPane createSplitPane() {
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        splitPane.setLeftComponent(imagePanel);
        splitPane.setRightComponent(getAnnotationPanel());
        return splitPane;
    }

    private Component getAnnotationPanel() {
        return new AnnotationPanel(imagePanel, true);
    }

}
