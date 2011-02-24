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

import java.io.IOException;
import java.io.Writer;

public abstract class ConsoleWriter extends Writer {
    private char[] cb;
    private int nChars = 1024, nextChar = 0;
    private Writer writer;

    public ConsoleWriter(Writer adapter) {
        super();
        cb = new char[nChars];
        writer = adapter;
    }

    public void close() throws IOException {
    }

    public void flush() throws IOException {
        synchronized (lock) {
            writer.write(cb, 0, nextChar);
            nextChar = 0;
        }
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        for (int i = 0; i < len; i++)
            addCharToBuffer(cbuf[i]);
    }

    private void addCharToBuffer(char c) throws IOException {
        synchronized (lock) {
            cb[nextChar] = c;
            nextChar++;
            if (nextChar == nChars || c == '\n') {
                flush();
            }
        }
    }
}
