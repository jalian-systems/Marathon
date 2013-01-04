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
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import net.sourceforge.marathon.api.ComponentId;
import net.sourceforge.marathon.api.IScriptModelServerPart;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.INamingStrategy;
import net.sourceforge.marathon.event.IPredicate;
import net.sourceforge.marathon.recorder.WindowMonitor;

public class ScreenCaptureAction extends AbstractMarathonAction {
    private static final long serialVersionUID = 1L;
    private String fileName;
    private String imageType;
    private String windowName = null;

    public ScreenCaptureAction(String fileName, IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        super(new ComponentId("ScreenCaptureAction"), scriptModel, windowMonitor);
        this.fileName = fileName;
        imageType = fileName.replaceAll("[^\\.]*\\.", "");
        if ("".equals(imageType)) {
            imageType = "png";
            this.fileName = fileName + ".png";
        }
    }

    public ScreenCaptureAction(String fileName, String windowName, IScriptModelServerPart scriptModel, WindowMonitor windowMonitor) {
        this(fileName, scriptModel, windowMonitor);
        this.windowName = windowName;
    }

    public void play(ComponentFinder resolver) {
        try {
            Rectangle rectangle;
            if (windowName == null) {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                rectangle = new Rectangle(0, 0, screenSize.width, screenSize.height);
            } else {
                IPredicate windowTest = new SameTitle(windowName, windowMonitor.getNamingStrategy());
                Window window = windowMonitor.getWindow(windowTest);
                Dimension windowSize = window.getSize();
                rectangle = new Rectangle(window.getX(), window.getY(), windowSize.width, windowSize.height);
            }
            Robot robot = new Robot();
            BufferedImage image = robot.createScreenCapture(rectangle);
            File file;
            file = new File(fileName);
            ImageIO.write(image, imageType, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String toScriptCode() {
        return scriptModel.getScriptCodeForCapture(windowName, fileName);
    }

    private static class SameTitle implements IPredicate {
        private final String title;
        private final INamingStrategy<Component> namingStrategy;

        public SameTitle(String title, INamingStrategy<Component> namingStrategy) {
            this.title = title;
            this.namingStrategy = namingStrategy;
        }

        public boolean evaluate(Object obj) {
            if (title.startsWith("/") && !title.startsWith("//")) {
                if (!Pattern.matches(title.substring(1), namingStrategy.getName((Window) obj)))
                    return false;
            } else {
                String titleString = title;
                if (title.startsWith("//"))
                    titleString = title.substring(1);
                if (!titleString.equals(namingStrategy.getName((Window) obj)))
                    return false;
            }
            return true;
        }

        public String toString() {
            return title;
        }

    }
}
