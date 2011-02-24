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
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JMenu;
import javax.swing.JSeparator;

import net.sourceforge.marathon.event.FireableMouseClickEvent;
import net.sourceforge.marathon.recorder.WindowMonitor;

public class MMenu extends MCollectionComponent {
    public MMenu(Component component, String name, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(component, name, finder, windowMonitor);
    }

    public JMenu getJMenu() {
        return (JMenu) getComponent();
    }

    public int getRowCount() {
        return getRowCount(getJMenu(), 0);
    }

    private int getRowCount(JMenu menu, int count) {
        Component[] items = (Component[]) eventQueueRunner.invoke(menu, "getMenuComponents");
        for (int i = 0; i < items.length; i++) {
            if (items[i] instanceof JMenu)
                count = getRowCount((JMenu) items[i], count);
            if (!(items[i] instanceof JSeparator))
                count++;
        }
        return count;
    }

    public String[][] getContent() {
        List<String> v = new Vector<String>();
        getContent(getJMenu(), v);
        String[][] result = new String[1][v.size()];
        String[] content = new String[v.size()];
        v.toArray(content);
        result[0] = content;
        return result;
    }

    private void getContent(JMenu menu, List<String> v) {
        Component[] items = (Component[]) eventQueueRunner.invoke(menu, "getMenuComponents");
        for (int i = 0; i < items.length; i++) {
            if (!(items[i] instanceof AbstractButton))
                continue;
            AbstractButton item = (AbstractButton) items[i];
            v.add((item.isEnabled() ? "Enabled," : "Disabled,") + item.getText());
            if (item instanceof JMenu)
                getContent((JMenu) item, v);
        }
    }

    public void click(int numberOfClicks, boolean isPopupTrigger) {
        swingWait();
        new FireableMouseClickEvent(getComponent(), numberOfClicks, isPopupTrigger).fire();
        swingWait();
    }

    public String getText() {
        return (String) eventQueueRunner.invoke(getJMenu(), "getText");
    }
}
