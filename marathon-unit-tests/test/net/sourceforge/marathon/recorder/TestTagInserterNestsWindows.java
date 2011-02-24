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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import net.sourceforge.marathon.api.IScriptElement;
import net.sourceforge.marathon.component.WindowIdMock;

import org.junit.Before;
import org.junit.Test;

public class TestTagInserterNestsWindows {
    private WindowElement w1, w2, w3;
    private TagInserter inserter;
    private int temporaryWindowIndex;

    @Before
    public void setUp() throws Exception {
        inserter = new TagInserter();
    }

    @Test
    public void testWindowChanged() {
        inserter.add(w1 = window("window1"), mock());
        inserter.add(w2 = window("window2"), mock());
        assertSame(w2, inserter.getRootTag().getChildren().get(1));
    }

    @Test
    public void testNestedWindow() {
        IScriptElement child = null;
        inserter.add(w1 = window("window1"), mock());
        inserter.add(w2 = window("window2", "window1"), mock());
        inserter.add(window("window2", "window1"), mock());
        inserter.add(w3 = window("window1"), child = mock());
        assertEquals(2, w2.getChildren().size());
        assertSame(w2, w1.getChildren().get(1));
        assertSame(w1, inserter.getRootTag().getChildren().last());
        assertSame(child, w1.getChildren().last());
    }

    @Test
    public void testNestedWindowChanged() {
        inserter.add(w1 = window("window1"), mock());
        inserter.add(w2 = window("window2", "window1"), mock());
        inserter.add(w3 = window("window3", "window1"), mock());
        assertEquals(3, w1.getChildren().size());
        assertSame(w3, w1.getChildren().get(2));
    }

    @Test
    public void testNestedWindowCloses() {
        inserter.add(w1 = window("window1"), mock());
        inserter.add(w2 = window("window2", "window1"), mock());
        inserter.add(w3 = window("window3"), mock());
        assertSame(w3, inserter.getRootTag().getChildren().get(1));
    }

    @Test
    public void testDeeplyNestedWindowCloses() {
        inserter.add(w1 = window("window1"), mock());
        inserter.add(window("window2", "window1"), mock());
        inserter.add(window("window3", "window2"), mock());
        inserter.add(window("window4", "window3"), mock());
        inserter.add(w2 = window("window5", "window1"), mock());
        assertSame(w2, w1.getChildren().get(2));
    }

    private WindowElement window(String title) {
        return new WindowElement(new WindowIdMock(title));
    }

    private WindowElement window(String title, String parentTitle) {
        return new WindowElement(new WindowIdMock(title, parentTitle));
    }

    private RecordableMock mock() {
        return new RecordableMock("foo" + ++temporaryWindowIndex);
    }
}
