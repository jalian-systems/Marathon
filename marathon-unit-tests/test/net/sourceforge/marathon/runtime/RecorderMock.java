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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import net.sourceforge.marathon.api.IRecorder;
import net.sourceforge.marathon.api.IScriptElement;
import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.recorder.InsertChecklistElement;
import net.sourceforge.marathon.recorder.InsertScriptElement;
import net.sourceforge.marathon.recorder.ShowChecklistElement;

public class RecorderMock implements IRecorder {
    List<IScriptElement> builtTags = new ArrayList<IScriptElement>();
    private int index = 0;

    public void record(IScriptElement recordable) {
        builtTags.add(recordable);
    }

    public void assertNext(Object expected) {
        Assert.assertTrue("list is empty", index < builtTags.size());
        Assert.assertEquals(expected, builtTags.get(index++));
    }

    public void assertEmpty() {
        Assert.assertTrue("list should be empty, but isn't", index == builtTags.size());
    }

    public void abortRecording() {
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (Iterator<IScriptElement> iter = builtTags.iterator(); iter.hasNext();) {
            String element = iter.next().toString();
            buffer.append(element);
        }
        return buffer.toString();
    }

    public void insertChecklist(String name) {
    }

    public String recordInsertScriptElement(WindowId windowId, String function) {
        record(new InsertScriptElement(windowId, function));
        return "";
    }

    public void recordInsertChecklistElement(WindowId windowId, String fileName) {
        record(new InsertChecklistElement(windowId, fileName));
    }

    public void recordShowChecklistElement(WindowId windowId, String fileName) {
        record(new ShowChecklistElement(windowId, fileName));
    }
}
