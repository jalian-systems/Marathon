/*******************************************************************************
 *  
 *  $Id: OSXUtil.java 84 2008-11-13 11:20:38Z kd $
 *  Copyright (C) 2006 Jalian Systems Private Ltd.
 *  Copyright (C) 2006 Contributors to Marathon OSS Project
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 *  Project website: http://www.marathontesting.com
 *  Help: Marathon help forum @ http://groups.google.com/group/marathon-testing
 * 
 *******************************************************************************/
package net.sourceforge.marathon.util.osx;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;

@SuppressWarnings("deprecation") public class OSXUtil {
    private Application application = Application.getApplication();
    private IOSXApplicationListener window;

    public OSXUtil(IOSXApplicationListener window) {
        this.window = window;
        application.setEnabledAboutMenu(true);
        application.setEnabledPreferencesMenu(true);
        application.addApplicationListener(new ApplicationListener() {
            public void handleAbout(ApplicationEvent arg0) {
                OSXUtil.this.window.handleAbout();
                arg0.setHandled(true);
            }

            public void handleOpenApplication(ApplicationEvent arg0) {
            }

            public void handleOpenFile(ApplicationEvent arg0) {
            }

            public void handlePreferences(ApplicationEvent arg0) {
                OSXUtil.this.window.handlePreferences();
                arg0.setHandled(true);
            }

            public void handlePrintFile(ApplicationEvent arg0) {
            }

            public void handleQuit(ApplicationEvent arg0) {
                arg0.setHandled(OSXUtil.this.window.handleQuit());
            }

            public void handleReOpenApplication(ApplicationEvent arg0) {
            }
        });
    }
}
