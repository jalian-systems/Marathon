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
package net.sourceforge.marathon.providers;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.ComponentResolver;
import net.sourceforge.marathon.component.DefaultComponentResolver;
import net.sourceforge.marathon.recorder.WindowMonitor;

import com.google.inject.Provider;

public class ResolversProvider implements Provider<List<ComponentResolver>> {

    protected ComponentFinder finder;
    protected boolean isRecording;
    protected WindowMonitor windowMonitor;

    public void setWindowMonitor(WindowMonitor windowMonitor) {
        this.windowMonitor = windowMonitor;
    }

    public void setFinder(ComponentFinder finder) {
        this.finder = finder;
    }

    public void setRecording(boolean isRecording) {
        this.isRecording = isRecording;
    }

    public List<ComponentResolver> get() {
        String prop = System.getProperty(Constants.PROP_COMPONENT_RESOLVERS, "");
        String[] resolverNames = prop.split(";");
        List<ComponentResolver> resolvers = new ArrayList<ComponentResolver>();
        for (int i = 0; i < resolverNames.length; i++) {
            if (resolverNames[i].equals(""))
                continue;
            try {
                @SuppressWarnings("unchecked")
                Class<? extends ComponentResolver> r = (Class<? extends ComponentResolver>) Class.forName(resolverNames[i]);
                Constructor<? extends ComponentResolver> cr = r.getConstructor(new Class[] { ComponentFinder.class, boolean.class,
                        WindowMonitor.class });
                ComponentResolver res = cr.newInstance(new Object[] { finder, Boolean.valueOf(isRecording), windowMonitor });
                resolvers.add(res);
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        resolvers.add(new DefaultComponentResolver(finder, isRecording, windowMonitor));
        return resolvers;
    }
}
