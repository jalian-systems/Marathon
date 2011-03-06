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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.Retry;

/**
 * <code>MCollectionComponent</code> represents a collection of
 * <code>MCellComponent</code>s. Note that a <code>MCollectionComponent</code>
 * can created by itself without associating with <code>MCellComponent</code>.
 * 
 * See {@link MComboBox} for example.
 */

public abstract class MCollectionComponent extends MComponent {
    /**
     * Constructs a <code>MCollectionComponent</code> for a component with the
     * given name.
     * 
     * @param component
     * @param name
     * @param windowMonitor
     */
    public MCollectionComponent(Component component, String name, ComponentFinder finder, WindowMonitor windowMonitor) {
        super(component, name, finder, windowMonitor);
    }

    /**
     * Get the number of elements in the component.
     * 
     * @return
     */
    public abstract int getRowCount();

    /**
     * Get the content in string form. Each String[] represents a row.
     * 
     * @return content
     */
    public abstract String[][] getContent();

    public MComponent findMatchingComponent(final Properties props) {
        try {
            new Retry("Search for cell component", ComponentFinder.getRetryInterval(), ComponentFinder.getRetryCount(),
                    new Retry.Attempt() {
                        public void perform() {
                            if (findMatch(props) == null)
                                retry();
                        }
                    });
        } catch (Exception e) {
            return null;
        }
        return findMatch(props);
    }

    private MComponent findMatch(Properties props) {
        if (props.size() == 0)
            return null;
        Iterator<MComponent> iter = iterator();
        while (iter.hasNext()) {
            MComponent item = iter.next();
            if (item.matched(props))
                return item;
        }
        return null;
    }

    public Iterator<MComponent> iterator() {
        return null;
    }

    public boolean hasDuplicates() {
        Iterator<MComponent> iter = iterator();
        if (iter == null)
            return true;
        ArrayList<String> listText = new ArrayList<String>();
        while (iter.hasNext()) {
            MComponent item = iter.next();
            String text = item.getText();
            if (text == null)
                return true;
            listText.add(text);
        }
        Collections.sort(listText);
        for (int i = 1; i < listText.size(); i++) {
            if (listText.get(i).equals(listText.get(i - 1)))
                return true;
        }
        return false;
    }

    public void setCellSelection(Properties[] properties, IScriptModelServerPart scriptModel) {
        throw new ComponentException("Array of properties not implemented for this component", scriptModel, windowMonitor);
    }
}
