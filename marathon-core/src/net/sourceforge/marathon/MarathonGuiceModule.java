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
package net.sourceforge.marathon;

import java.util.Properties;

import net.sourceforge.marathon.api.IRuntimeFactory;
import net.sourceforge.marathon.api.IScriptModelClientPart;
import net.sourceforge.marathon.api.ScriptModelClientPart;
import net.sourceforge.marathon.display.Display.IDisplayProperties;
import net.sourceforge.marathon.display.FixtureSelector;
import net.sourceforge.marathon.display.IActionProvider;
import net.sourceforge.marathon.editor.IEditorProvider;
import net.sourceforge.marathon.editor.MultiEditorProvider;
import net.sourceforge.marathon.editor.rsta.RSTAEditorProvider;
import net.sourceforge.marathon.providers.PlaybackResultProvider;
import net.sourceforge.marathon.providers.RecorderProvider;
import net.sourceforge.marathon.runtime.JavaRuntimeFactory;
import net.sourceforge.marathon.suite.editor.SuiteEditorProvider;

import com.google.inject.AbstractModule;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

public class MarathonGuiceModule extends AbstractModule {

    protected MultiEditorProvider editorProvider;

    public MarathonGuiceModule() {
        editorProvider = new MultiEditorProvider();
        IEditorProvider rstaEditorProvider = new RSTAEditorProvider();
        editorProvider.add(rstaEditorProvider, true);

        SuiteEditorProvider suiteEditorProvider = new SuiteEditorProvider();
        editorProvider.add(suiteEditorProvider, false);
    }

    @Override
    protected void configure() {
        bind(Properties.class).annotatedWith(IDisplayProperties.class).toInstance(System.getProperties());
        bindRuntime();
        bind(RecorderProvider.class).toInstance(new RecorderProvider());
        bind(PlaybackResultProvider.class).toInstance(new PlaybackResultProvider());
        bind(IScriptModelClientPart.class).toInstance(ScriptModelClientPart.getModel());
        bind(IEditorProvider.class).toInstance(editorProvider);
        try {
            bind(BrowserLauncher.class).toInstance(new BrowserLauncher());
        } catch (BrowserLaunchingInitializingException e) {
            e.printStackTrace();
        } catch (UnsupportedOperatingSystemException e) {
            e.printStackTrace();
        }
        bind(FixtureSelector.class).toInstance(new FixtureSelector());
        bindActionProvider();
    }

    protected void bindActionProvider() {
        bind(IActionProvider.class).toInstance(new MarathonActionProvider(editorProvider));
    }

    protected void bindRuntime() {
        bind(IRuntimeFactory.class).toInstance(new JavaRuntimeFactory());
    }
}
