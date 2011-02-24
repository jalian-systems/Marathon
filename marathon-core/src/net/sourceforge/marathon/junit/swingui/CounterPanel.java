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
package net.sourceforge.marathon.junit.swingui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * A panel with test run counters
 */
public class CounterPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private JTextField numberOfErrors;
    private JTextField numberOfFailures;
    private JTextField numberOfRuns;
    private Icon failureIcon = Icons.FAILURE;
    private Icon errorIcon = Icons.ERROR;
    private int total;

    public CounterPanel() {
        super(new GridBagLayout());
        numberOfErrors = createOutputField(5);
        numberOfFailures = createOutputField(5);
        numberOfRuns = createOutputField(9);
        addToGrid(new JLabel("Runs:", SwingConstants.CENTER), 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0));
        addToGrid(numberOfRuns, 1, 0, 1, 1, 0.33, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 8,
                0, 0));
        addToGrid(new JLabel("Errors:", errorIcon, SwingConstants.LEFT), 2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(0, 8, 0, 0));
        addToGrid(numberOfErrors, 3, 0, 1, 1, 0.33, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 8,
                0, 0));
        addToGrid(new JLabel("Failures:", failureIcon, SwingConstants.LEFT), 4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(0, 8, 0, 0));
        addToGrid(numberOfFailures, 5, 0, 1, 1, 0.33, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,
                8, 0, 0));
    }

    private JTextField createOutputField(int width) {
        JTextField field = new JTextField("0", width);
        field.setMinimumSize(field.getPreferredSize());
        field.setMaximumSize(field.getPreferredSize());
        field.setHorizontalAlignment(SwingConstants.LEFT);
        field.setEditable(false);
        field.setBorder(BorderFactory.createEmptyBorder());
        return field;
    }

    public void addToGrid(Component comp, int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty,
            int anchor, int fill, Insets insets) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.gridwidth = gridwidth;
        constraints.gridheight = gridheight;
        constraints.weightx = weightx;
        constraints.weighty = weighty;
        constraints.anchor = anchor;
        constraints.fill = fill;
        constraints.insets = insets;
        add(comp, constraints);
    }

    public void reset() {
        setLabelValue(numberOfErrors, 0);
        setLabelValue(numberOfFailures, 0);
        setLabelValue(numberOfRuns, 0);
        total = 0;
    }

    public void setTotal(int value) {
        total = value;
    }

    public void setRunValue(int value) {
        numberOfRuns.setText(Integer.toString(value) + "/" + total);
    }

    public void setErrorValue(int value) {
        setLabelValue(numberOfErrors, value);
    }

    public void setFailureValue(int value) {
        setLabelValue(numberOfFailures, value);
    }

    private void setLabelValue(JTextField label, int value) {
        label.setText(Integer.toString(value));
    }
}
