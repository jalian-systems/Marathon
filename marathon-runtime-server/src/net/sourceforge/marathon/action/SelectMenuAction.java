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
package net.sourceforge.marathon.action;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.KeyStroke;

import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.ComponentNotFoundException;
import net.sourceforge.marathon.component.MComponent;
import net.sourceforge.marathon.event.AWTSync;
import net.sourceforge.marathon.recorder.WindowMonitor;
import net.sourceforge.marathon.util.Retry;
import net.sourceforge.marathon.util.Snooze;

public class SelectMenuAction extends AbstractMarathonAction {
    private static final long serialVersionUID = 1L;
    private transient ArrayList<Object> menuList = null;
    private String menuItems = null;
    private KeyStroke ks = null;
    private String keyStrokeText = null;

    public SelectMenuAction(ArrayList<Object> menuList, IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        super(new ComponentId("SelectMenuAction"), scriptModel, windowMonitor);
        this.menuList = menuList;
    }

    public SelectMenuAction(String menuItems, String keyStrokeText, IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        super(new ComponentId("SelectMenuAction"), scriptModel, windowMonitor);
        this.menuItems = menuItems;
        this.keyStrokeText = keyStrokeText;
    }

    public SelectMenuAction(ArrayList<Object> menuList, KeyStroke ks, IScriptModelServerPart scriptModel,
            WindowMonitor windowMonitor) {
        super(new ComponentId("SelectMenuAction"), scriptModel, windowMonitor);
        this.menuList = menuList;
        this.ks = ks;
    }

    public void play(final ComponentFinder resolver) {
        if (keyStrokeText == null) {
            try {
                RuntimeException err = new ComponentNotFoundException("couldn't open menu " + menuItems, scriptModel, windowMonitor);
                new Retry(err, ComponentFinder.getRetryInterval(), ComponentFinder.getRetryCount(), new Retry.Attempt() {
                    public void perform() {
                        String[] items = menuItems.split("\\>\\>");
                        ArrayList<ComponentId> ids = new ArrayList<ComponentId>();
                        for (int i = 0; i < items.length; i++) {
                            String s = items[i];
                            ComponentId ci = new ComponentId(s);
                            ids.add(ci);
                        }
                        for (int i = 0; i < ids.size(); i++) {
                            if (i > 0)
                                new Snooze(delayInMS);

                            ComponentId id = (ComponentId) ids.get(i);

                            MComponent mc = resolver.getMComponentById(id);
                            Component c = null;
                            if (mc != null) {
                                c = mc.getComponent();
                                if (c instanceof JMenu && (((JMenu) c).getPopupMenu().getComponentCount() == 0 || ((JMenu) c).getPopupMenu().isShowing())) {
                                    continue;
                                }
                            }

                            new ClickAction(id, scriptModel, windowMonitor).play(resolver, 3); // few
                            // retries

                            // Wait for the menu to show.
                            // This is necessary because otherwise the
                            // wrong component (e.g.
                            // a button with the same name in a parent
                            // menu) could get clicked.
                            if (c instanceof JMenu) {
                                for (int j = 0; j < 100; j++) {
                                    if (((JMenu) c).getPopupMenu().isShowing())
                                        break;
                                    new Snooze(10);
                                    AWTSync.sync();
                                }
                            }
                        }
                    }
                });
            } catch (TestException e) {
                e.captureScreen();
                throw e;
            }
        } else {
            new KeyStrokeAction(keyStrokeText, scriptModel, windowMonitor).play(resolver);
        }
    }

    public String toScriptCode() {
        return scriptModel.getScriptCodeForSelectMenu(ks, menuList);
    }

}
