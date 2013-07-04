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
package net.sourceforge.marathon.api;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JDialog;

import net.sourceforge.marathon.api.module.Function;

public interface IScriptModelClientPart extends ISubpanelProvider {

    public enum SCRIPT_FILE_TYPE {
        TEST, MODULE, FIXTURE, OTHER
    }

    public abstract String getDefaultTestHeader(String fixture);

    public abstract String getFixtureHeader(String fixture);

    public abstract String getModuleHeader(String moduleFunction, String description);

    public abstract String getScriptCodeForWindow(WindowId windowId);

    public abstract String getScriptCodeForWindowClose(WindowId windowId);

    public abstract String getFunctionCallForInsertDialog(Function f, String[] arguments);

    public abstract String[] parseMessage(String msg);

    public abstract String[] getFixtures();

    public abstract boolean isSourceFile(File f);

    public abstract boolean isTestFile(File f);

    public abstract String getSuffix();

    public abstract int getLinePositionForInsertion();

    public abstract String getScriptCodeForShowChecklist(String fileName);

    public abstract void createDefaultFixture(JDialog configurationUI, Properties props, File fixtureDir, List<String> keys);

    public abstract String getClasspath();

    public abstract String getScriptCodeForInsertChecklist(String fileName);

    public abstract String getScriptCodeForImportAction(String pkg, String function);

    public abstract String getFunctionFromInsertDialog(String function);

    public abstract String getPackageFromInsertDialog(String function);

    public abstract int getLinePositionForInsertionModule();

    public abstract String updateScriptWithImports(String text, HashSet<String> importStatements);

    public abstract String getDefaultFixtureHeader(Properties props, String launcher, List<String> keys);

    public abstract void fileUpdated(File file, SCRIPT_FILE_TYPE type);

    public abstract String getMarathonStartMarker();

    public abstract String getMarathonEndMarker();

    public abstract String getPlaybackImportStatement();

    public Map<String, Object> getFixtureProperties(String script);
    
    public Object eval(String script);

    public abstract String getAgentJar();
}