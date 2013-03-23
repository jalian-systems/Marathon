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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import net.sourceforge.marathon.navigator.DefaultActions;
import net.sourceforge.marathon.navigator.Navigator;
import net.sourceforge.marathon.navigator.NavigatorAbstractAction;
import net.sourceforge.marathon.util.FileHandler;

/**
 * Additional actions to be displayed on right clicking on a file displayed in
 * Navigator view.
 */
public class DisplayWindowNavigatorActions extends DefaultActions {

    private final class OnTestFileAction extends NavigatorAbstractAction {
        private static final long serialVersionUID = 1L;
        private Runnable runnable;

        private OnTestFileAction(Runnable runnable, Navigator navigator, String name, Icon icon_enabled, Icon icon_disabled) {
            super(navigator, name, icon_enabled, icon_disabled);
            this.runnable = runnable;
        }

        @Override public boolean getEnabledState(File[] files) {
            if (files != null && files.length == 1 && files[0].isFile()) {
                try {
                    fileHandler.readFile(files[0]);
                    return fileHandler.isTestFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }

            }
            return false;
        }

        @Override public void actionPerformed(ActionEvent e, File[] file) {
            displayWindow.openFile(file[0]);
            SwingUtilities.invokeLater(runnable);
        }
    }

    /**
     * 
     */
    private final DisplayWindow displayWindow;

    private final FileHandler fileHandler;

    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.marathon.navigator.DefaultActions#DefaultActions(
     * Navigator)
     */
    public DisplayWindowNavigatorActions(DisplayWindow displayWindow, Navigator navigator, FileHandler fileHandler) {
        super(navigator);
        this.displayWindow = displayWindow;
        this.fileHandler = fileHandler;
    }

    /**
     * Create JMenu with new file/folder options
     * 
     * @return
     */
    protected JMenu createNewMenu() {
        JMenu newMenu = new JMenu("New");
        newMenu.add((new NewFolderAction()).getMenuItem());
        newMenu.add(displayWindow.getMenuItemWithAccelKey(this.displayWindow.newTestcaseAction, "^+N"));
        newMenu.add(this.displayWindow.etAction);
        newMenu.add(this.displayWindow.newModuleAction);
        newMenu.add(this.displayWindow.newFixtureAction);
        newMenu.add(this.displayWindow.newModuleDirAction);
        newMenu.add(this.displayWindow.newSuiteFileAction);
        newMenu.add((new NewFileAction()).getMenuItem());
        return newMenu;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.marathon.navigator.DefaultActions#getMenuItems()
     */
    public List<Component> getMenuItems() {
        List<Component> menuItems = new Vector<Component>();
        menuItems.add(createNewMenu());
        menuItems.add(new JSeparator());

        menuItems.add(displayWindow.getMenuItemWithAccelKey(new OnTestFileAction(new Runnable() {
            public void run() {
                displayWindow.onPlay();
            }
        }, navigator, "Play", displayWindow.playAction.getIconEnabled(), displayWindow.playAction.getIconDisabled()), "^+P"));

        menuItems.add(displayWindow.getMenuItemWithAccelKey(new OnTestFileAction(new Runnable() {
            public void run() {
                displayWindow.onSlowPlay();
            }
        }, navigator, "Slow Play", displayWindow.slowPlayAction.getIconEnabled(), displayWindow.slowPlayAction.getIconDisabled()),
                "^S+P"));

        menuItems.add(displayWindow.getMenuItemWithAccelKey(new OnTestFileAction(new Runnable() {
            public void run() {
                displayWindow.onDebug();
            }
        }, navigator, "Debug", displayWindow.debugAction.getIconEnabled(), displayWindow.debugAction.getIconDisabled()), "^A+P"));

        menuItems.add(new JSeparator());
        menuItems.add(displayWindow.getMenuItemWithAccelKey(new CopyAction(), "^+C"));
        menuItems.add(displayWindow.getMenuItemWithAccelKey(new PasteAction(), "^+V"));
        menuItems.add(displayWindow.getMenuItemWithAccelKey(new DeleteAction(), "DELETE"));
        menuItems.add((new MoveAction()).getMenuItem());
        menuItems.add(displayWindow.getMenuItemWithAccelKey(new RenameAction(), "F2"));
        menuItems.add(new JSeparator());
        menuItems.add(displayWindow.getMenuItemWithAccelKey(new ExpandAllAction(), "PLUS"));
        menuItems.add(displayWindow.getMenuItemWithAccelKey(new CollapseAllAction(), "MINUS"));
        menuItems.add(new JSeparator());
        menuItems.add((new GoIntoAction()).getMenuItem());
        menuItems.add((new GoUpAction()).getMenuItem());
        menuItems.add((new HomeAction()).getMenuItem());
        menuItems.add(new JSeparator());
        menuItems.add(displayWindow.getMenuItemWithAccelKey(new RefreshAction(), "F5"));
        menuItems.add(new JSeparator());
        menuItems.add(displayWindow.getMenuItemWithAccelKey(new PropertiesAction(), "A+ENTER"));
        return menuItems;
    }
}