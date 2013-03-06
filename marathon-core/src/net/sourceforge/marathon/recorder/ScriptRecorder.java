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
package net.sourceforge.marathon.recorder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.SwingUtilities;

import net.sourceforge.marathon.api.IRecorder;
import net.sourceforge.marathon.api.IScriptElement;
import net.sourceforge.marathon.api.WindowId;

public class ScriptRecorder implements IRecorder {
    private IScriptListener scriptListener;
    private TagInserter tagInserter = new TagInserter();

    private final BlockingQueue<RecordEvent> recordEvents = new LinkedBlockingQueue<RecordEvent>();

    private static class RecordEvent {
        IScriptElement recordable;

        public RecordEvent(IScriptElement recordable) {
            this.recordable = recordable;
        }

        public IScriptElement getRecordable() {
            return recordable;
        }
    }

    private final Runnable processRecordEventsRunnable = new Runnable() {
        public void run() {
            RecordEvent evt;

            while ((evt = recordEvents.poll()) != null) {
                IScriptElement recordable = evt.getRecordable();
                if (recordable.getWindowId() == null)
                    tagInserter.add(recordable);
                else {
                    WindowElement windowTag = new WindowElement(recordable.getWindowId());
                    tagInserter.add(windowTag, recordable);
                }
                updateScript();
            }
        }
    };

    public ScriptRecorder(IScriptListener scriptListener) {
        this.scriptListener = scriptListener;
    }

    public void record(IScriptElement recordable) {
        recordEvents.add(new RecordEvent(recordable));
        if (recordEvents.size() == 1) {
            SwingUtilities.invokeLater(processRecordEventsRunnable);
        }
    }

    public void updateScript() {
        if (scriptListener != null) {
            synchronized (scriptListener) {
                scriptListener.setScript(toScriptCode());
            }
        }
    }

    private String toScriptCode() {
        return tagInserter.getRootTag().toScriptCode();
    }

    public void abortRecording() {
        scriptListener.abortRecording();
    }

    public void insertChecklist(String name) {
        scriptListener.insertChecklistAction(name);
    }

    public String recordInsertScriptElement(WindowId windowId, String function) {
        InsertScriptElement recordable = new InsertScriptElement(windowId, function);
        record(recordable);
        String ims = recordable.getImportStatement();
        if (scriptListener != null) {
            scriptListener.addImportStatement(ims);
        }
        return ims;
    }

    public void recordInsertChecklistElement(WindowId topWindowId, String fileName) {
        record(new InsertChecklistElement(topWindowId, fileName));
    }

    public void recordShowChecklistElement(WindowId windowId, String fileName) {
        record(new ShowChecklistElement(windowId, fileName));
    }

    public boolean isCreatingObjectMap() {
        // TODO Auto-generated method stub
        return false;
    }
}
