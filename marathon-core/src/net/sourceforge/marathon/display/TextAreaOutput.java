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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.util.UIUtils;

import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockingConstants;
import com.vlsolutions.swing.docking.ui.DockingUISettings;
import com.vlsolutions.swing.toolbars.ToolBarConstraints;
import com.vlsolutions.swing.toolbars.ToolBarContainer;
import com.vlsolutions.swing.toolbars.ToolBarPanel;
import com.vlsolutions.swing.toolbars.VLToolBar;

class TextSegment {
    SimpleAttributeSet attr;
    String text;

    public TextSegment(SimpleAttributeSet attr, String text) {
        this.attr = attr;
        this.text = text;
    }
}

class AttrTextPane extends JTextPane {
    private static final long serialVersionUID = 1L;
    private static final Color[] styleColors = new Color[] { Color.BLACK, Color.BLUE, Color.RED, Color.BLUE, Color.RED };
    private static final int[] styleFont = new int[] { Font.BOLD, Font.PLAIN | Font.BOLD, Font.PLAIN | Font.BOLD, Font.ITALIC,
            Font.ITALIC };

    private SimpleAttributeSet[] attributes = new SimpleAttributeSet[5];
    private DefaultStyledDocument document = new DefaultStyledDocument();
    private ArrayList<TextSegment> segments = new ArrayList<TextSegment>();
    private boolean updateSet = false;

    private static int documentSize = Integer.getInteger(Constants.PROP_TEXT_AREA_OUTPUT_SIZE, 10*1024);
    
    public AttrTextPane() {
        setDocument(document);
        for (int i = 0; i < 5; i++) {
            attributes[i] = new SimpleAttributeSet();
            StyleConstants.setForeground(attributes[i], styleColors[i]);
            if ((styleFont[i] & Font.ITALIC) != 0)
                StyleConstants.setItalic(attributes[i], true);
            if ((styleFont[i] & Font.BOLD) != 0)
                StyleConstants.setBold(attributes[i], true);
        }
    }

    void clear() {
        setText("");
    }

    public void append(final String text, final int type) {
        synchronized (this) {
            segments.add(new TextSegment(attributes[type], text));
            if (updateSet)
                return;
            updateSet = true;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    synchronized (AttrTextPane.this) {
                        int size = segments.size();
                        int length = document.getLength();
                        if (segments.size() > 0 && length > documentSize) {
                            document.replace(0, 1024, "", segments.get(0).attr);
                        }
                        for (int i = 0; i < size; i++) {
                            TextSegment segment = (TextSegment) segments.remove(0);
                            document.insertString(document.getLength(), segment.text, segment.attr);
                        }
                        updateSet = false;
                    }
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

public class TextAreaOutput implements IStdOut, Dockable {
    private static final Icon ICON_OUTPUT = new ImageIcon(TextAreaOutput.class.getResource("icons/enabled/console_view.gif"));
    private static final DockKey DOCK_KEY = new DockKey("Output", "Output", "Output from the scripts", ICON_OUTPUT,
            DockingConstants.HIDE_BOTTOM);

    private AttrTextPane textPane = new AttrTextPane();
    private Component component;

    public TextAreaOutput() {
        DockingUISettings.getInstance().installUI();
        ToolBarContainer container = ToolBarContainer.createDefaultContainer(true, false, false, false, FlowLayout.TRAILING);
        ToolBarPanel barPanel = container.getToolBarPanelAt(BorderLayout.NORTH);
        VLToolBar bar = new VLToolBar();
        JButton clear = UIUtils.createClearButton();
        clear.setToolTipText("Clear");
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textPane.clear();
            }
        });
        bar.add(clear);
        JButton export = UIUtils.createExportButton();
        export.setToolTipText("Export to a text file");
        export.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int dialog = fileChooser.showSaveDialog(component.getParent());
                if (dialog == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if (file.exists()) {

                    }
                    try {
                        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
                        stream.write(textPane.getText().getBytes());
                        stream.close();
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        });
        bar.add(export);
        barPanel.add(bar, new ToolBarConstraints());

        textPane.setSize(100, 100);
        textPane.setEditable(false);
        textPane.setEnabled(true);
        textPane.setName("output");
        container.add(new JScrollPane(textPane));
        component = container;
    }

    public String getText() {
        return textPane.getText();
    }

    public synchronized void append(String text, int type) {
        textPane.append(text, type);
    }

    public void clear() {
        textPane.clear();
    }

    public Component getComponent() {
        return component;
    }

    public DockKey getDockKey() {
        return DOCK_KEY;
    }
}
