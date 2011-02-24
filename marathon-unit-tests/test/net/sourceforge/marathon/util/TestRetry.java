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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestRetry {
    private int attempts = 0;

    @Test
    public void testRetryFails() throws Exception {
        attempts = 0;
        SpecialException exception = new SpecialException();
        try {
            new Retry(exception, 1, 10, new Retry.Attempt() {
                public void perform() {
                    attempts++;
                    retry();
                }
            });
            fail("should have failed after 10 retries");
        } catch (RuntimeException e) {
            assertSame("did not throw my exception!", exception, e);
        }
        assertEquals(10, attempts);
    }

    private static class SpecialException extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }
}
