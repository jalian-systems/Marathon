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
package net.sourceforge.marathon.display;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import net.sourceforge.marathon.api.ILogger;
import net.sourceforge.marathon.api.LogRecord;
import net.sourceforge.marathon.editor.IEditorProvider;
import net.sourceforge.marathon.util.UIUtils;

import com.google.inject.Inject;
import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.toolbars.ToolBarConstraints;
import com.vlsolutions.swing.toolbars.ToolBarContainer;
import com.vlsolutions.swing.toolbars.ToolBarPanel;
import com.vlsolutions.swing.toolbars.VLToolBar;

public class LogView implements Dockable {
    private static final Icon ICON_LOGVIEW = new ImageIcon(
            TextAreaOutput.class.getResource("/net/sourceforge/marathon/display/icons/enabled/warn.gif"));
    private static final Icon ICON_MSG = null;
    private static final Icon ICON_INFO = new ImageIcon(
            TextAreaOutput.class.getResource("/net/sourceforge/marathon/display/icons/enabled/info.gif"));
    static final Icon ICON_ERROR = new ImageIcon(
            TextAreaOutput.class.getResource("/net/sourceforge/marathon/display/icons/enabled/error.gif"));
    private static final Icon ICON_WARNING = new ImageIcon(
            TextAreaOutput.class.getResource("/net/sourceforge/marathon/display/icons/enabled/warn.gif"));

    private static final DockKey DOCK_KEY = new DockKey("Log", "Record & Playback Log", "Log", ICON_LOGVIEW);

    private DateFormat dateTimeInstance = DateFormat.getDateTimeInstance();

    private static int MAX_RECORDS = 1000;
    private static int WATERMARK = 100;

    private class LogRecordList {
        private final class ArrayListExtension extends ArrayList<LogRecord> {
            private static final long serialVersionUID = 1L;

            @Override public void removeRange(int fromIndex, int toIndex) {
                super.removeRange(fromIndex, toIndex);
            }
        }

        private ArrayListExtension logRecordList = new ArrayListExtension();

        public void add(LogRecord log) {
            synchronized (logRecordList) {
                if (logRecordList.size() >= MAX_RECORDS + WATERMARK)
                    logRecordList.removeRange(0, WATERMARK);
                logRecordList.add(log);
            }
        }

        public void clear() {
            synchronized (logRecordList) {
                logRecordList.clear();
            }
        }

        public int count() {
            synchronized (logRecordList) {
                int count = 0;
                for (LogRecord logRecord : logRecordList) {
                    if (logRecord.getType() >= logger.getLogLevel())
                        count++;
                }
                return count;
            }
        }

        public LogRecord getRecordAt(int rowIndex) {
            synchronized (logRecordList) {
                int count = -1;
                for (LogRecord logRecord : logRecordList) {
                    if (logRecord.getType() >= logger.getLogLevel())
                        count++;
                    if (count == rowIndex)
                        return logRecord;
                }
                return null;
            }
        }
    }

    LogRecordList logList = new LogRecordList();

    private LogTableModel logTableModel;
    private JTable logTable;
    public LogRecord selectedLog;

    private ToolBarContainer panel;
    private JToggleButton infoButton;
    private JToggleButton warnButton;
    private JToggleButton errorButton;
    private JButton showMessage;
    private JScrollPane tableScrollPane;
    private IErrorListener errorListener;

    public LogView() {
        initUI();
    }

