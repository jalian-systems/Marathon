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
package net.sourceforge.marathon.junit;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.sourceforge.marathon.api.IConsole;
import net.sourceforge.marathon.api.ScriptModelClientPart;

public class MarathonDDTestSuite extends TestSuite implements Test {
    private final File file;
    private String suffix = ScriptModelClientPart.getModel().getSuffix();
    private DDTestRunner ddt;

    public MarathonDDTestSuite(File file, boolean acceptChecklist, IConsole console) throws IOException {
        this.file = file;
        ddt = new DDTestRunner(console, file);
        while (ddt.hasNext()) {
            ddt.next();
            MarathonTestCase testCase = new MarathonTestCase(file, acceptChecklist, console, ddt.getDataVariables(), ddt.getName());
            super.addTest(testCase);
        }
    }

    public String getName() {
        String name = file.getName();

        if (name.endsWith(suffix)) {
            name = name.substring(0, name.length() - suffix.length());
        }
        return name;
    }

    @Override public String toString() {
        return getName();
    }

}
