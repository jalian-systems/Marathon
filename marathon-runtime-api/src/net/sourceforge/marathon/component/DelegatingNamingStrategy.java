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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import net.sourceforge.marathon.Constants;
import net.sourceforge.marathon.api.RuntimeLogger;

public class DelegatingNamingStrategy implements INamingStrategy {

    private static final Logger logger = Logger.getLogger(DelegatingNamingStrategy.class.getName());

    private static final INamingStrategy namingStrategy = create();

    public void setTopLevelComponent(Component pcontainer) {
        namingStrategy.setTopLevelComponent(pcontainer);
    }

    public Component getComponent(String name, int retryCount, boolean isContainer) {
        return namingStrategy.getComponent(name, retryCount, isContainer);
    }

    public String getName(Component component) {
        return namingStrategy.getName(component);
    }

    public String getVisibleComponentNames() {
        return namingStrategy.getVisibleComponentNames();
    }

    public Map<String, Component> getAllComponents() {
        return namingStrategy.getAllComponents();
    }

    @SuppressWarnings("unchecked") private static INamingStrategy create() {
        String s = Constants.getNSClassName();
        try {
            logger.info("Creating naming strategy: " + s);
            return ((Class<? extends INamingStrategy>) ((Class<? extends INamingStrategy>) Class.forName(s))).newInstance();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unable to create naming strategy: " + s, "Error creating Naming Strategy",
                    JOptionPane.ERROR_MESSAGE);
            StringWriter sr = new StringWriter();
            PrintWriter pr = new PrintWriter(sr);
            pr.println("Unable to create naming strategy: " + s);
            e.printStackTrace(pr);
            RuntimeLogger.getRuntimeLogger().error("Naming Strategy", "Unable to create naming strategy: " + s, sr.toString());
            System.exit(0);
        }
        return null;
    }

    public void saveIfNeeded() {
        namingStrategy.saveIfNeeded();
    }

    public boolean isObjectMapNamingStrategy() {
        return namingStrategy.getClass().getName().equals(Constants.DEFAULT_NAMING_STRATEGY);
    }

    public INamingStrategy getInstance() {
        return namingStrategy;
    }

    public void markUsed(String name) {
        namingStrategy.markUsed(name);
    }

    public void init() {
        namingStrategy.init();
    }

}
