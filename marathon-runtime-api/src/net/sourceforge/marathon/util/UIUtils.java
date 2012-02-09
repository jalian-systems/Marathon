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
package net.sourceforge.marathon.util;

import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class UIUtils {

    private static final Icon ICON_CANCEL = new ImageIcon(UIUtils.class.getResource("icons/cancel.gif"));
    private static final Icon ICON_OK = new ImageIcon(UIUtils.class.getResource("icons/ok.gif"));
    private static final Icon ICON_UP = new ImageIcon(UIUtils.class.getResource("icons/up.gif"));
    private static final Icon ICON_DOWN = new ImageIcon(UIUtils.class.getResource("icons/down.gif"));
    private static final Icon ICON_COLLAPSE_ALL = new ImageIcon(UIUtils.class.getResource("icons/collapseall.gif"));
    private static final Icon ICON_EXPAND_ALL = new ImageIcon(UIUtils.class.getResource("icons/expandall.gif"));
    private static final Icon ICON_REFRESH = new ImageIcon(UIUtils.class.getResource("icons/refresh.gif"));
    private static final Icon ICON_LOAD_DEFAULTS = new ImageIcon(UIUtils.class.getResource("icons/loaddefaults.gif"));
    private static final ImageIcon ICON_CLEAR = new ImageIcon(UIUtils.class.getResource("icons/clear.gif"));
    private static final ImageIcon ICON_SHOW_MESSAGE = new ImageIcon(UIUtils.class.getResource("icons/editor.gif"));
    private static final ImageIcon ICON_EXPORT = new ImageIcon(UIUtils.class.getResource("icons/export.gif"));
    private static final ImageIcon ICON_NEW = new ImageIcon(UIUtils.class.getResource("icons/new.gif"));
    private static final ImageIcon ICON_EDIT = new ImageIcon(UIUtils.class.getResource("icons/edit.gif"));
    private static final ImageIcon ICON_BROWSE = new ImageIcon(UIUtils.class.getResource("icons/browse.gif"));
    private static final ImageIcon ICON_SAVE = new ImageIcon(UIUtils.class.getResource("icons/save.gif"));
    private static final ImageIcon ICON_TEST = new ImageIcon(UIUtils.class.getResource("icons/test.gif"));
    private static final ImageIcon ICON_ADD = new ImageIcon(UIUtils.class.getResource("icons/add.gif"));
    private static final ImageIcon ICON_ADD_FOLDER = new ImageIcon(UIUtils.class.getResource("icons/addfolder.gif"));
    private static final ImageIcon ICON_ADD_JAR = new ImageIcon(UIUtils.class.getResource("icons/addjar.gif"));
    private static final ImageIcon ICON_REMOVE = new ImageIcon(UIUtils.class.getResource("icons/remove.gif"));

    public static JButton createHeaderButton() {
        return new JButton("Header");
    }

    public static JButton createChecklistButton() {
        return new JButton("Checklist");
    }

    public static JButton createTextboxButton() {
        return new JButton("Textbox");
    }

    public static JButton createRemoveButton() {
        JButton b = new JButton("Remove", ICON_REMOVE);
        b.setMnemonic(KeyEvent.VK_MINUS);
        return b;
    }

    public static JButton createCancelButton() {
        return new JButton("Cancel", ICON_CANCEL);
    }

    public static JButton createOKButton() {
        return new JButton("OK", ICON_OK);
    }

    public static JButton createDownButton() {
        return new JButton("Down", ICON_DOWN);
    }

    public static JButton createUpButton() {
        return new JButton("Up", ICON_UP);
    }

    public static JButton createNewButton() {
        return new JButton("New...", ICON_NEW);
    }

    public static JButton createSaveButton() {
        return new JButton("Save", ICON_SAVE);
    }

    public static JButton createEditButton() {
        return new JButton("Edit...", ICON_EDIT);
    }

    public static JButton createSaveAsButton() {
        return new JButton("Save As");
    }

    public static JButton createInsertButton() {
        return new JButton("Insert");
    }

    public static JButton createDoneButton() {
        return new JButton("Done");
    }

    public static JButton createCreditsButton() {
        return new JButton("Credits");
    }

    public static JButton createScreenCaptureButton() {
        return new JButton("Screen Capture");
    }

    public static JButton createTestButton() {
        return new JButton("Test", ICON_TEST);
    }

    public static JButton createSelectButton() {
        return new JButton("Select", ICON_OK);
    }

    public static JButton createExpandAllButton() {
        JButton jButton = new JButton(ICON_EXPAND_ALL);
        jButton.setToolTipText("Expand All");
        return jButton;
    }

    public static JButton createCollapseAllButton() {
        JButton jButton = new JButton(ICON_COLLAPSE_ALL);
        jButton.setToolTipText("Collapse All");
        return jButton;
    }

    public static JButton createRefreshButton() {
        JButton jButton = new JButton(ICON_REFRESH);
        jButton.setToolTipText("Refresh");
        return jButton;
    }

    public static JButton createActionButton(Action action) {
        return new JButton(action);
    }

    public static JButton createGotoButton() {
        return new JButton("Goto");
    }

    public static JButton createLoadDefaultsButton() {
        return new JButton("Load Defaults", ICON_LOAD_DEFAULTS);
    }

    public static JButton createCloseButton() {
        return new JButton("Close");
    }

    public static JButton createClearButton() {
        JButton jButton = new JButton(ICON_CLEAR);
        jButton.setToolTipText("Clear");
        return jButton;
    }

    public static JButton createShowMessageButton() {
        JButton jButton = new JButton(ICON_SHOW_MESSAGE);
        jButton.setToolTipText("Show Message");
        return jButton;
    }

    public static JButton createFindButton() {
        return new JButton("Find");
    }

    public static JButton createReplaceFindButton() {
        return new JButton("Replace/Find");
    }

    public static JButton createReplaceButton() {
        return new JButton("Replace");
    }

    public static JButton createReplaceAllButton() {
        return new JButton("Replace All");
    }

    public static JButton createExportButton() {
        JButton jButton = new JButton(ICON_EXPORT);
        jButton.setToolTipText("Export");
        return jButton;
    }

    public static JButton createBrowseButton() {
        return new JButton("Browse..", ICON_BROWSE);
    }

    public static JButton createAddButton() {
        JButton b = new JButton("Add...", ICON_ADD);
        b.setMnemonic(KeyEvent.VK_PLUS);
        return b;
    }

    public static JButton createAddClassButton() {
        return createAddButton();
    }

    public static JButton createAddArchivesButton() {
        return new JButton("Add Archives...", ICON_ADD_JAR);
    }

    public static JButton createAddFoldersButton() {
        JButton b = new JButton("Add Folders...", ICON_ADD_FOLDER);
        b.setMnemonic(KeyEvent.VK_PLUS);
        return b;
    }

    public static JButton createInsertAssertionButton() {
        return new JButton("Insert Assertion");
    }

    public static JButton createInsertWaitButton() {
        return new JButton("Insert Wait");
    }

    public static JButton createEmptyButton() {
        return new JButton();
    }

}
