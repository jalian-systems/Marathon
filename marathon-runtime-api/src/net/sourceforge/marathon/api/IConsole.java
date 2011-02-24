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
package net.sourceforge.marathon.api;

/**
 * Represents a place to put output of the test script, and its runtime
 * environment
 */
public interface IConsole {
    /**
     * write output of the actual test script
     */
    void writeScriptOut(char cbuf[], int off, int len);

    /**
     * write error stream of the actual test script
     */
    void writeScriptErr(char cbuf[], int off, int len);

    /**
     * write output from the application under test that was written to stdout,
     * or the equivalent
     */
    void writeStdOut(char cbuf[], int off, int len);

    /**
     * write output from the application under test that was written to stderr,
     * or the equivalent.
     */
    void writeStdErr(char cbuf[], int off, int len);

    /**
     * Clear output from console (if possible)
     */
    void clear();
}
