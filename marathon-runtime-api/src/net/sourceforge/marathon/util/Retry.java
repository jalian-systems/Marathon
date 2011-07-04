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

/**
 * this class sleeps for a specified time, then throws an exception if it's
 * asked to sleep beyond that time
 */
public class Retry {
    public Retry(String message, int sleepIntervalMs, int retryCount, Attempt attempt) {
        this(new RuntimeException(message), sleepIntervalMs, retryCount, attempt);
    }

    public Retry(RuntimeException exception, int sleepIntervalMs, int retryCount, Attempt attempt) {
        boolean again = true;
        do {
            try {
                attempt.perform();
                again = false;
            } catch (RetryException e) {
                if (--retryCount <= 0) {
                    throw exception;
                } else {
                    new Snooze(sleepIntervalMs);
                }
            }
        } while (again);
    }

    private static class RetryException extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }

    public static abstract class Attempt {
        public abstract void perform();

        protected final void retry() {
            throw new RetryException();
        }
    }
}
