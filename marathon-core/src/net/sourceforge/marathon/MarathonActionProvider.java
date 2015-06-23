package net.sourceforge.marathon;


import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import net.sourceforge.marathon.api.IScriptModelClientPart;
import net.sourceforge.marathon.display.DisplayWindow;
import net.sourceforge.marathon.display.IActionProvider;
import net.sourceforge.marathon.display.IMarathonAction;
import net.sourceforge.marathon.display.MarathonAction;
import net.sourceforge.marathon.editor.IEditorProvider;
import net.sourceforge.marathon.util.Blurb;
import net.sourceforge.marathon.util.UIUtils;

public class MarathonActionProvider implements IActionProvider {

    public static final Icon EMPTY_ICON = new ImageIcon(MarathonActionProvider.class.getResource("empty.gif"));
    public static final Icon OK_ICON = new ImageIcon(MarathonActionProvider.class.getResource("ok.gif"));
    public static final Icon ERROR_ICON = new ImageIcon(MarathonActionProvider.class.getResource("error.gif"));
    public static final Icon REFRESH_ICON = new ImageIcon(MarathonActionProvider.class.getResource("refresh.gif"));
    public static final Icon CLEAR_ICON = new ImageIcon(MarathonActionProvider.class.getResource("clear.gif"));
    private Preferences prefs = Preferences.userNodeForPackage(Constants.class);

    public static class SeparatorAction extends MarathonAction {
        public SeparatorAction(String menuName, boolean toolbar, boolean menu) {
            super(menuName, null, (char) 0, null, null, null, toolbar, menu);
        }

        public void actionPerformed(DisplayWindow parent, IScriptModelClientPart scriptModel, String script, int beginCaretPostion,
                int endCaretPosition, int startLine) throws Exception {
        }

        @Override public boolean isSeperator() {
            return true;
        }

    }

    private IMarathonAction[] actions;

