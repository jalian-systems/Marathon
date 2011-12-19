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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import net.sourceforge.marathon.api.IRuntimeFactory;
import net.sourceforge.marathon.providers.DisplayEventQueueProvider;
import net.sourceforge.marathon.providers.PlaybackResultProvider;
import net.sourceforge.marathon.providers.RecorderProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import atunit.AtUnit;
import atunit.Container;
import atunit.Container.Option;
import atunit.Mock;
import atunit.MockFramework;
import atunit.Unit;

import com.google.inject.Inject;

/**
 * the enabling is based on states, so let's just make sure the states
 * transition, and we can check enabling on the window itself
 */
@RunWith(AtUnit.class)
@Container(Option.GUICE)
@MockFramework(atunit.MockFramework.Option.EASYMOCK)
@Ignore
public class TestDisplayStateTransition {
    private @Unit
    @Inject
    Display display;
    private @Mock
    IDisplayView view;
    private @Mock
    IRuntimeFactory factory;
    private @Mock
    RecorderProvider recorderProvider;
    private @Mock
    PlaybackResultProvider playbackResultProvider;
    private @Mock
    DisplayEventQueueProvider displayEventQueueProvider;
    private @Mock
    DisplayEventQueue displayEventQueue;

    @Before
    public void setUp() throws Exception {
        displayEventQueueProvider.setReporter((IExceptionReporter) anyObject());
        expect(displayEventQueueProvider.get()).andReturn(displayEventQueue);
        replay(displayEventQueueProvider);
        display.setView(view);
        reset(view);
        reset(factory);
        reset(recorderProvider);
        reset(playbackResultProvider);
        reset(displayEventQueueProvider);
    }

    @After
    public void tearDown() throws Exception {
        try {
            display.destroy();
        } finally {
        }
    }

}
