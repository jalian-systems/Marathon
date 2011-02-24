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
package net.sourceforge.marathon.editor.rsta;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

import net.sourceforge.marathon.editor.IContentChangeListener;
import net.sourceforge.marathon.editor.IEditor;
import net.sourceforge.marathon.editor.ISearchDialog;
import net.sourceforge.marathon.editor.IStatusBar;

import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.GutterIconInfo;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchEngine;

public class RSTAEditor extends RSyntaxTextArea implements IEditor, DocumentListener, CaretListener, PreferenceChangeListener {

    public static final ImageIcon CURRENTLINE = new ImageIcon(RSTAEditor.class.getResource("currentline.gif"));
    public static final ImageIcon EMPTY_ICON = new ImageIcon(RSTAEditor.class.getResource("empty.gif"));

    private static class InsertPosition {
        int soff, eoff;
    }

    private final static class RSTADocument extends RSyntaxDocument {
        private static final long serialVersionUID = 1L;
        private UndoManager undoManager;

        private RSTADocument(String syntaxStyle) {
            super(syntaxStyle);
        }

        @Override public void addUndoableEditListener(UndoableEditListener listener) {
            super.addUndoableEditListener(listener);
            if (listener instanceof UndoManager)
                undoManager = (UndoManager) listener;
        }

        public UndoManager getUndoManager() {
            return undoManager;
        }
    }

    private static final long serialVersionUID = 1L;

    private RTextScrollPane scrollPane;
    private InsertPosition insert;

    private Map<String, Object> dataMap = new HashMap<String, Object>();
    private EventListenerList listeners = new EventListenerList();
    private IStatusBar statusBar;
    private boolean dirty;

    private GutterIconInfo currentLineTrackingInfo;
    private SyntaxScheme syntaxScheme;

    /** Search parameters **/
    private String searchText;
    private boolean caseSensitive;
    private boolean wrapSearch;
    private boolean wholeWord;
    private boolean regex;

    public RSTAEditor(boolean linenumbers, int startLineNumber) {
        super(new RSTADocument(SyntaxConstants.SYNTAX_STYLE_NONE));
        installPreferenceListener();
        readPreferences();
        scrollPane = new RTextScrollPane(this, linenumbers);
        if (!linenumbers)
            scrollPane.setIconRowHeaderEnabled(true);
        Document document = getDocument();
        document.addDocumentListener(this);
        addCaretListener(this);
        Gutter gutter = scrollPane.getGutter();
        gutter.setLineNumberingStartIndex(startLineNumber);
        gutter.setBookmarkingEnabled(true);
        setupGutterListener(gutter);
    }

    @Override protected void init() {
        setTextAreaFont();
        syntaxScheme = new SyntaxScheme(getFont());
        readPreferences();
        super.init();
    }

    @Override public SyntaxScheme getDefaultSyntaxScheme() {
        return syntaxScheme;
    }

    private void readPreferences() {
        Preferences preferences = Preferences.userNodeForPackage(RSTAEditor.class);
        try {
            String[] keys = preferences.keys();
            for (String key : keys) {
                String value = preferences.get(key, null);
                handlePreference(key, value);
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    private void installPreferenceListener() {
        Preferences preferences = Preferences.userNodeForPackage(RSTAEditor.class);
        preferences.addPreferenceChangeListener(this);
    }

    private void setupGutterListener(Gutter gutter) {
        Component[] components = gutter.getComponents();
        for (Component component : components) {
            component.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    if (e.getClickCount() > 1) {
                        fireGutterDoubleClickedEvent(e);
                    }
                }
            });
        }
    }