    public MarathonActionProvider(IEditorProvider editorProvider) {
        boolean iteBlurbs = Boolean.parseBoolean(prefs.get(Constants.PREF_ITE_BLURBS, "false"));
        if(iteBlurbs) {
            MarathonAction[] mactions = new MarathonAction[1];
            mactions[0] = new MarathonAction("Welcome Message", "Show the welcome message", (char) 0, null, null, editorProvider,
                    false, true) {
                public void actionPerformed(DisplayWindow parent, IScriptModelClientPart scriptModel, String script,
                        int beginCaretPostion, int endCaretPosition, int startLine) throws Exception {
                    WelcomeMessage.showWelcomeMessage();
                }
            };
            mactions[0].setMenuName("File");
            actions = new IMarathonAction[mactions.length];
            for (int i = 0; i < mactions.length; i++) {
                actions[i] = mactions[i];
            }
            return;
        }
        MarathonAction[] mactions = new MarathonAction[9];
        mactions[0] = new MarathonAction("Extract Module", "Extract into a module method", (char) 0, UIUtils.ICON_MODULE, null,
                editorProvider, true, true) {
            public void actionPerformed(DisplayWindow parent, IScriptModelClientPart scriptModel, String script, int startOffset,
                    int endOffset, int startLine) throws Exception {
                new Blurb("about/extract-module", "Refactoring - Extracting a Module") {
                };
            }

        };
        mactions[0].setMenuName("Refactor");
        mactions[0].setMenuMnemonic('R');
        mactions[0].setAccelKey("^S+M");
        mactions[1] = new MarathonAction("Create DDT", "Convert to a data driven test", (char) 0, UIUtils.ICON_CONVERT, null,
                editorProvider, true, true) {
            public void actionPerformed(DisplayWindow parent, IScriptModelClientPart scriptModel, String script, int startOffset,
                    int endOffset, int startLine) throws Exception {
                new Blurb("about/create-ddt", "Refactoring - Create DDT") {
                };
            }
        };

        mactions[1].setMenuName("Refactor");
        mactions[1].setMenuMnemonic('R');
        mactions[1].setAccelKey("^S+D");
        mactions[2] = new MarathonAction("Create Data Loop", "Convert to a loop that uses data file", (char) 0, UIUtils.ICON_LOOP,
                null, editorProvider, true, true) {
            public void actionPerformed(DisplayWindow parent, IScriptModelClientPart scriptModel, String script, int startOffset,
                    int endOffset, int startLine) throws Exception {
                new Blurb("about/create-data-loop", "Refactoring - Create Data Loop") {
                };
            }
        };
        mactions[2].setMenuName("Refactor");
        mactions[2].setMenuMnemonic('R');
        mactions[2].setAccelKey("^S+L");
        mactions[3] = new SeparatorAction("Refactor", true, false);
        mactions[4] = new MarathonAction("Create object map...", "Create/modify the object map using the application", (char) 0,
                UIUtils.ICON_OMAP_CREATE, null, editorProvider, true, true) {

            public void actionPerformed(DisplayWindow parent, IScriptModelClientPart scriptModel, String script,
                    int beginCaretPostion, int endCaretPosition, int startLine) throws Exception {
                new Blurb("about/create-object-map", "Creating a Object Map") {
                };
            }
        };
        mactions[4].setMenuName("Object Map");
        mactions[4].setMenuMnemonic('O');
        mactions[5] = new MarathonAction("Edit Object Map...", "Modify the recognition properties for objects", (char) 0,
                UIUtils.ICON_OBJECTMAP, null, editorProvider, true, true) {

            public void actionPerformed(DisplayWindow parent, IScriptModelClientPart scriptModel, String script,
                    int beginCaretPostion, int endCaretPosition, int startLine) throws Exception {
                new Blurb("about/edit-object-map", "Edit Object Map Entries") {
                };
            }
        };
        mactions[5].setAccelKey("^S+O");
        mactions[5].setMenuName("Object Map");
        mactions[5].setMenuMnemonic('O');
        mactions[6] = new MarathonAction("Edit Object Map Configuration...", "Modify the object map configuration", (char) 0,
                UIUtils.ICON_CONF_EDIT, null, editorProvider, true, true) {

            public void actionPerformed(DisplayWindow parent, IScriptModelClientPart scriptModel, String script,
                    int beginCaretPostion, int endCaretPosition, int startLine) throws Exception {
                new Blurb("about/edit-object-map-configuration", "Edit Object Map Configuration") {
                };
            }
        };
        mactions[6].setMenuName("Object Map");
        mactions[6].setMenuMnemonic('O');
        mactions[7] = new MarathonAction("Clean Up", "Clean up the object map", (char) 0, null, null, editorProvider, false, true) {
            @Override public void actionPerformed(DisplayWindow parent, IScriptModelClientPart scriptModel, String script,
                    int beginCaretPostion, int endCaretPosition, int startLine) throws Exception {
            }

            @Override public boolean isPopupMenu() {
                return true;
            }

            @Override public JMenu getPopupMenu() {
                JMenu menu = new JMenu("Cleanup");
                menu.setIcon(REFRESH_ICON);
                JMenuItem markAll = new JMenuItem(new AbstractAction("Mark all components as unused", ERROR_ICON) {
                    private static final long serialVersionUID = 1L;

                    @Override public void actionPerformed(ActionEvent e) {
                        new Blurb("about/clean-object-map", "Clean Object Map") {
                        };
                    }
                });
                menu.add(markAll);
                System.setProperty(Constants.PLAY_MODE_MARK, "" + false);
                final JMenuItem startMarking = new JCheckBoxMenuItem();
                startMarking.setAction(new AbstractAction("Start marking used components", OK_ICON) {
                    private static final long serialVersionUID = 1L;

                    @Override public void actionPerformed(ActionEvent e) {
                        new Blurb("about/clean-object-map", "Clean Object Map") {
                        };
                    }
                });
                menu.add(startMarking);
                JMenuItem removeUnused = new JMenuItem(new AbstractAction("Remove all unused object map entries", CLEAR_ICON) {
                    private static final long serialVersionUID = 1L;

                    @Override public void actionPerformed(ActionEvent e) {
                        new Blurb("about/clean-object-map", "Clean Object Map") {
                        };
                    }
                });
                menu.add(removeUnused);
                menu.add(new JSeparator());
                JMenuItem cleanDir = new JMenuItem(new AbstractAction("Clean Object Map folder...", EMPTY_ICON) {
                    private static final long serialVersionUID = 1L;

                    @Override public void actionPerformed(ActionEvent e) {
                        new Blurb("about/clean-object-map", "Clean Object Map") {
                        };
                    }
                });
                menu.add(cleanDir);
                return menu;
            }
        };
        mactions[7].setMenuName("Object Map");
        mactions[8] = new MarathonAction("Welcome Message", "Show the welcome message", (char) 0, null, null, editorProvider,
                false, true) {
            public void actionPerformed(DisplayWindow parent, IScriptModelClientPart scriptModel, String script,
                    int beginCaretPostion, int endCaretPosition, int startLine) throws Exception {
                WelcomeMessage.showWelcomeMessage();
            }
        };
        mactions[8].setMenuName("File");
        actions = new IMarathonAction[mactions.length];
        for (int i = 0; i < mactions.length; i++) {
            actions[i] = mactions[i];
        }
    }

    public IMarathonAction[] getActions() {
        return actions;
    }
}
