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
package net.sourceforge.marathon.action;

import junit.framework.Assert;
import net.sourceforge.marathon.component.ComponentFinder;

public abstract class ActionTestCase {

    protected static void testEquals(AbstractMarathonAction tag, AbstractMarathonAction sameTag, AbstractMarathonAction differentTag) {
        Assert.assertTrue(tag != null);
        Assert.assertEquals(tag, sameTag);
        Assert.assertTrue(!tag.equals(differentTag));
        Assert.assertTrue(!tag.equals(new ActionMock("bob")));
        // check hashcode
        Assert.assertEquals(tag.hashCode(), sameTag.hashCode());
    }

    public static void assertPasses(AbstractMarathonAction tag, ComponentFinder resolver) {
        tag.play(resolver);
    }

    public static void assertFails(AbstractMarathonAction tag, ComponentFinder resolver) {
        try {
            tag.play(resolver);
            Assert.fail("should have failed");
        } catch (TestException e) {
        } catch (Throwable t) {
        }
    }
}
