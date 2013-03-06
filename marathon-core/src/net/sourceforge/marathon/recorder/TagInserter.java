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

import net.sourceforge.marathon.api.IScriptElement;

public class TagInserter {
    private CompositeScriptElement rootTag = new TestRootElement(null);
    private CompositeScriptElement currentContainer = null;

    public CompositeScriptElement getRootTag() {
        return rootTag;
    }

    public void add(CompositeScriptElement container, IScriptElement recordable) {
        if (undo(recordable))
            return;
        addTagContainer(currentContainer, container);
        if (recordable.toScriptCode() != null)
            currentContainer.add(recordable);
    }

    public void add(IScriptElement recordable) {
        if (undo(recordable))
            return;
        if (currentContainer == null)
            rootTag.add(recordable);
        else
            currentContainer.add(recordable);
    }

    private boolean undo(IScriptElement recordable) {
        if (recordable.isUndo()) {
            IScriptElement element = last();
            if (element != null && element.equals(recordable.getUndoElement())) {
                currentContainer.getChildren().removeLast();
            }
            return true;
        }
        return false;
    }

    private void addTagContainer(CompositeScriptElement oldContainer, CompositeScriptElement newContainer) {
        if (oldContainer == null) {
            if (!newContainer.equals(rootTag.getChildren().last())) {
                rootTag.add(newContainer);
                currentContainer = newContainer;
            } else {
                currentContainer = (WindowElement) rootTag.getChildren().last();
            }
        } else if (oldContainer.owns(newContainer)) {
            if (!newContainer.equals(oldContainer.getChildren().last())) {
                oldContainer.add(newContainer);
                currentContainer = newContainer;
            } else {
                currentContainer = (WindowElement) oldContainer.getChildren().last();
            }
        } else {
            addTagContainer(getParent(oldContainer), newContainer);
        }
    }

    private CompositeScriptElement getParent(CompositeScriptElement container) {
        CompositeScriptElement parent = (CompositeScriptElement) rootTag.getChildren().last();
        if (parent.equals(container))
            return null;
        while (true) {
            WindowElement child = (WindowElement) parent.getChildren().last();
            if (child.equals(container))
                return parent;
            parent = child;
        }
    }

    private IScriptElement last() {
        if (currentContainer == null)
            return null;
        return currentContainer.getChildren().last();
    }
}
