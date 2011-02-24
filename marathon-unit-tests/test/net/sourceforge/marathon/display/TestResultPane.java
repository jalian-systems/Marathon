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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import javax.swing.ListSelectionModel;

import net.sourceforge.marathon.api.Failure;
import net.sourceforge.marathon.api.PlaybackResult;
import net.sourceforge.marathon.api.SourceLine;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import atunit.AtUnit;
import atunit.Container;
import atunit.Container.Option;
import atunit.MockFramework;
import atunit.Unit;

@RunWith(AtUnit.class)
@Container(Option.GUICE)
@MockFramework(atunit.MockFramework.Option.EASYMOCK)
public class TestResultPane {
    private @Unit
    ResultPane dialog;
    private PlaybackResult result;

    @Before
    public void setUp() throws Exception {
        result = new PlaybackResult();
        result.addFailure("poobag", createTraceback("poobag"));
        result.addFailure("dudebag", createTraceback("dudebag"));
        result.addFailure("handbag", createTraceback("handbag"));
        result.addFailure("dimebag", createTraceback("dimebag"));
        dialog = new ResultPane();
        dialog.addResult(result);
    }

    @After
    public void tearDown() throws Exception {
    }

    private SourceLine[] createTraceback(String id) {
        return new SourceLine[] { new SourceLine(id + "file1", id + "func1", 5), new SourceLine(id + "file2", id + "func2", 5),
                new SourceLine(id + "file3", id + "func3", 5), };
    }

    /**
     * the activity of this dialog is driven off of the playback
     * events(failures, etc...) contained in the summary list as such, it is
     * important that this list get populated correctly off of the playback
     * results.
     */
    @Test
    public void testPopulationOfPlaybackEventSummary() throws Exception {
        assertEquals("event summary size", 4, dialog.getEventSize());
        assertSummaryValue(result, dialog);
    }

    /**
     * we only have one event that can be focused at a time, therefore let's
     * enforce that.
     */
    @Test
    public void testSelectionMode() throws Exception {
        assertEquals("event selection mode", ListSelectionModel.SINGLE_SELECTION, dialog.getSelectionMode());
    }

    /**
     * When the dialog first has focus, which event details are showing
     * initially?
     * 
     */
    @Test
    public void testInitialStateOfSelectedEvent() throws Exception {
        assertEquals("selected event", true, (dialog.selectedResult != null));
        assertEquals("selected message content", "poobag", dialog.selectedResult.getMessage());
        SourceLine[] selectedTraceback = dialog.selectedResult.getTraceback();
        assertEquals("selected traceback size", 3, selectedTraceback.length);
        assertSelectedTraceBack("initial traceback", result.failures()[0].getTraceback(), selectedTraceback);
    }

    /**
     * sometimes, especially in the case of a successful playback in which there
     * were no errors or assertion failures, the playback result will be empty.
     * We must account for this. This should not throw errors, and should let
     * the user know what's up
     */
    @Test
    public void testResultDialogWithNoEvents() throws Exception {
        dialog = new ResultPane();
        dialog.addResult(new PlaybackResult());
        assertEquals("successful playback message", "No Errors", dialog.getMessageText());
    }

    /**
     * select the various events from the results, and make sure that as each
     * event is selected, that its
     */
    @Test
    public void testSelectingADifferentEventFromTheSummaryCausesThatEventToBecomeSelected() throws Exception {
        checkSelectionChange(2);
        checkSelectionChange(3);
        checkSelectionChange(0);
        checkSelectionChange(1);
    }

    @Test
    public void testSetResult() throws Exception {
        result = new PlaybackResult();
        result.addFailure("poobag_new", createTraceback("poobag_new"));
        dialog.clear();
        dialog.addResult(result);
        assertEquals("event summary size", 1, dialog.getEventSize());
        assertEquals("error playback message", "1 error", dialog.getMessageText());
        result.addFailure("dudebag_new", createTraceback("dudebag_new"));
        result.addFailure("handbag_new", createTraceback("handbag_new"));
        dialog.clear();
        dialog.addResult(result);
        assertEquals("event summary size", 3, dialog.getEventSize());
        assertEquals("selected event", true, (dialog.selectedResult != null));
        assertEquals("selected message content", "poobag_new", dialog.selectedResult.getMessage());
        assertEquals("error playback message", "3 errors", dialog.getMessageText());
    }

    private void checkSelectionChange(int index) {
        dialog.setSelectionIndex(index, index);
        Failure event = result.failures()[index];
        assertEquals("selected event message", event.getMessage(), dialog.selectedResult.getMessage());
        assertSelectedTraceBack("selected event traceback", event.getTraceback(), dialog.selectedResult.getTraceback());
    }

    private void assertSelectedTraceBack(String message, SourceLine[] expected, SourceLine[] selectedTraceback) {
        assertEquals(message + " size", expected.length, selectedTraceback.length);
        for (int i = 0; i < expected.length; i++) {
            SourceLine sourceLine = expected[i];
            assertEquals(message + " element " + i, sourceLine, selectedTraceback[i]);
        }
    }

    private void assertSummaryValue(PlaybackResult result, ResultPane dialog) {
        for (int i = 0; i < result.failures().length; i++) {
            Failure failure = result.failures()[i];
            assertSame("event " + i, failure, dialog.pbResult.getFailureAt(i));
        }
    }
}
