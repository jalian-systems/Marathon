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
package net.sourceforge.marathon.examples;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class SampleAppTable extends JFrame {

    class SampleAppTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 1L;

        private List<SampleData> dataList;
        private String[] columnNames = { "Name", "Age", "Marital Status", "Gender" };

        public SampleAppTableModel() {
            dataList = new ArrayList<SampleData>();
        }

        public void removeRow(int selectedRow) {
            dataList.remove(selectedRow);
            fireTableDataChanged();
        }

        public int getColumnCount() {
            return 4;
        }

        public int getRowCount() {
            return dataList.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            SampleData data = (SampleData) dataList.get(rowIndex);

            switch (columnIndex) {
            case 0:
                return data.getName();
            case 1:
                return data.getAge();
            case 2:
                return data.isMarried();
            default:
                return data.getGender();
            }
        }

        public String getColumnName(int column) {
            return columnNames[column];
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        public Class<? extends Object> getColumnClass(int columnIndex) {
            switch (columnIndex) {
            case 0:
            default:
                return String.class;
            case 1:
                return Integer.class;
            case 2:
                return Boolean.class;
            case 3:
                return String.class;
            }
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            SampleData data = (SampleData) dataList.get(rowIndex);
            switch (columnIndex) {
            case 0:
                data.setName(aValue.toString());
                break;
            case 1:
                data.setAge((Integer) aValue);
                break;
            case 2:
                data.setMarried((Boolean) aValue);
                break;
            case 3:
                data.setGender((String) aValue);
            }
        }

        public void addRow() {
            dataList.add(new SampleData());
            fireTableDataChanged();
        }

        public void addRow(String name, int age, boolean married) {
            dataList.add(new SampleData(name, age, married));
            fireTableDataChanged();
        }

    }

    class SampleData {
        private String name;
        private Integer age;
        private Boolean married;
        private String gender;

        public SampleData() {
            name = "";
            age = new Integer(0);
            married = Boolean.FALSE;
        }

        public Object getGender() {
            return gender;
        }

        public void setGender(String aValue) {
            this.gender = aValue;
        }

        public SampleData(String name, int age, boolean married) {
            this.name = name;
            this.age = new Integer(age);
            this.married = Boolean.valueOf(married);
        }

        public Integer getAge() {
            return age;
        }

        public Boolean isMarried() {
            return married;
        }

        public String getName() {
            return name;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public void setMarried(Boolean married) {
            this.married = married;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private static final long serialVersionUID = 1L;

    public SampleAppTable() {
        setTitle("Marathon Demo");
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        URL url = getClass().getClassLoader().getResource("images/save.gif");
        Icon icon = new ImageIcon(url);

        JButton saveButton = new JButton(icon);
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JOptionPane.showMessageDialog(SampleAppTable.this, "To save or not to save is the question ?", "Simple Widgets",
                        JOptionPane.QUESTION_MESSAGE);
            }
        });
        saveButton.setName("Save");
        url = getClass().getClassLoader().getResource("images/load.gif");
        icon = new ImageIcon(url);
        JButton loadButton = new JButton(icon);
        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JOptionPane.showMessageDialog(SampleAppTable.this, "Open the Pandora's Box", "Load",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        loadButton.setName("Load");
        toolbar.add(saveButton);
        toolbar.add(loadButton);
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(createTablePanel());
        setJMenuBar(createMenuBar());
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });
        pack();
        setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('f');

        JMenuItem item = new JMenuItem("Exit");
        item.setMnemonic('e');
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }

        });
        fileMenu.add(item);
        bar.add(fileMenu);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('h');
        item = new JMenuItem("About");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(SampleAppTable.this, "Marathon Test Demo version 1.0", "About Marathon Demo",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        helpMenu.add(item);

        bar.add(helpMenu);

        return bar;

    }

    public class ComboBoxRenderer extends JComboBox implements TableCellRenderer {
        private static final long serialVersionUID = 1L;

        public ComboBoxRenderer(String[] items) {
            super(items);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }

            // Select the current value
            setSelectedItem(value);
            return this;
        }
    }

    public class ComboBoxEditor extends DefaultCellEditor {
        private static final long serialVersionUID = 1L;

        public ComboBoxEditor(String[] items) {
            super(new JComboBox(items));
        }
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        final JTable table = new JTable();
        table.setRowHeight(20);
        table.setName("Table");
        final SampleAppTableModel model = new SampleAppTableModel();
        model.addRow();
        table.setModel(model);
        table.setColumnSelectionAllowed(true);
        table.setRowSelectionAllowed(true);
        TableColumn column = table.getColumnModel().getColumn(3);
        String[] genders = new String[] { "male", "female" };
        column.setCellEditor(new ComboBoxEditor(genders));
        column.setCellRenderer(new ComboBoxRenderer(genders));
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JPanel optionsPanel = new JPanel(new BorderLayout());
        ButtonGroup selectionModeGroup = new ButtonGroup();

        JPanel smPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JRadioButton smSingle = new JRadioButton("Single", false);
        final JRadioButton smSingleInterval = new JRadioButton("Single Interval", false);
        final JRadioButton smMultipleInterval = new JRadioButton("Multiple Interval", true);
        smPanel.add(smSingle);
        smPanel.add(smSingleInterval);
        smPanel.add(smMultipleInterval);
        optionsPanel.add(smPanel, BorderLayout.NORTH);

        selectionModeGroup.add(smSingle);
        selectionModeGroup.add(smSingleInterval);
        selectionModeGroup.add(smMultipleInterval);
        ActionListener smAction = new ActionListener() {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == smSingle)
                    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                else if (e.getSource() == smSingleInterval)
                    table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
                else
                    table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            }
        };
        smSingle.addActionListener(smAction);
        smSingleInterval.addActionListener(smAction);
        smMultipleInterval.addActionListener(smAction);

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JCheckBox selColumnSelection = new JCheckBox("Column Selection", true);
        final JCheckBox selRowSelection = new JCheckBox("Row Selection", true);
        selectionPanel.add(selRowSelection);
        selectionPanel.add(selColumnSelection);
        optionsPanel.add(selectionPanel, BorderLayout.SOUTH);
        ActionListener selectionAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == selColumnSelection)
                    table.setColumnSelectionAllowed(selColumnSelection.isSelected());
                else if (e.getSource() == selRowSelection)
                    table.setRowSelectionAllowed(selRowSelection.isSelected());
            }
        };
        selColumnSelection.addActionListener(selectionAction);
        selRowSelection.addActionListener(selectionAction);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final JButton populateButton = new JButton("Populate");
        populateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                populateButton.setEnabled(false);
                new Thread() {
                    public void run() {
                        updateTableWithNotReallyRandom(model);
                    }
                }.start();
            }

            private void updateTableWithNotReallyRandom(final SampleAppTableModel model) {
                for (int i = 0; i < 5; i++) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e1) {
                    }
                    model.addRow(getRandomName(i), (int) (Math.random() * 100), getRandomBoolean());
                }
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            populateButton.setEnabled(true);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            private String getRandomName(int i2) {
                return "RandomNameDoesNotHelp" + i2;
            }

            private boolean getRandomBoolean() {
                return Math.random() < 0.5;
            }
        });
        populateButton.setMnemonic(KeyEvent.VK_N);
        populateButton.setName("noActionButton");
        JButton addButton = new JButton("Add");
        addButton.setName("addButton");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.addRow();
            }
        });

        final JButton removeButton = new JButton("Remove");
        removeButton.setEnabled(false);
        removeButton.setName("removeButton");
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1)
                    model.removeRow(selectedRow);
            }
        });

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;
                int row = table.getSelectedRow();
                removeButton.setEnabled(row != -1);
            }
        });
        buttonPanel.add(populateButton);
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        panel.add(new JScrollPane(table));
        panel.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(optionsPanel, BorderLayout.NORTH);
        return panel;
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                System.out.println("Message from SampleApp using SwingUtilities.invokeAndWait");
                System.out.println("The property value of test.sampleapp = " + System.getProperty("test.sampleapp"));
                System.out.println("The property value of 'test sampleapp' = '" + System.getProperty("test sampleapp") + "'");
                new SampleAppTable();
            }
        });
    }
}
