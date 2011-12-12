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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.marathon.action.ClickAction;
import net.sourceforge.marathon.action.SelectAction;
import net.sourceforge.marathon.api.IRecorder;
import net.sourceforge.marathon.recorder.RecordingEventListener;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.Retry;

public class MTabbedPane extends MCollectionComponent {

    private final class TabbedPaneChangeListener implements ChangeListener {

        private final Component c;

        public TabbedPaneChangeListener(Component c) {
            this.c = c;
        }

        public void stateChanged(ChangeEvent e) {
            if (!(e.getSource() instanceof Component) || !RecordingEventListener.getInstance().isRecording())
                return;
            final MComponent component = finder.getMComponentByComponent((Component) e.getSource());
            if (component == null)
                return;
            IRecorder recorder = RecordingEventListener.getInstance().getRecorder();
            String title = component.getText();
            if (title == null)
                return;
            recorder.record(new SelectAction(component.getComponentId(), title, finder.getScriptModel(), windowMonitor)
                    .enscript(component));
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((c == null) ? 0 : c.hashCode());
            return result;
        }

        @Override public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TabbedPaneChangeListener other = (TabbedPaneChangeListener) obj;
            if (c == null) {
                if (other.c != null)
                    return false;
            } else if (!c.equals(other.c))
                return false;
            return true;
        }
    }

    private ChangeListener changeListener;

    public MTabbedPane(JTabbedPane component, String name, ComponentFinder finder, WindowMonitor windowMonitor) {
        this(component, name, finder, windowMonitor, true);
    }
    
    public MTabbedPane(JTabbedPane component, String name, ComponentFinder finder, WindowMonitor windowMonitor, boolean addListener) {
        super(component, name, finder, windowMonitor);
        if (addListener) {
            changeListener = new TabbedPaneChangeListener(component);
            if (RecordingEventListener.getInstance() != null
                    && !(Arrays.asList(component.getChangeListeners()).contains(changeListener))) {
                component.addChangeListener(changeListener);
                component.addComponentListener(new ComponentListener() {

                    public void componentShown(ComponentEvent e) {
                    }

                    public void componentResized(ComponentEvent e) {
                    }

                    public void componentMoved(ComponentEvent e) {
                    }

                    public void componentHidden(ComponentEvent e) {
                        ((JTabbedPane) e.getComponent()).removeChangeListener(changeListener);
                    }
                });
            }
        }
    }
    

    public void setText(String text) {
        int index = getTab(text);
        if (index == -1) {
            throw new ComponentException(text + " is not a valid tab title.", finder.getScriptModel(), windowMonitor);
        }
        eventQueueRunner.invoke(getTabbedPane(), "setSelectedIndex", new Object[] { Integer.valueOf(index) },
                new Class[] { Integer.TYPE });
        swingWait();
    }

    private int getTab(final String text) {
        try {
            new Retry("Search for cell component", ComponentFinder.getRetryInterval(), ComponentFinder.getRetryCount(),
                    new Retry.Attempt() {
                        public void perform() {
                            if (findTab(text) == -1)
                                retry();
                        }
                    });
        } catch (Exception e) {
            return -1;
        }

        return findTab(text);
    }

    private int findTab(String text) {
        int tabCount = getRowCount();
        for (int i = 0; i < tabCount; i++)
            if (text.equals(getTabName(i)))
                return i;
        return -1 ;
    }

    public String getText() {
        int selectedIndex = eventQueueRunner.invokeInteger(getTabbedPane(), "getSelectedIndex");
        if (selectedIndex == -1)
            return null;
        return getTabName(selectedIndex);
    }

    protected JTabbedPane getTabbedPane() {
        JTabbedPane pane = (JTabbedPane) getComponent();
        return pane;
    }

    public int getRowCount() {
        return eventQueueRunner.invokeInteger(getTabbedPane(), "getTabCount");
    }

    public String[][] getContent() {
        String[][] content = new String[1][getRowCount()];
        for (int i = 0; i < content[0].length; i++) {
            content[0][i] = getTabName(i);
        }
        return content;
    }

    private String getTabName(int i) {
        String tabTitle = (String) eventQueueRunner.invoke(getTabbedPane(), "getTitleAt", new Object[] { Integer.valueOf(i) },
                new Class[] { Integer.TYPE });
        if (tabTitle == null || "".equals(tabTitle))
            return getTabIconName(i);
        return tabTitle;
    }

    private String getTabIconName(int i) {
        Icon icon = (Icon) eventQueueRunner.invoke(getTabbedPane(), "getIconAt", new Object[] { Integer.valueOf(i) },
                new Class[] { Integer.TYPE });
        if (icon == null || !(icon instanceof ImageIcon))
            return "tabIndex-" + i ;
        String description = ((ImageIcon) icon).getDescription();
        if (description == null || description.length() == 0)
            return "tabIndex-" + i ;
        return PropertyWrapper.mapFromImageDescription(description);
    }

    public int clickNeeded(MouseEvent e) {
        return ClickAction.RECORD_NONE;
    }

    public boolean isFocusNeeded() {
        return false;
    }

    public boolean recordOtherKeys() {
        return false;
    }

    public boolean effectsWindowName() {
        return true;
    }
}
