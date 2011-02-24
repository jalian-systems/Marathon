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
package net.sourceforge.marathon.display;

import net.sourceforge.marathon.api.PlaybackResult;
import net.sourceforge.marathon.api.SourceLine;

public interface IDisplayView {
    void setError(Throwable exception, String message);

    void setState(State state);

    IStdOut getOutputPane();

    void setResult(PlaybackResult result);

    int trackProgress(SourceLine line, int line_reached);

    String getScript();

    String getFilePath();

    void insertScript(String script);

    void trackProgress();

    void startInserting();

    void stopInserting();

    boolean isDebugging();

    int acceptChecklist(String fileName);

    int showChecklist(String fileName);

    void insertChecklistAction(String name);

    void endTestRun();

    void endTest(PlaybackResult result);

    void startTestRun();

    void startTest();

    void addImport(String ims);

    void updateOMapFile();
}
