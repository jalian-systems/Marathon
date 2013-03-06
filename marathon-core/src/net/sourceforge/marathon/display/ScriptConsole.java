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

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.display.readline.TextAreaReadline;

public final class ScriptConsole extends JDialog implements IStdOut {
    private static final long serialVersionUID = 1L;
    private JEditorPane text;
    private transient TextAreaReadline textAreaReadline;
    private Font font;
    private Color backgroundColor = new Color(0xf2, 0xf2, 0xf2);
    private Color foregroundColor = new Color(0xa4, 0x00, 0x00);
    private Color caretColor = new Color(0xa4, 0x00, 0x00);
    private Color promptForegroundColor = new Color(0xa4, 0x00, 0x00);
    private Color inputForegroundColor = new Color(0x20, 0x4a, 0x87);
    private Color outputForegroundColor = Color.darkGray;
    private Color resultForegroundColor = new Color(0x20, 0x4a, 0x87);
    private Color errorForegroundColor = Color.RED;
    protected PrintWriter spooler;
    private PrintStream oldOut;
    private PrintStream oldErr;

    public ScriptConsole(JFrame parent, Font defaultFont, final IScriptConsoleListener l, final String spoolSuffix) {
        super(parent);
        setTitle("Script Console");
        text = new JTextPane();
        text.setMargin(new Insets(8, 8, 8, 8));
        readPreferences(defaultFont);
        text.setCaretColor(caretColor);
        text.setBackground(backgroundColor);
        text.setForeground(foregroundColor);
        text.setFont(font);
        JScrollPane pane = new JScrollPane();
        pane.setViewportView(text);
        pane.setBorder(BorderFactory.createLineBorder(Color.darkGray));
        getContentPane().add(pane);
        setSize(640, 480);
        validate();
        textAreaReadline = new TextAreaReadline(text, "Marathon Script Console \n\n") {
            @Override public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    textAreaReadline.shutdown();
                    if (spooler != null)
                        spooler.close();
                    resetStdStreams();
                } else {
                    super.keyPressed(event);
                }
            }
        };
        textAreaReadline.setPromptForegroundColor(promptForegroundColor);
        textAreaReadline.setErrorForegroundColor(errorForegroundColor);
        textAreaReadline.setInputForegroundColor(inputForegroundColor);
        textAreaReadline.setResultForegroundColor(resultForegroundColor);
        textAreaReadline.setOutputForegroundColor(outputForegroundColor);
        final String projectDir = System.getProperty(Constants.PROP_PROJECT_DIR);
        try {
            textAreaReadline.setHistoryFile(new File(projectDir, ".history"));
        } catch (IOException e1) {
        }

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                textAreaReadline.shutdown();
                if (spooler != null)
                    spooler.close();
                resetStdStreams();
            }
        });
        setLocationRelativeTo(getParent());
        try {
            spooler = new PrintWriter(new FileWriter(new File(projectDir, "spool" + spoolSuffix), true));
        } catch (IOException e1) {
        }
        Thread t2 = new Thread() {
            public void run() {
                String line = null;
                while ((line = textAreaReadline.readLine(">> ")) != null) {
                    line = line.trim();
                    if (line.equals(""))
                        continue;
                    spooler.println(line);
                    spooler.flush();
                    if (line.equals("help"))
                        line = "marathon_help()";
                    else if (line.equals("spool clear")) {
                        try {
                            if (spooler != null)
                                spooler.close();
                            spooler = new PrintWriter(new FileWriter(new File(projectDir, "spool" + spoolSuffix), false));
                        } catch (IOException e) {
                        }
                        continue;
                    }
                    textAreaReadline.getHistory().addToHistory(line);
                    String ret = l.evaluateScript(line);
                    if (ret != null && !ret.equals(""))
                        append("=> " + ret + "\n", IStdOut.STD_OUT);
                }
                l.sessionClosed();
            }

        };
        t2.start();
    }

    private void setStdStreams() {
        oldOut = System.out;
        oldErr = System.err;
        oldOut.flush();
        oldErr.flush();
        System.setOut(new PrintStream(new OutputStream() {
            @Override public void write(int b) throws IOException {
                append((byte) b, IStdOut.STD_OUT);
            }
        }));
        System.setErr(new PrintStream(new OutputStream() {
            @Override public void write(int b) throws IOException {
                append((byte) b, IStdOut.STD_ERR);
            }
        }));
    }

    private void resetStdStreams() {
        System.setOut(oldOut);
        System.setErr(oldErr);
    }

    private void readPreferences(Font defaultFont) {
        Preferences prefs = Preferences.userNodeForPackage(ScriptConsole.class);
        Color color;
        if ((color = getPrefColor(prefs, "marathon.scriptconsole.caretcolor")) != null)
            caretColor = color;
        if ((color = getPrefColor(prefs, "marathon.scriptconsole.foregroundcolor")) != null)
            foregroundColor = color;
        if ((color = getPrefColor(prefs, "marathon.scriptconsole.backgroundcolor")) != null)
            backgroundColor = color;
        if ((color = getPrefColor(prefs, "marathon.scriptconsole.promptforegroundcolor")) != null)
            promptForegroundColor = color;
        if ((color = getPrefColor(prefs, "marathon.scriptconsole.inputforegroundcolor")) != null)
            inputForegroundColor = color;
        if ((color = getPrefColor(prefs, "marathon.scriptconsole.outputforegroundcolor")) != null)
            outputForegroundColor = color;
        if ((color = getPrefColor(prefs, "marathon.scriptconsole.resultforegroundcolor")) != null)
            resultForegroundColor = color;
        if ((color = getPrefColor(prefs, "marathon.scriptconsole.errorforegroundcolor")) != null)
            errorForegroundColor = color;

        font = null;
        String prop = prefs.get("marathon.scriptconsole.font", null);
        if (prop != null) {
            font = Font.decode(prop);
        }
        if (font == null)
            font = defaultFont;
    }

    private Color getPrefColor(Preferences prefs, String key) {
        Color color = null;
        String prop = prefs.get(key, null);
        if (prop != null)
            color = Color.decode(prop);
        return color;
    }

    public void append(String text, int type) {
        OutputStream stream = null;
        try {
            stream = isErrorType(type) ? textAreaReadline.getErrorStream() : textAreaReadline.getOutputStream();
            stream.write(text.getBytes());
            stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (IOException e) {
                }
        }
    }

    public void append(byte b, int type) {
        OutputStream stream = null;
        try {
            stream = isErrorType(type) ? textAreaReadline.getErrorStream() : textAreaReadline.getOutputStream();
            stream.write(new byte[] { b });
            stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (IOException e) {
                }
        }
    }

    private boolean isErrorType(int type) {
        return type == IStdOut.SCRIPT_ERR || type == IStdOut.STD_ERR;
    }

    public void clear() {
        text.setText("");
    }

    public String getText() {
        return text.getText();
    }

    @Override public void setVisible(boolean b) {
        if (b)
            setStdStreams();
        super.setVisible(b);
    }
}