    protected void fireGutterDoubleClickedEvent(MouseEvent e) {
        IGutterListener[] gutterListeners = listeners.getListeners(IGutterListener.class);
        for (IGutterListener listener : gutterListeners) {
            int offset = viewToModel(e.getPoint());
            if (offset > -1) {
                try {
                    int line = getLineOfOffset(offset);
                    listener.gutterDoubleClickedAt(line);
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private UndoManager getUndoManager() {
        return ((RSTADocument) getDocument()).getUndoManager();
    }

    public void setStatusBar(IStatusBar statusBar) {
        this.statusBar = statusBar;
        updateStatusBar();
    }

    public void addKeyBinding(String keyBinding, ActionListener action) {
    }

    public void startInserting() {
        insert = new InsertPosition();
        insert.soff = getCaretPosition();
        insert.eoff = getCaretPosition();
    }

    public void stopInserting() {
        insert = null;
    }

    public void insertScript(String script) {
        if (insert == null) {
            return;
        }
        replaceRange(script, insert.soff, insert.eoff);
        insert.eoff = insert.soff + script.length();
        setCaretPosition(insert.eoff);
    }

    public void highlightLine(int line) {
        Gutter gutter = scrollPane.getGutter();
        if (line == -1) {
            if (currentLineTrackingInfo != null)
                gutter.removeTrackingIcon(currentLineTrackingInfo);
            currentLineTrackingInfo = null;
            return ;
        }
        setCaretLine(line);
        try {
            if (currentLineTrackingInfo != null)
                gutter.removeTrackingIcon(currentLineTrackingInfo);
            currentLineTrackingInfo = gutter.addLineTrackingIcon(line, CURRENTLINE);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void undo() {
        undoLastAction();
    }

    public void redo() {
        redoLastAction();
    }

    public boolean canUndo() {
        return getUndoManager().canUndo();
    }

    public boolean canRedo() {
        return getUndoManager().canRedo();
    }

    public void clearUndo() {
        discardAllEdits();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean b) {
        dirty = b;
    }

    public void addCaretListener(CaretListener listener) {
        super.addCaretListener(listener);
    }

    public void refresh() {
        repaint();
    }

    public void addContentChangeListener(IContentChangeListener l) {
        listeners.add(IContentChangeListener.class, l);
    }

    public int getCaretLine() {
        int caretPosition = getCaretPosition();
        try {
            return getLineOfOffset(caretPosition);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setCaretLine(int line) {
        try {
            int offset = getLineStartOffset(line);
            setCaretPosition(offset);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public Component getComponent() {
        return scrollPane;
    }

    public Selection getSelection() {
        int startOffset = getSelectionStart();
        int endOffset = getSelectionEnd();
        if (startOffset == endOffset)
            return null;
        try {
            int startLine = getLineOfOffset(startOffset);
            int endLine = getLineOfOffset(endOffset);
            String text = getSelectedText();
            return new Selection(text, startOffset, endOffset, startLine, endLine);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void addGutterListener(IGutterListener l) {
        listeners.add(IGutterListener.class, l);
    }

    public Object getData(String key) {
        return dataMap.get(key);
    }

    public void setData(String key, Object fileHandler) {
        dataMap.put(key, fileHandler);
    }

    public void setMode(String mode) {
        String styleKey = SyntaxConstants.SYNTAX_STYLE_NONE;
        if ("ruby".equals(mode))
            styleKey = SyntaxConstants.SYNTAX_STYLE_RUBY;
        else if ("xml".equals(mode))
            styleKey = SyntaxConstants.SYNTAX_STYLE_XML;
        else if ("html".equals(mode))
            styleKey = SyntaxConstants.SYNTAX_STYLE_HTML;
        else if ("python".equals(mode))
            styleKey = SyntaxConstants.SYNTAX_STYLE_PYTHON;
        else if ("java".equals(mode))
            styleKey = SyntaxConstants.SYNTAX_STYLE_JAVA;
        setSyntaxEditingStyle(styleKey);
    }

    public void dispose() {
    }

    /** Document Listener **/
    public void insertUpdate(DocumentEvent e) {
        fireContentChangeEvent();
    }

    public void removeUpdate(DocumentEvent e) {
        fireContentChangeEvent();
    }

    public void changedUpdate(DocumentEvent e) {
        fireContentChangeEvent();
    }

    private void fireContentChangeEvent() {
        setDirty(true);
        IContentChangeListener[] la = listeners.getListeners(IContentChangeListener.class);
        for (final IContentChangeListener l : la) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    l.contentChanged();
                }
            });
        }
    }

    /** Caret Listener **/
    public void caretUpdate(CaretEvent e) {
        updateStatusBar();
    }

    private void updateStatusBar() {
        if (statusBar == null)
            return;
        int row = getCaretLineNumber() + 1;
        int col = getCaretOffsetFromLineStart() + 1;
        statusBar.setCaretLocation(row, col);
        statusBar.setIsOverwriteEnabled(getTextMode() == RTextArea.OVERWRITE_MODE);
    }

    /** Support for popup menu **/
    @Override protected JPopupMenu createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        return popupMenu;
    }

    @Override protected void configurePopupMenu(JPopupMenu popupMenu) {
        requestFocusInWindow();
        super.configurePopupMenu(popupMenu);
    }

    @Override public void paint(Graphics g) {
        Gutter gutter = scrollPane.getGutter();
        gutter.removeAllTrackingIcons();
        IGutterListener[] gutterListeners = listeners.getListeners(IGutterListener.class);
        for (IGutterListener listener : gutterListeners) {
            for (int line = 0; line < getLineCount(); line++) {
                Icon icon = listener.getIconAtLine(line);
                if (icon != null)
                    try {
                        gutter.addLineTrackingIcon(line, icon);
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
            }
        }
        if (currentLineTrackingInfo != null) {
            try {
                currentLineTrackingInfo = gutter.addOffsetTrackingIcon(currentLineTrackingInfo.getMarkedOffset(),
                        currentLineTrackingInfo.getIcon());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
        super.paint(g);
    }

    public void setMenuItems(JMenuItem[] menuItems) {
        JPopupMenu menu = getPopupMenu();
        for (JMenuItem item : menuItems) {
            if (item == null)
                menu.addSeparator();
            else
                menu.add(item);
        }
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setIcon(EMPTY_ICON);
        toolsMenu.add(new AbstractAction("Convert tabs to spaces", EMPTY_ICON) {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                convertTabsToSpaces();
            }
        });
        toolsMenu.add(new AbstractAction("Convert spaces to tabs", EMPTY_ICON) {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                convertSpacesToTabs();
            }
        });
        menu.add(toolsMenu);
    }

    public void setFocus() {
        requestFocusInWindow();
    }

    /** PrefenceChange listener **/
    public void preferenceChange(PreferenceChangeEvent evt) {
        final String key = evt.getKey();
        final String value = evt.getNewValue();
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    handlePreference(key, value);
                }
                
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void handlePreference(String key, final String value) {
        if ("editor.font".equals(key) || "editor.fontsize".equals(key)) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setFontPreference();
                }
            });
        } else if ("editor.tabconversion".equals(key)) {
            boolean tabConversion = Boolean.parseBoolean(value);
            setTabsEmulated(tabConversion);
        } else if ("editor.tabsize".equals(key)) {
            int tabSize = Integer.parseInt(value);
            setTabSize(tabSize);
        }
    }

    public void setFontPreference() {
        Font font = setTextAreaFont();
        if (font != null) {
            for (int i = 0; i < syntaxScheme.styles.length; i++) {
                if (syntaxScheme.styles[i] != null) {
                    syntaxScheme.styles[i].font = font;
                }
            }
            setSyntaxScheme(syntaxScheme);
        }
    }

    private Font setTextAreaFont() {
        Preferences p = Preferences.userNodeForPackage(RSTAEditor.class);
        String fontName = p.get("editor.font", getFont().getFamily());
        String fontSize = p.get("editor.fontsize", Integer.toString(getFont().getSize()));
        Font font = Font.decode(fontName);
        font = font.deriveFont(Float.parseFloat(fontSize));
        setFont(font);
        return font;
    }

    /**
     * Search Methods
     */
    public void closeSearch() {
    }

    public int find(String searchText, boolean bForward, boolean bAllLines, boolean bCaseSensitive, boolean bWrapSearch,
            boolean bWholeWord, boolean bRegex) {
        this.searchText = searchText;
        this.caseSensitive = bCaseSensitive;
        this.wrapSearch = bWrapSearch;
        this.wholeWord = bWholeWord;
        this.regex = bRegex;
        boolean b = SearchEngine.find(this, searchText, bForward, bCaseSensitive, bWholeWord, bRegex);
        if (!b)
            beep();
        if (!b && bWrapSearch) {
            if (bForward)
                setCaretPosition(0);
            else
                setCaretPosition(getDocument().getLength() - 1);
            b = SearchEngine.find(this, searchText, bForward, bCaseSensitive, bWholeWord, bRegex);
            if (b)
                return FIND_WRAPPED;
        }
        return b ? FIND_SUCCESS : FIND_FAILED ;
    }

    private void beep() {
        UIManager.getLookAndFeel().provideErrorFeedback(this);
    }

    public int replaceFind(String searchText, String replaceText, boolean bForward, boolean bAllLines, boolean bCaseSensitive,
            boolean bWrapSearch, boolean bWholeWord, boolean bRegex) {
        replace(searchText, replaceText, bForward, bAllLines, bCaseSensitive, bWrapSearch, bWholeWord, bRegex);
        return find(searchText, bForward, bAllLines, bCaseSensitive, bWrapSearch, bWholeWord, bRegex);
    }

    public void replace(String searchText, String replaceText, boolean bForward, boolean bAllLines, boolean bCaseSensitive,
            boolean bWrapSearch, boolean bWholeWord, boolean bRegex) {
        this.searchText = searchText;
        this.caseSensitive = bCaseSensitive;
        this.wrapSearch = bWrapSearch;
        this.wholeWord = bWholeWord;
        this.regex = bRegex;
        SearchEngine.replace(this, searchText, replaceText, bForward, bCaseSensitive, bWholeWord, bRegex);
    }

    public void replaceAll(String searchText, String replaceText, boolean bCaseSensitive, boolean bWholeWord, boolean bRegex) {
        this.searchText = searchText;
        this.caseSensitive = bCaseSensitive;
        this.wholeWord = bWholeWord;
        this.regex = bRegex;
        SearchEngine.replaceAll(this, searchText, replaceText, bCaseSensitive, bWholeWord, bRegex);
    }

    public void find(int findPrev) {
        String searchText = null;
        Selection selection = getSelection();
        if (selection != null) {
            if (selection.getStartLine() == selection.getEndLine()) {
                searchText = getSelectedText();
            }
        }
        boolean regex = false;
        if (searchText == null) {
            searchText = this.searchText;
            regex = this.regex;
        }
        if (searchText == null) {
            beep();
            return;
        }
        boolean forward = findPrev == IEditor.FIND_NEXT;
        find(searchText, forward, true, caseSensitive, wrapSearch, wholeWord, regex);
    }

    public void showSearchDialog(ISearchDialog dialog) {
        Selection selection = getSelection();
        dialog.setSelectedLine(false);
        if (selection != null && selection.getStartLine() == selection.getEndLine())
            dialog.setSearchText(getSelectedText());
        else if (selection != null && selection.getStartLine() != selection.getEndLine()) {
            dialog.setSelectedLine(true);
        }
        dialog.setVisible(true);
    }

    public void toggleInsertMode() {
        int mode = getTextMode();
        if (mode == RTextArea.OVERWRITE_MODE)
            mode = RTextArea.INSERT_MODE;
        else
            mode = RTextArea.OVERWRITE_MODE;
        setTextMode(mode);
        updateStatusBar();
    }

}
