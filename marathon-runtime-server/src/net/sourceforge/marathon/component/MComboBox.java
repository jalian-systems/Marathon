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

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.accessibility.AccessibleContext;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.ComboPopup;

import net.sourceforge.marathon.action.ClickAction;
import net.sourceforge.marathon.event.FireableMouseClickEvent;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.Retry;
import net.sourceforge.marathon.util.Snooze;

public class MComboBox extends MCollectionComponent {
    public MComboBox(JComboBox comboBox, String name, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(comboBox, name, finder, windowMonitor);
    }

    private JComboBox getComboBox() {
        return (JComboBox) getComponent();
    }

    public String getText() {
        if (eventQueueRunner.invokeBoolean(getComboBox(), "isEditable")) {
            return getEditor().getText();
        }
        Object selectedItem = eventQueueRunner.invoke(getComboBox(), "getSelectedItem");
        if (selectedItem == null)
            return null;
        return getStringRep(selectedItem);
    }

    private String getStringRep(Object selectedItem) {
        ListCellRenderer listCellRenderer = (ListCellRenderer) eventQueueRunner.invoke(getComboBox(), "getRenderer");
        Component rendererComponent = listCellRenderer.getListCellRendererComponent(new JList(), selectedItem, 0, false, false);
        if (rendererComponent != null) {
            MComponent mcomp = finder.getMComponentByComponent(rendererComponent, "doesn't matter", null);
            if (mcomp != null && !mcomp.getClass().equals(MComponent.class))
                return mcomp.getText();
        }
        return selectedItem.toString();
    }

    private MComponent getEditor() {
        ComboBoxEditor editor = (ComboBoxEditor) eventQueueRunner.invoke(getComboBox(), "getEditor");
        MComponent editorComponent = finder.getMComponentByComponent(editor.getEditorComponent(), "doesn't matter", null);
        return editorComponent;
    }

    public void setText(String text) {
        if (eventQueueRunner.invokeBoolean(getComboBox(), "isEditable")) {
            getEditor().setText(text, false);
        } else {
            int selectedItem = findMatchWithRetries(text);
            new FireableMouseClickEvent(getComboBox()).fire(1);
            swingWait();
            if (selectedItem == -1) {
                throw new RuntimeException("Could not find item " + text + " in Combobox " + getMComponentName());
            }
            setListText(selectedItem, accessPopupList(true));
            swingWait();
        }
    }

    private int findMatchWithRetries(final String text) {
        try {
            new Retry("Search for combobox item", ComponentFinder.getRetryInterval(), ComponentFinder.getRetryCount(),
                    new Retry.Attempt() {
                        public void perform() {
                            if (findMatch(text) == -1)
                                retry();
                        }
                    });
        } catch (Exception e) {
            return -1;
        }
        return findMatch(text);
    }

    private int findMatch(String text) {
        int selectedItem = -1;
        for (int i = 0; i < getRowCount(); i++) {
            Object itemAt = null;
            itemAt = eventQueueRunner.invoke(getComboBox(), "getItemAt", new Object[] { Integer.valueOf(i) },
                    new Class[] { Integer.TYPE });
            if (getStringRep(itemAt).equals(text)) {
                selectedItem = i;
                break;
            }
        }
        return selectedItem;
    }

    public int getRowCount() {
        return eventQueueRunner.invokeInteger(getComboBox(), "getItemCount");
    }

    private JList accessPopupList(boolean showPopup) {
        AccessibleContext accessibleContext = (AccessibleContext) eventQueueRunner.invoke(getComboBox(), "getAccessibleContext");
        ComboPopup popup = (ComboPopup) accessibleContext.getAccessibleChild(0 /* popup */);
        JList list = popup.getList();
        if (showPopup) {
            eventQueueRunner.invoke(getComboBox(), "requestFocus");
            eventQueueRunner.invoke(getComboBox(), "showPopup");
            eventQueueRunner.invoke(list, "requestFocus");
        }
        return list;
    }

    private void setListText(int index, JList list) {
        list.ensureIndexIsVisible(index);
        swingWait();
        Point p = list.getCellBounds(index, index).getLocation();
        new Snooze(50);
        new FireableMouseClickEvent(list).fire(p, 1);
        swingWait();
    }

    public String[][] getContent() {
        ComboBoxModel model = (ComboBoxModel) eventQueueRunner.invoke(getComboBox(), "getModel");
        int elementCount = model.getSize();
        String[][] content = new String[1][elementCount];
        for (int i = 0; i < elementCount; i++) {
            content[0][i] = model.getElementAt(i).toString();
        }
        return content;
    }

    public int clickNeeded(MouseEvent e) {
        return ClickAction.RECORD_NONE;
    }

    public boolean recordOnMouseRelease() {
        return true;
    }
}
