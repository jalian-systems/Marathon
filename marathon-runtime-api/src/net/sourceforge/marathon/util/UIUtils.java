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
    private static final ImageIcon ICON_CLEAR = new ImageIcon(UIUtils.class.getResource("icons/clear.gif"));
    private static final ImageIcon ICON_SHOW_MESSAGE = new ImageIcon(UIUtils.class.getResource("icons/editor.gif"));
    private static final ImageIcon ICON_EXPORT = new ImageIcon(UIUtils.class.getResource("icons/export.gif"));


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
        return new JButton("Remove");
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
        return new JButton("New...");
    }

    public static JButton createSaveButton() {
        return new JButton("Save");
    }

    public static JButton createEditButton() {
        return new JButton("Edit...");
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
        return new JButton("Test");
    }

    public static JButton createSelectButton() {
        return new JButton("Select");
    }

    public static JButton createExpandAllButton() {
        return new JButton(ICON_EXPAND_ALL);
    }

    public static JButton createCollapseAllButton() {
        return new JButton(ICON_COLLAPSE_ALL);
    }

    public static JButton createRefreshButton() {
        return new JButton(ICON_REFRESH);
    }

    public static JButton createActionButton(Action action) {
        return new JButton(action);
    }

    public static JButton createGotoButton() {
        return new JButton("Goto");
    }

    public static JButton createLoadDefaultsButton() {
        return new JButton("Load Defaults");
    }

    public static JButton createCloseButton() {
        return new JButton("Close");
    }

    public static JButton createClearButton() {
        return new JButton(ICON_CLEAR);
    }

    public static JButton createShowMessageButton() {
        return new JButton(ICON_SHOW_MESSAGE);
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
        return new JButton(ICON_EXPORT);
    }

    public static JButton createAddAssertionButton() {
        return new JButton("Add Assertion...");
    }

    public static JButton createBrowseButton() {
        return new JButton("Browse..");
    }

    public static JButton createAddButton() {
        return new JButton("Add...");
    }

    public static JButton createAddClassButton() {
        return new JButton("Add Class...");
    }

    public static JButton createAddArchivesButton() {
        return new JButton("Add Archives...");
    }

    public static JButton createAddFoldersButton() {
        return new JButton("Add Folders...");
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
