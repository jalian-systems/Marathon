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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import net.sourceforge.marathon.util.PropertyList.Property;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class PropertyEditor extends EscapeDialog {

    private static final class PropertyTable extends JTable {
        private static final long serialVersionUID = 1L;

        private PropertyTable(TableModel dm) {
            super(dm);
            setRowHeight(20);
        }

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            if (column == 0)
                return super.getCellRenderer(row, column);
            PropertyTableModel model = (PropertyTableModel) getModel();
            Property property = model.getProperty(row);
            if (property.getKlass().equals(Color.class))
                return new ColorRenderer(false);
            if (property.getKlass().equals(Font.class))
                return new FontRenderer();
            return super.getCellRenderer(row, column);
        }

        @Override
        public TableCellEditor getCellEditor(int row, int column) {
            if (column == 0)
                return super.getCellEditor(row, column);
            PropertyTableModel model = (PropertyTableModel) getModel();
            Property property = model.getProperty(row);
            if (property.getKlass().equals(Color.class))
                return new ColorEditor();
            if (property.getKlass().equals(Font.class)) {
                JComboBox box = new JComboBox(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
                return new DefaultCellEditor(box);
            }
            if (property.getKlass().equals(Boolean.class)) {
                JComboBox box = new JComboBox(new String[] { "true", "false" });
                return new DefaultCellEditor(box);
            }
            if (property.getKlass().equals(String[].class)) {
                JComboBox box = new JComboBox(property.getItems());
                return new DefaultCellEditor(box);
            }
            if (property.getKlass().equals(Integer.class))
                return new IntegerEditor(0, Integer.MAX_VALUE);
            return super.getCellEditor(row, column);
        }
    }

    public static class IntegerEditor extends DefaultCellEditor {
        private static final long serialVersionUID = 1L;

        JFormattedTextField ftf;
        NumberFormat integerFormat;
        private Integer minimum, maximum;

        public IntegerEditor(int min, int max) {
            super(new JFormattedTextField());
            ftf = (JFormattedTextField) getComponent();
            minimum = Integer.valueOf(min);
            maximum = Integer.valueOf(max);

            // Set up the editor for the integer cells.
            integerFormat = NumberFormat.getIntegerInstance();
            NumberFormatter intFormatter = new NumberFormatter(integerFormat);
            intFormatter.setFormat(integerFormat);
            intFormatter.setMinimum(minimum);
            intFormatter.setMaximum(maximum);

            ftf.setFormatterFactory(new DefaultFormatterFactory(intFormatter));
            ftf.setValue(minimum);
            ftf.setHorizontalAlignment(JTextField.TRAILING);
            ftf.setFocusLostBehavior(JFormattedTextField.PERSIST);

            // React when the user presses Enter while the editor is
            // active. (Tab is handled as specified by
            // JFormattedTextField's focusLostBehavior property.)
            ftf.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "check");
            ftf.getActionMap().put("check", new AbstractAction() {
                private static final long serialVersionUID = 1L;

                public void actionPerformed(ActionEvent e) {
                    if (!ftf.isEditValid()) { // The text is invalid.
                        if (userSaysRevert()) { // reverted
                            ftf.postActionEvent(); // inform the editor
                        }
                    } else
                        try { // The text is valid,
                            ftf.commitEdit(); // so use it.
                            ftf.postActionEvent(); // stop editing
                        } catch (java.text.ParseException exc) {
                        }
                }
            });
        }

        // Override to invoke setValue on the formatted text field.
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            JFormattedTextField ftf = (JFormattedTextField) super
                    .getTableCellEditorComponent(table, value, isSelected, row, column);
            ftf.setValue(Integer.parseInt(value.toString()));
            return ftf;
        }

        // Override to ensure that the value remains an Integer.
        public Object getCellEditorValue() {
            JFormattedTextField ftf = (JFormattedTextField) getComponent();
            Object o = ftf.getValue();
            if (o instanceof Integer) {
                return o;
            } else if (o instanceof Number) {
                return Integer.valueOf(((Number) o).intValue());
            } else {
                try {
                    return integerFormat.parseObject(o.toString());
                } catch (ParseException exc) {
                    return null;
                }
            }
        }

        // Override to check whether the edit is valid,
        // setting the value if it is and complaining if
        // it isn't. If it's OK for the editor to go
        // away, we need to invoke the superclass's version
        // of this method so that everything gets cleaned up.
        public boolean stopCellEditing() {
            JFormattedTextField ftf = (JFormattedTextField) getComponent();
            if (ftf.isEditValid()) {
                try {
                    ftf.commitEdit();
                } catch (java.text.ParseException exc) {
                }

            } else { // text is invalid
                if (!userSaysRevert()) { // user wants to edit
                    return false; // don't let the editor go away
                }
            }
            return super.stopCellEditing();
        }

        /**
         * Lets the user know that the text they entered is bad. Returns true if
         * the user elects to revert to the last good value. Otherwise, returns
         * false, indicating that the user wants to continue editing.
         */
        protected boolean userSaysRevert() {
            Toolkit.getDefaultToolkit().beep();
            ftf.selectAll();
            Object[] options = { "Edit", "Revert" };
            int answer = JOptionPane.showOptionDialog(SwingUtilities.getWindowAncestor(ftf),
                    "The value must be an integer between " + minimum + " and " + maximum + ".\n"
                            + "You can either continue editing " + "or revert to the last valid value.", "Invalid Text Entered",
                    JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[1]);

            if (answer == 1) { // Revert!
                ftf.setValue(ftf.getValue());
                return true;
            }
            return false;
        }
    }

    public static class FontRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        private String fontName;

        @Override
        protected void setValue(Object value) {
            fontName = (String) value;
            super.setValue(value);
        }

        @Override
        public Font getFont() {
            return Font.decode(fontName);
        }
    }

    public static class ColorEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private static final long serialVersionUID = 1L;

        Color currentColor;
        JButton button;
        JColorChooser colorChooser;
        JDialog dialog;
        protected static final String EDIT = "edit";

        public ColorEditor() {
            // Set up the editor (from the table's point of view),
            // which is a button.
            // This button brings up the color chooser dialog,
            // which is the editor from the user's point of view.
            button = new JButton();
            button.setActionCommand(EDIT);
            button.addActionListener(this);
            button.setBorderPainted(false);

            // Set up the dialog that the button brings up.
            colorChooser = new JColorChooser();
            dialog = JColorChooser.createDialog(button, "Pick a Color", true, // modal
                    colorChooser, this, // OK button handler
                    null); // no CANCEL button handler
        }

        /**
         * Handles events from the editor button and from the dialog's OK
         * button.
         */
        public void actionPerformed(ActionEvent e) {
            if (EDIT.equals(e.getActionCommand())) {
                // The user has clicked the cell, so
                // bring up the dialog.
                button.setBackground(currentColor);
                colorChooser.setColor(currentColor);
                dialog.setVisible(true);

                // Make the renderer reappear.
                fireEditingStopped();

            } else { // User pressed dialog's "OK" button.
                currentColor = colorChooser.getColor();
            }
        }

        // Implement the one CellEditor method that AbstractCellEditor doesn't.
        public Object getCellEditorValue() {
            return "#" + Integer.toHexString((currentColor.getRGB() & 0x00FFFFFF) | 0x1000000).substring(1);
        }

        // Implement the one method defined by TableCellEditor.
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentColor = Color.decode(value.toString());
            return button;
        }
    }

    public static class ColorRenderer extends JLabel implements TableCellRenderer {
        private static final long serialVersionUID = 1L;

        Border unselectedBorder = null;
        Border selectedBorder = null;
        boolean isBordered = true;

        public ColorRenderer(boolean isBordered) {
            this.isBordered = isBordered;
            setOpaque(true); // MUST do this for background to show up.
        }

        public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row,
                int column) {
            Color newColor = Color.decode(color.toString());
            setBackground(newColor);
            if (isBordered) {
                if (isSelected) {
                    if (selectedBorder == null) {
                        selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
                    }
                    setBorder(selectedBorder);
                } else {
                    if (unselectedBorder == null) {
                        unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
                    }
                    setBorder(unselectedBorder);
                }
            }

            setToolTipText("RGB value: " + newColor.getRed() + ", " + newColor.getGreen() + ", " + newColor.getBlue());
            return this;
        }
    }

    class PropertyTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;

        public int getColumnCount() {
            return 2;
        }

        public int getRowCount() {
            return propList.getSize();
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0)
                return "Property";
            else
                return "Value";
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Property property = propList.getProperty(rowIndex);
            if (columnIndex == 0)
                return property.getDescription();
            else
                return property.getValue();
        }

        public Property getProperty(int row) {
            return propList.getProperty(row);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex != 0;
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            Property property = propList.getProperty(rowIndex);
            if (columnIndex == 0)
                return;
            else
                property.setValue(value.toString());
        }
    }

    private static final long serialVersionUID = 1L;
    private PropertyList propList;
    protected Class<?> preferencesPackage;
    private URL defaultProperties;

    public PropertyEditor(JFrame parent, Class<?> preferencesPackage, URL propertiesURL, String title) {
        super(parent, title, true);
        this.preferencesPackage = preferencesPackage;
        this.defaultProperties = propertiesURL;
        setResizable(false);
        loadDefaultProperties();
        updateWithPrefs();
        final JTable table = createTable();
        FormLayout layout = new FormLayout("3dlu, pref, 3dlu", "3dlu, fill:pref, 3dlu, pref, 3dlu");
        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints constraints = new CellConstraints();
        builder.add(new JScrollPane(table), constraints.xy(2, 2));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        JButton resetButton = new JButton("Defaults");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                savePreferences();
                dispose();
            }

            private void savePreferences() {
                Preferences prefs = Preferences.userNodeForPackage(PropertyEditor.this.preferencesPackage);
                List<Property> list = propList.getProperties();
                for (Property property : list) {
                    prefs.put(property.getKey(), property.getValue());
                }
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadDefaultProperties();
                ((PropertyTableModel) table.getModel()).fireTableDataChanged();
            }
        });
        JPanel bbuilder = ButtonBarFactory.buildRightAlignedBar(okButton, cancelButton, resetButton);
        bbuilder.setBackground(new Color(255, 255, 255));
        builder.getPanel().setBackground(new Color(255, 255, 255));
        builder.add(bbuilder, constraints.xy(2, 4));
        getContentPane().add(builder.getPanel());
        setCloseButton(cancelButton);
        getRootPane().setDefaultButton(okButton);
        pack();
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((size.width - getSize().width) / 2, (size.height - getSize().height) / 2);
    }

    private void updateWithPrefs() {
        Preferences prefs = Preferences.userNodeForPackage(preferencesPackage);
        List<Property> list = propList.getProperties();
        for (Property property : list) {
            String val = prefs.get(property.getKey(), null);
            if (val != null)
                property.setValue(val);
        }
    }

    private JTable createTable() {
        JTable table = new PropertyTable(new PropertyTableModel());
        return table;
    }

    private void loadDefaultProperties() {
        propList = new PropertyList();
        Properties props = new Properties();
        try {
            props.load(defaultProperties.openStream());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        SortedSet<Entry<Object, Object>> sortedSet = new TreeSet<Entry<Object, Object>>(new Comparator<Entry<Object, Object>>() {
            public int compare(Entry<Object, Object> o1, Entry<Object, Object> o2) {
                String key1 = (String) o1.getKey();
                String key2 = (String) o2.getKey();
                return key1.compareTo(key2);
            }
        });
        sortedSet.addAll(props.entrySet());
        for (Entry<Object, Object> prop : sortedSet) {
            String key = (String) prop.getKey();
            String value = (String) prop.getValue();
            if (key.startsWith("view.style"))
                continue;
            if (value.equals("true") || value.equals("false")) {
                propList.addBooleanProperty(key, key, Boolean.parseBoolean(value));
                continue;
            } else if (value.startsWith("#")) {
                try {
                    Color color = Color.decode(value);
                    propList.addColorProperty(key, key, color);
                    continue;
                } catch (NumberFormatException e) {
                }
            }
            try {
                int i = Integer.parseInt(value);
                propList.addIntegerProperty(key, key, i);
                continue;
            } catch (NumberFormatException e) {
            }
            if (key.endsWith(".font")) {
                Font font2 = Font.decode(value);
                if (font2 == null)
                    font2 = Font.decode(null);
                propList.addFontProperty(key, key, font2);
            } else if (key.equals("buffer.wrap")) {
                propList.addSelectionProperty(key, key, value, new String[] { "none", "soft", "hard" });
            } else if (key.equals("view.gutter.numberAlignment")) {
                propList.addSelectionProperty(key, key, value, new String[] { "left", "center", "right" });
            } else if (key.equals("buffer.folding")) {
                propList.addSelectionProperty(key, key, value, new String[] { "explicit", "indent", "none" });
            } else if (key.equals("view.antiAlias")) {
                propList.addSelectionProperty(key, key, value, new String[] { "none", "standard", "subpixel" });
            } else if (key.endsWith(".fontstyle")) {
                propList.addSelectionProperty(key, key, value, new String[] { "normal", "bold", "italic", "bold-italic" });
            } else {
                propList.addStringProperty(key, key, value);
            }
        }
    }
}
