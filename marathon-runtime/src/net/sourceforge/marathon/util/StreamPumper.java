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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

public class StreamPumper implements Runnable {
    private static int instanceCount = 0;
    private InputStreamReader in;
    private Writer writer;
    private Thread pumpingThread;

    public StreamPumper(InputStream in, Writer writer) {
        pumpingThread = new Thread(this, "Stream Pumper " + instanceNumber());
        this.in = new InputStreamReader(in);
        this.writer = writer == null ? new BitBucket() : writer;
    }

    private static synchronized int instanceNumber() {
        return instanceCount++;
    }

    public void run() {
        char[] cbuf = new char[1024];
        try {
            while (true) {
                int n = in.read(cbuf);
                if (n != -1) {
                    writeChar(cbuf, n);
                } else {
                    return;
                }
            }
        } catch (IOException e) {
            // No need to print stack trace - the application must have quit
            // e.printStackTrace();
        }
    }

    private void writeChar(char[] cbuf, int n) throws IOException {
        synchronized (this.writer) {
            writer.write(cbuf, 0, n);
        }
    }

    public void start() {
        pumpingThread.start();
    }

    public void setWriter(Writer writer) {
        synchronized (this) {
            this.writer = writer;
        }
    }
}
