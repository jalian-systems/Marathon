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
package net.sourceforge.marathon.runtime;

import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import net.sourceforge.marathon.api.IConsole;
import net.sourceforge.marathon.api.IJavaRuntimeInstantiator;
import net.sourceforge.marathon.api.IMarathonRuntime;

public class JavaRuntimeInstantiatorImpl implements IJavaRuntimeInstantiator {
    private String[] args;
    private JavaRuntime runtime;

    public JavaRuntimeInstantiatorImpl(String[] args) {
        this.args = args;
    }

    public IMarathonRuntime createRuntime(IConsole console) {
        runtime = new JavaRuntime(console, args);
        return runtime;
    }

    public void setProperties(Properties properties) {
        if (properties != null) {
            Properties sysprops = System.getProperties();
            sysprops.putAll(properties);
            System.setProperties(sysprops);
        }
        try {
            Logger.getLogger(JavaRuntimeInstantiatorImpl.class.getName()).info("Using logging configuration file: " + System.getProperty("java.util.logging.config.file"));
            LogManager.getLogManager().readConfiguration();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