    private void initUI() {
        logTableModel = new LogTableModel();
        logTable = new JTable(logTableModel);
        logTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        logTable.getColumnModel().getColumn(0).setMaxWidth(18);
        logTable.getColumnModel().getColumn(0).setMinWidth(18);
        logTable.getColumnModel().getColumn(0).setPreferredWidth(18);
        int width = new JTextField(dateTimeInstance.format(new Date())).getPreferredSize().width;
        width += 4;
        logTable.getColumnModel().getColumn(3).setMaxWidth(width);
        logTable.getColumnModel().getColumn(3).setMinWidth(width);
        logTable.getColumnModel().getColumn(3).setPreferredWidth(width);
        logTable.getColumnModel().getColumn(2).setMaxWidth(width);
        logTable.getColumnModel().getColumn(2).setMinWidth(width);
        logTable.getColumnModel().getColumn(2).setPreferredWidth(width);

        logTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableCellRenderer renderer = logTable.getTableHeader().getDefaultRenderer();
        if (renderer instanceof DefaultTableCellRenderer) {
            ((DefaultTableCellRenderer) renderer).setHorizontalAlignment(SwingConstants.LEFT);
        }
        logTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;
                int selectedRow = logTable.getSelectedRow();
                if (selectedRow >= 0 && logList != null && selectedRow < logList.count()) {
                    selectedLog = logList.getRecordAt(selectedRow);
                    showMessage.setEnabled(selectedLog.getDescription() != null);
                } else {
                    selectedLog = null;
                    if (showMessage != null)
                        showMessage.setEnabled(false);
                }
            }
        });
        logTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int clickCount = e.getClickCount();
                if (clickCount > 1) {
                    if (selectedLog != null)
                        showMessage(selectedLog);
                }
            }
        });
        logTable.getSelectionModel().setSelectionInterval(0, 0);
        JPanel panel1 = new JPanel(new BorderLayout());
        tableScrollPane = new JScrollPane(logTable);
        panel1.add(tableScrollPane, BorderLayout.CENTER);
        panel = ToolBarContainer.createDefaultContainer(true, false, false, false, FlowLayout.TRAILING);
        panel.add(panel1);
        ToolBarPanel barPanel = panel.getToolBarPanelAt(BorderLayout.NORTH);
        VLToolBar option = new VLToolBar();
        infoButton = UIUtils.createInfoButton();
        infoButton.setToolTipText("Show all messages");
        infoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                warnButton.setSelected(false);
                errorButton.setSelected(false);
                infoButton.setSelected(true);
                logger.setLogLevel(ILogger.INFO);
                logTableModel.fireTableDataChanged();
            }
        });
        option.add(infoButton);
        warnButton = UIUtils.createWarnButton();
        warnButton.setToolTipText("Show only warnings and errors");
        warnButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                warnButton.setSelected(true);
                errorButton.setSelected(false);
                infoButton.setSelected(false);
                logger.setLogLevel(ILogger.WARN);
                logTableModel.fireTableDataChanged();
            }
        });
        option.add(warnButton);
        errorButton = UIUtils.createErrorButton();
        errorButton.setToolTipText("Show only errors");
        errorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                warnButton.setSelected(false);
                errorButton.setSelected(true);
                infoButton.setSelected(false);
                logger.setLogLevel(ILogger.ERROR);
                logTableModel.fireTableDataChanged();
            }
        });
        option.add(errorButton);
        barPanel.add(option, new ToolBarConstraints());
        VLToolBar toolBar = new VLToolBar();
        JButton clear = UIUtils.createClearButton();
        clear.setToolTipText("Clear");
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clear();
            }
        });
        showMessage = UIUtils.createShowMessageButton();
        showMessage.setToolTipText("Show message");
        showMessage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = logTable.getSelectedRow();
                if (row == -1)
                    return;
                LogRecord record = logList.getRecordAt(row);
                showMessage(record);
            }
        });
        toolBar.add(showMessage);
        toolBar.add(clear);
        barPanel.add(toolBar, new ToolBarConstraints());
    }

    @Inject private IEditorProvider editorProvider;
    private ILogger logger;

    protected void showMessage(LogRecord record) {
        if (record.getDescription() != null) {
            String title = "Log @" + dateTimeInstance.format(record.getDate()) + " >" + record.getModule();
            new MessageDialog(record.getDescription(), title, editorProvider).setVisible(true);
        }
    }

    private class LogTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        String[] columnNames = { "", "Message", "Module", "Date" };

        public String getColumnName(int column) {
            return columnNames[column];
        }

        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0)
                return Icon.class;
            return String.class;
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            if (logList == null)
                return 0;
            return logList.count();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            LogRecord result = logList.getRecordAt(rowIndex);
            switch (columnIndex) {
            case 0:
                int type = result.getType();
                if (type == ILogger.INFO)
                    return ICON_INFO;
                else if (type == ILogger.MESSAGE)
                    return ICON_MSG;
                else if (type == ILogger.WARN)
                    return ICON_WARNING;
                else
                    return ICON_ERROR;
            case 1:
                return result.getMessage();
            case 2:
                return result.getModule();
            case 3:
                return dateTimeInstance.format(result.getDate());
            default:
                return "";
            }
        }
    }

    public void addLog(LogRecord result) {
        logList.add(result);
        logTableModel.fireTableDataChanged();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JScrollBar vbar = tableScrollPane.getVerticalScrollBar();
                vbar.setValue(vbar.getMaximum());
            }
        });
        if (errorListener != null && result.getType() == ILogger.ERROR)
            errorListener.addError(result);
    }

    public void clear() {
        logList.clear();
        this.logTableModel.fireTableDataChanged();
    }

    public Component getComponent() {
        return panel;
    }

    public DockKey getDockKey() {
        return DOCK_KEY;
    }

    public void setLogger(ILogger logger) {
        this.logger = logger;
        int logLevel = logger.getLogLevel();
        warnButton.setSelected(false);
        infoButton.setSelected(false);
        errorButton.setSelected(false);
        if (logLevel == ILogger.WARN)
            warnButton.setSelected(true);
        else if (logLevel == ILogger.ERROR)
            errorButton.setSelected(true);
        else if (logLevel == ILogger.INFO)
            infoButton.setSelected(true);
        logTableModel.fireTableDataChanged();
    }

    public void setErrorListener(IErrorListener controller) {
        this.errorListener = controller;
    }
}
