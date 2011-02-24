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

public class TrivialFixture {

    public static String convertCode(String[] code, String[] codeTest) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < code.length; i++) {
            sb.append(code[i]).append("\n");
        }
        for (int i = 0; i < codeTest.length; i++) {
            sb.append(codeTest[i]).append("\n");
        }
        return sb.toString();
    }

    public static String[] codeDummyTest = { "def test():", "    pass" };

    public static String[] codeFixture = { "class Fixture:", "    def setup(self):", "        from javax.swing import JFrame", "",
            "        self.f = JFrame('Trivial Fixture')", "        self.f.pack()", "        self.f.setVisible(1)", "",
            "    def teardown(self):", "        self.f.setVisible(0)", "", "fixture = Fixture()", "" };
}
