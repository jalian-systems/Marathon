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

import net.sourceforge.marathon.action.AbstractScriptElement;
import net.sourceforge.marathon.api.ScriptModelClientPart;
import net.sourceforge.marathon.api.WindowId;
import net.sourceforge.marathon.util.Indent;

public class InsertScriptElement extends AbstractScriptElement {
    private static final long serialVersionUID = 1L;
    private String function;
    private String pkg = null;

    public InsertScriptElement(WindowId windowId, String function) {
        super(null, windowId);
        this.function = ScriptModelClientPart.getModel().getFunctionFromInsertDialog(function);
        this.pkg = ScriptModelClientPart.getModel().getPackageFromInsertDialog(function);
    }

    public String toScriptCode() {
        StringBuffer sb = new StringBuffer();
        sb.append(Indent.getIndent()).append(function).append("\n");
        return sb.toString();
    }

    public String getImportStatement() {
        return ScriptModelClientPart.getModel().getScriptCodeForImportAction(pkg, function);
    }

}
