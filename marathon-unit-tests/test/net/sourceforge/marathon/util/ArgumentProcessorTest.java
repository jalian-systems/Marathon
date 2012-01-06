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

package net.sourceforge.marathon.util;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class ArgumentProcessorTest {

	@Test
	public void testParseArguments() {
		ArgumentProcessor p = new ArgumentProcessor("one two three");
		List<String> arguments = p.parseArguments();
		assertEquals(3, arguments.size());
		assertEquals("one", arguments.get(0));
		assertEquals("two", arguments.get(1));
		assertEquals("three", arguments.get(2));
	}

	@Test
	public void testParseArgumentsWithSpaces() {
		ArgumentProcessor p = new ArgumentProcessor("argument-without-spaces \"argument with spaces\"");
		List<String> arguments = p.parseArguments();
		assertEquals(2, arguments.size());
		assertEquals("argument-without-spaces", arguments.get(0));
		assertEquals("argument with spaces", arguments.get(1));
	}

	@Test
	public void testParseArgumentsWithEscape() {
		ArgumentProcessor p = new ArgumentProcessor("\"This argument contains \\\" (double quote)\"");
		List<String> arguments = p.parseArguments();
		assertEquals(1, arguments.size());
		assertEquals("This argument contains \" (double quote)", arguments.get(0));
	}

	@Test
	public void testParseArgumentsEscapingANonSpecialCharacterKeepsTheEscapeCharacter() {
		ArgumentProcessor p = new ArgumentProcessor("\"This argument \\contains \\' (single quote)\"");
		List<String> arguments = p.parseArguments();
		assertEquals(1, arguments.size());
		assertEquals("This argument \\contains \\' (single quote)", arguments.get(0));
	}

}
