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

import java.util.Stack;

import net.sourceforge.marathon.api.SourceLine;

public class CallStack {
    private static class StackMethod {
        private String methodName;

        public StackMethod(String methodName) {
            this.methodName = methodName;
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
            return result;
        }


        @Override public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            StackMethod other = (StackMethod) obj;
            if (methodName == null) {
                if (other.methodName != null)
                    return false;
            } else if (!methodName.equals(other.methodName))
                return false;
            return true;
        }


        @Override
        public String toString() {
            return methodName;
        }
    }

    private Stack<CallStack.StackMethod> stack = new Stack<CallStack.StackMethod>();
    private int stackDepth = 0;

    public void update(int type, SourceLine line) {
        CallStack.StackMethod elem = new StackMethod(line.functionName);
        if (type == Display.METHOD_RETURNED) {
            if (!stack.contains(elem)) {
                return;
            }
            stackDepth--;
        } else if (type == Display.METHOD_CALLED) {
            stack.push(elem);
            stackDepth++;
        }
    }

    public void clear() {
        stack.clear();
        stackDepth = 0;
    }

    public int getStackDepth() {
        return stackDepth;
    }

}