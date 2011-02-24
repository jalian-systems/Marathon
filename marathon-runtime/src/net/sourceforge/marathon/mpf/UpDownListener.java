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
package net.sourceforge.marathon.mpf;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JList;

class UpDownListener implements ActionListener {
    private JList list;
    private boolean shouldMoveUp;

    public UpDownListener(JList list, boolean shouldMoveUp) {
        this.list = list;
        this.shouldMoveUp = shouldMoveUp;
    }

    public void actionPerformed(ActionEvent e) {
        int selectedIndex = list.getSelectedIndex();
        if (selectedIndex == -1)
            return;
        MovableItemListModel model = (MovableItemListModel) list.getModel();
        if (shouldMoveUp) {
            model.moveUp(selectedIndex);
            list.setSelectedIndex(selectedIndex - 1);
        } else {
            model.moveDown(selectedIndex);
            if (selectedIndex == model.getSize() - 1)
                list.setSelectedIndex(selectedIndex);
            else
                list.setSelectedIndex(selectedIndex + 1);
        }
    }
}
