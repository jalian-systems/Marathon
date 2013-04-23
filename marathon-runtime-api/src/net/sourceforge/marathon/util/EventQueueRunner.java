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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import net.sourceforge.marathon.api.InterruptionError;
import net.sourceforge.marathon.api.MarathonException;

public class EventQueueRunner {

    private volatile Throwable throwException = null;
    private volatile Object returnValue;

    public Object invoke(final Object object, String methodName, final Object[] parameters, Class<?>[] types) {

        throwException = null;
        Method method;
        try {
            method = object.getClass().getMethod(methodName, types);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        return invokeMethod(method, object, parameters);
    }

    public Object invokeMethod(final Method method, final Object object, final Object[] parameters) {
        Runnable doRun = new Runnable() {
            public void run() {
                boolean accessible = method.isAccessible();
                try {
                    method.setAccessible(true);
                    returnValue = method.invoke(object, parameters);
                } catch (IllegalArgumentException e) {
                    throwException = e;
                } catch (IllegalAccessException e) {
                    throwException = e;
                } catch (InvocationTargetException e) {
                    throwException = e.getTargetException();
                } finally {
                    method.setAccessible(accessible);
                }
            }
        };
        throwException = null ;
        if (SwingUtilities.isEventDispatchThread()) {
            doRun.run();
        } else {
            try {
                final Thread currentThread = Thread.currentThread();
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override public void run() {
                        currentThread.interrupt();
                    }
                }, 60000);
                SwingUtilities.invokeAndWait(doRun);
                t.cancel();
            } catch (InterruptedException e) {
                throw new InterruptionError();
            } catch (InvocationTargetException e) {
                Throwable err = e.getTargetException();
                if (err instanceof RuntimeException)
                    throw (RuntimeException) err;
                if (err instanceof Error)
                    throw (Error) err;
                throw new MarathonException("error during swing invocation", err);
            }
        }
        if (throwException != null)
            throw new RuntimeException(throwException);
        return returnValue;
    }

    public Object invoke(Object object, String methodName) {
        return invoke(object, methodName, new Object[] {}, new Class[] {});
    }

    public boolean invokeBoolean(Object object, String methodName) {
        Boolean invoke = (Boolean) invoke(object, methodName, new Object[] {}, new Class[] {});
        return invoke.booleanValue();
    }

    public boolean invokeBoolean(Object object, String methodName, final Object[] parameters, Class<?>[] types) {
        Boolean invoke = (Boolean) invoke(object, methodName, parameters, types);
        return invoke.booleanValue();
    }

    public int invokeInteger(Object object, String methodName) {
        Integer invoke = (Integer) invoke(object, methodName, new Object[] {}, new Class[] {});
        return invoke.intValue();
    }

    public int invokeInteger(Object object, String methodName, Object[] objects, Class<?>[] classes) {
        Integer invoke = (Integer) invoke(object, methodName, objects, classes);
        return invoke.intValue();
    }
}
