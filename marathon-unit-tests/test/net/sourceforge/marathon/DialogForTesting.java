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

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreeNode;

import net.sourceforge.marathon.api.ScriptModelServerPart;
import net.sourceforge.marathon.component.ComponentFinder;
import net.sourceforge.marathon.component.ComponentResolver;
import net.sourceforge.marathon.component.MButton;
import net.sourceforge.marathon.providers.ResolversProvider;
import net.sourceforge.marathon.recorder.WindowMonitor;

public class DialogForTesting extends JDialog {
    private static final long serialVersionUID = 1L;
    private ComponentFinder finder;
    private JButton button;
    private JButton messageBoxButton;
    private JTextField textField;
    private JComboBox comboBox;
    private JList list;
    private JMenu menu;
    private JMenuItem menuItem;
    private JTabbedPane tabbedPane;
    private JTabbedPane tabbedPaneWithIcons;
    private JCheckBox checkbox;
    private JTable table;
    private JTree tree;
    private JSpinner spinner;
    private boolean disposed = false;

    static {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    WindowMonitor.getInstance();
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public DialogForTesting(String title) {
        setTitle(title);
        setName(title);
        getContentPane().setLayout(new FlowLayout());
    }

    /**
     * Addes a single menu with a menu item
     */
    public void addMenu(String name, String text, String menuItemName, String menuItemText) {
        checkNull(menu);
        JMenuBar menuBar = new JMenuBar();
        menu = new JMenu(text);
        menu.setName(name);
        // add menu item
        checkNull(menuItem);
        menuItem = new JMenuItem(menuItemText);
        menuItem.setName(menuItemName);
        menu.add(menuItem);
        menuBar.add(menu);
        this.setJMenuBar(menuBar);
    }

    public void addButton(String name, String text) {
        checkNull(button);
        button = new JButton(text);
        addContent(name, button);
    }

    public void addMessageBoxButton(String name, String buttonTitle, final String messageTitle) {
        if (messageTitle.equals(getTitle())) {
            throw new RuntimeException("titles of windows must be different");
        }
        messageBoxButton = new JButton(buttonTitle);
        addContent(name, messageBoxButton);
        messageBoxButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(DialogForTesting.this, messageTitle, messageTitle, JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public void addTextField(String name, String text) {
        checkNull(textField);
        textField = new JTextField(text);
        addContent(name, textField);
    }

    public void addComboBox(String name, String[] items) {
        checkNull(comboBox);
        comboBox = new JComboBox(items);
        addContent(name, comboBox);
    }

    public void addList(String name, Object[] items) {
        checkNull(list);
        list = new JList(items);
        addContent(name, list);
    }

    public void addTabbedPane(String name, String tab1, String tab2) {
        checkNull(tabbedPane);
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(tab1, new JLabel(tab1 + " content That is very very long"));
        tabbedPane.addTab(tab2, new JLabel(tab2 + " content That is very very long"));
        addContent(name, tabbedPane);
    }

    public void addTabbedPane(String name, Icon tab1, Icon tab2) {
        checkNull(tabbedPaneWithIcons);
        tabbedPaneWithIcons = new JTabbedPane();
        tabbedPaneWithIcons.addTab(null, tab1, new JLabel(tab1 + " content That is very very long"));
        tabbedPaneWithIcons.addTab(null, tab2, new JLabel(tab2 + " content That is very very long"));
        addContent(name, tabbedPaneWithIcons);
    }

    public void addCheckBox(String name, String text) {
        checkNull(checkbox);
        checkbox = new JCheckBox(text);
        addContent(name, checkbox);
    }

    public void addSpinner() {
        checkNull(spinner);
        spinner = new JSpinner();
        addContent("spinner", spinner);
    }

    public void addTree(String name, TreeNode rootNode) {
        checkNull(tree);
        tree = new JTree(rootNode);
        tree.setName(name);
        addContent(null, new JScrollPane(tree));
    }

    public void addTable() {
        addTable("table.name", true, new Object[][] { { "a", "b" }, { "c", "d" } }, new String[] { "col1", "col2" });
    }

    public void addTable(String name, final boolean isEditable, Object[][] data, String[] columns) {
        checkNull(table);
        table = new JTable(new DefaultTableModel(data, columns) {
            private static final long serialVersionUID = 1L;

            public boolean isCellEditable(int row, int column) {
                return isEditable;
            }
        });
        table.getTableHeader().setName(name + ".header");
        table.setName("table.name");
        getContentPane().add(new JScrollPane(table));
    }

    public void addContent(String name, Component component) {
        component.setName(name);
        getContentPane().add(component);
    }

    public ComponentFinder getResolver() {
        if (finder == null) {
            WindowMonitor windowMonitor = WindowMonitor.getInstance();
            finder = new ComponentFinder(Boolean.FALSE, windowMonitor.getNamingStrategy(),
                    new ResolversProvider(), ScriptModelServerPart.getModelServerPart(), windowMonitor);
            finder.push(this);
        }
        return finder;
    }

    public ComponentFinder getResolver(final Class<? extends ComponentResolver> class1) {
        if (finder == null) {
            WindowMonitor windowMonitor = WindowMonitor.getInstance();
            finder = new ComponentFinder(Boolean.FALSE, windowMonitor.getNamingStrategy(),
                    new ResolversProvider() {
                        public List<ComponentResolver> get() {
                            ArrayList<ComponentResolver> l = new ArrayList<ComponentResolver>();
                            try {
                                Constructor<? extends ComponentResolver> cr = class1.getConstructor(new Class[] {
                                        ComponentFinder.class, boolean.class, WindowMonitor.class });
                                ComponentResolver res = cr.newInstance(new Object[] { finder, Boolean.valueOf(isRecording),
                                        windowMonitor });
                                l.add(res);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return l;
                        }
                    }, ScriptModelServerPart.getModelServerPart(), windowMonitor);
            finder.push(this);
        }
        return finder;
    }

    public JButton getButton() {
        return button;
    }

    public JButton getMessageBoxButton() {
        return messageBoxButton;
    }

    public JTextField getTextField() {
        return textField;
    }

    public JComboBox getComboBox() {
        return comboBox;
    }

    public JList getList() {
        return list;
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public JTabbedPane getTabbedPaneWithIcons() {
        return tabbedPaneWithIcons;
    }

    public JCheckBox getCheckBox() {
        return checkbox;
    }

    public JSpinner getSpinner() {
        return spinner;
    }

    public JTable getTable() {
        return table;
    }

    public JMenu getMenu() {
        return menu;
    }

    public JMenuItem getMenuItem() {
        return menuItem;
    }

    @SuppressWarnings("deprecation")
    public void show() {
        final Object waitLock = new Object();
        pack();
        disposed = false;
        addWindowListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                synchronized (waitLock) {
                    waitLock.notify();
                }
            }
        });
        synchronized (waitLock) {
            try {
                super.show();
                waitLock.wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        WindowMonitor.getInstance().waitForWindowToOpen(1000, this.getTitle(), ScriptModelServerPart.getModelServerPart());
    }

    public void dispose() {
        final Object waitLock = new Object();
        if (disposed) {
            // dispose() is a recursive call!
            return;
        }
        disposed = true;
        addWindowListener(new WindowAdapter() {
            public void windowDeactivated(WindowEvent e) {
                synchronized (waitLock) {
                    waitLock.notify();
                }
            }
        });
        synchronized (waitLock) {
            try {
                super.dispose();
                waitLock.wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String toString() {
        return "TestDialog(" + getTitle() + ")";
    }

    public static void main(String[] args) {
        DialogForTesting dialog = new DialogForTesting("Sample Dialog");
        dialog.addTable("table", true, new String[][] { { "a", "b", "c" }, { "d", "e", "f" } }, new String[] { "col1", "col2",
                "col3" });
        dialog.addTabbedPane("tabbedPane", "charles", "brilly");
        dialog.addButton(null, "Press Me");
        dialog.addTextField("textField", "some text");
        dialog.addComboBox("comboBox", new String[] { "foo", "bar", "baz" });
        dialog.addMessageBoxButton(null, "Show Message Dialog", "Message Dialog");
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        dialog.show();
    }

    private void checkNull(JComponent component) {
        if (component != null) {
            throw new RuntimeException("tried to add already existing component to TestDialog");
        }
    }

    public MButton getMButton() {
        return new MButton(button, button.getName(), null, WindowMonitor.getInstance());
    }

    public JTree getTree() {
        return tree;
    }

}
