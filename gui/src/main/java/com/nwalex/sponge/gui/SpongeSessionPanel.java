/**
 * Copyright (c) Neill Alexander. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software
 */
package com.nwalex.sponge.gui;

import com.nwalex.sponge.gui.plugins.LoadedPlugin;
import com.nwalex.sponge.gui.plugins.PluginController;
import com.nwalex.sponge.gui.plugins.PluginManager;
import com.nwalex.sponge.plugin.Plugin;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.PatternPredicate;

/**
 * This class enables having multiple sessions
 */
public class SpongeSessionPanel extends javax.swing.JPanel {

  private static final Logger log = Logger.getLogger(SpongeSessionPanel.class);
  private final SpongeGUIController controller;
  private final FindDialogController findController;
  private final SpongeGUI parent;
  private int rowSelectedBeforeDelete = -1;

  public SpongeSessionPanel(final SpongeGUI parent, final SpongeGUIController controller, final PluginController pluginController) {
    this.controller = controller;
    this.parent = parent;
    this.findController = new FindDialogController(parent);

    initComponents();
    initPlugins(pluginController);
    initModeSelector();

    // set up the key handlers
    exchangeTable.getActionMap().put("LABEL_EX", controller.getLabelExchangeAction((JXTable) exchangeTable));
    exchangeTable.getActionMap().put("DELETE_LABEL", controller.getDeleteLabelAction((JXTable) exchangeTable));
    exchangeTable.getActionMap().put("DEFAULT_RESPONSE", controller.getSetDefaultResponseAction((JXTable) exchangeTable));
    exchangeTable.getActionMap().put("DELETE_ROW", getWrappedDeleteAction());
    exchangeTable.getActionMap().put("DUPLICATE", controller.getDuplicateRowAction((JXTable) exchangeTable));

    exchangeTable.getActionMap().put("PACK", new AbstractAction() {

      @Override
      public void actionPerformed(ActionEvent e) {
        ((JXTable) exchangeTable).packAll();
      }
    });

    final Action resendRequestAction = controller.getResendRequestAction((JXTable) exchangeTable);
    exchangeTable.getActionMap().put("SEND", resendRequestAction);

    InputMap im = exchangeTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0), "LABEL_EX");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0), "DELETE_LABEL");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "DEFAULT_RESPONSE");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_ROW");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "PACK");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "SEND");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "DUPLICATE");

    // allow to move up / down with k / j
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_K, 0), im.get(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0)));
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_J, 0), im.get(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0)));
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), im.get(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)));

    // remove the key stroke for ctrl f from the table
    im.remove(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));

    // now add a global find action to the table and the 2 display panels
    initFindAction();

    // add highlighter to table
    ((JXTable) exchangeTable).addHighlighter(new ColorHighlighter(
            new PatternPredicate("R", 7),
            new Color(213, 234, 212), Color.BLACK));

    updateSelectedMode(controller);

    // set up listener to update display panels when data changes
    exchangeTable.getModel().addTableModelListener(new TableModelListener() {

      @Override
      public void tableChanged(TableModelEvent e) {
        try {
          if (e.getType() == TableModelEvent.UPDATE) {
            int rawIndex = exchangeTable.getSelectedRow();

            if (rawIndex > -1) {
              int selectedIndex = ((JXTable) exchangeTable).convertRowIndexToModel(rawIndex);
              requestPanel.setText(controller.getRequestDataForRow(selectedIndex), selectedIndex);
              responsePanel.setText(controller.getResponseDataForRow(selectedIndex), selectedIndex);
            }
          }
        } catch (Exception ex) {
          log.error("Exception while getting updated data", ex);
        }
      }
    });

    // set up a listener to highlight row on right click
    exchangeTable.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          // ignore if there are multiple rows selected
          int clickedRow = exchangeTable.rowAtPoint(e.getPoint());

          if (exchangeTable.getSelectedRowCount() > 1 && exchangeTable.isRowSelected(clickedRow)) {
            tablePopup.show(exchangeTable, e.getPoint().x, e.getPoint().y);
          } else {
            if (clickedRow > -1) {
              exchangeTable.getSelectionModel().setSelectionInterval(clickedRow, clickedRow);
              tablePopup.show(exchangeTable, e.getPoint().x, e.getPoint().y);
            } else {
              e.consume();
            }
          }
        }
      }
    });
  }

  private void initModeSelector() {
    modeSelector.removeAllItems();
    modeSelector.addItem(SpongeGUIController.FORWARD_ALL);
    modeSelector.addItem(SpongeGUIController.REPLAY_OR_FORWARD);
    updateSelectedMode(controller);

    modeSelector.addItemListener(new ItemListener() {

      @Override
      public void itemStateChanged(ItemEvent evt) {
        if (evt.getStateChange() == ItemEvent.SELECTED) {
          controller.setMode((String) evt.getItem());
        }
      }
    });
  }

  void updateSelectedMode(final SpongeGUIController controller) {
    modeSelector.setSelectedItem(controller.getMode());
  }

  private Action getLoadSessionAction() {
    return new AbstractAction() {

      @Override
      public void actionPerformed(ActionEvent e) {
        controller.getLoadAction().actionPerformed(e);
        updateSelectedMode(controller);
      }
    };
  }

  private Action getWrappedDeleteAction() {
    final Action deleteRowAction = controller.getDeleteRowAction((JXTable) exchangeTable);

    final Action wrappedDeleteAction = new AbstractAction() {

      @Override
      public void actionPerformed(ActionEvent e) {
        rowSelectedBeforeDelete = exchangeTable.getSelectedRow();
        deleteRowAction.actionPerformed(e);
      }
    };

    deleteRowAction.addPropertyChangeListener(new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("enabled")) {
          wrappedDeleteAction.setEnabled((Boolean) evt.getNewValue());
        }
      }
    });

    return wrappedDeleteAction;
  }

  private void updateDisplayedData(int rawIndex) {
    int selectedIndex = ((JXTable) exchangeTable).convertRowIndexToModel(rawIndex);
    requestPanel.setText(controller.getRequestDataForRow(selectedIndex), selectedIndex);
    responsePanel.setText(controller.getResponseDataForRow(selectedIndex), selectedIndex);
  }

  private void initFindAction() {
    findController.initFindActionOn(exchangeTable);
    findController.initFindActionOn(requestPanel);
    findController.initFindActionOn(responsePanel);
  }

  private JTable createExchangeTable() {
    return new JXTable(controller.getExchangeTableModel()) {

      @Override
      public void tableChanged(TableModelEvent ev) {
        super.tableChanged(ev);

        // when a delete is done, keep the current row highlighted (if possible)
        if (ev.getType() == TableModelEvent.DELETE) {
          int numRows = exchangeTable.getRowCount();
          int selectedRow = rowSelectedBeforeDelete;

          if (numRows > 0) {
            if (selectedRow < numRows) {
              exchangeTable.setRowSelectionInterval(selectedRow, selectedRow);
              updateDisplayedData(selectedRow);
            } else {
              exchangeTable.setRowSelectionInterval(numRows - 1, numRows - 1);
              updateDisplayedData(numRows - 1);
            }
          } else {
            requestPanel.setText("", -1);
            responsePanel.setText("", -1);
          }
        }
      }
    };
  }

  private void initPlugins(final PluginController pluginController) {
    PluginManager pluginManager = pluginController.getPluginManager().init();

    for (LoadedPlugin loadedPlugin : pluginManager.getAllLoadedPlugins()) {
      final Plugin plugin = loadedPlugin.getPlugin();
      JCheckBoxMenuItem pluginMenuItem = new JCheckBoxMenuItem(loadedPlugin.getName(), loadedPlugin.isEnabled());
      pluginMenuItem.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          JCheckBoxMenuItem mi = (JCheckBoxMenuItem) e.getSource();
          try {
            if (mi.isSelected()) {
              pluginController.pluginEnabled(plugin);
            } else {
              pluginController.pluginDisabled(plugin);
            }
          } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, ex.getClass().getName() + ": " + ex.getMessage(),
                    "Exception", JOptionPane.ERROR_MESSAGE);
            mi.setEnabled(false);
          }
        }
      });

      pluginPopup.add(pluginMenuItem);
    }

    if (pluginManager.getAllLoadedPlugins().size() == 0) {
      controlPanel.remove(pluginSelector);
    } else {
      pluginSelector.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
          maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
          pluginPopup.show(e.getComponent(),
                  e.getX(), e.getY());
        }
      });
    }
  }

  void setSessionInfo(String text) {
    configLabel.setText(text);
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    tablePopup = new javax.swing.JPopupMenu();
    attachLabelItem = new javax.swing.JMenuItem();
    deleteLabelItem = new javax.swing.JMenuItem();
    setDefaultResponseItem = new javax.swing.JMenuItem();
    resendRequestItem = new javax.swing.JMenuItem();
    duplicate = new javax.swing.JMenuItem();
    jSeparator1 = new javax.swing.JPopupMenu.Separator();
    delete = new javax.swing.JMenuItem();
    modeButtonGroup = new javax.swing.ButtonGroup();
    pluginPopup = new javax.swing.JPopupMenu();
    jSplitPane1 = new javax.swing.JSplitPane();
    jScrollPane2 = new javax.swing.JScrollPane();
    exchangeTable = createExchangeTable();
    //((JXTable) exchangeTable).setHighlighters(new Highlighter[] {HighlighterFactory.createSimpleStriping()});
    ((JXTable) exchangeTable).setColumnControlVisible(true);
    jSplitPane2 = new javax.swing.JSplitPane();
    requestPanel = new BodyPanel(controller.getUpdateRequestBodyAction((JXTable) exchangeTable));
    responsePanel = new BodyPanel(controller.getUpdateResponseBodyAction((JXTable) exchangeTable));
    controlPanel = new javax.swing.JPanel();
    configLabel = new javax.swing.JLabel();
    jButton1 = new javax.swing.JButton();
    startStopServerButton = new javax.swing.JButton();
    jButton2 = new javax.swing.JButton();
    modeSelector = new javax.swing.JComboBox();
    pluginSelector = new javax.swing.JButton();
    loadButton = new javax.swing.JButton();
    saveButton = new javax.swing.JButton();

    attachLabelItem.setAction(controller.getLabelExchangeAction((JXTable) exchangeTable));
    attachLabelItem.setText("Label...");
    tablePopup.add(attachLabelItem);

    deleteLabelItem.setAction(controller.getDeleteLabelAction((JXTable) exchangeTable));
    deleteLabelItem.setText("Unlabel");
    tablePopup.add(deleteLabelItem);

    setDefaultResponseItem.setAction(controller.getSetDefaultResponseAction((JXTable) exchangeTable));
    setDefaultResponseItem.setText("Replay");
    tablePopup.add(setDefaultResponseItem);

    resendRequestItem.setAction(controller.getResendRequestAction((JXTable ) exchangeTable));
    resendRequestItem.setText("Resend");
    tablePopup.add(resendRequestItem);

    duplicate.setAction(controller.getDuplicateRowAction((JXTable) exchangeTable));
    duplicate.setText("Duplicate");
    tablePopup.add(duplicate);
    tablePopup.add(jSeparator1);

    delete.setAction(getWrappedDeleteAction());
    delete.setText("Delete");
    tablePopup.add(delete);

    jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
    jSplitPane1.setResizeWeight(0.5);
    jSplitPane1.setContinuousLayout(true);
    jSplitPane1.setDoubleBuffered(true);

    jScrollPane2.setPreferredSize(new java.awt.Dimension(800, 280));

    exchangeTable.setModel(controller.getExchangeTableModel());
    exchangeTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    exchangeTable.getSelectionModel().addListSelectionListener(
      new ListSelectionListener() {

        public void valueChanged(ListSelectionEvent e) {
          if (e.getValueIsAdjusting()) {
            return;
          }

          ListSelectionModel rowSM = (ListSelectionModel) e.getSource();

          // check for more than one row selected
          if (!(rowSM.getMinSelectionIndex() == rowSM.getMaxSelectionIndex()) || rowSM.isSelectionEmpty()) {
            requestPanel.setText("", -1);
            responsePanel.setText("", -1);            
          } else {
            int rawIndex = rowSM.getMinSelectionIndex();         

            if (rawIndex > -1) {  
              findController.resetFindNext();
              updateDisplayedData(rawIndex); 
              requestPanel.displayReadOnlyView();
              responsePanel.displayReadOnlyView();
            }
          }
        }
      });

      jScrollPane2.setViewportView(exchangeTable);

      jSplitPane1.setLeftComponent(jScrollPane2);

      jSplitPane2.setResizeWeight(0.5);
      jSplitPane2.setContinuousLayout(true);
      jSplitPane2.setDoubleBuffered(true);
      jSplitPane2.setPreferredSize(new java.awt.Dimension(800, 280));
      jSplitPane2.setLeftComponent(requestPanel);
      jSplitPane2.setRightComponent(responsePanel);

      jSplitPane1.setBottomComponent(jSplitPane2);

      configLabel.setText("Configuration info to go here...");
      controlPanel.add(configLabel);

      jButton1.setAction(controller.getConfigureAction());
      jButton1.setText("Configure...");
      controlPanel.add(jButton1);

      startStopServerButton.setAction(controller.getStartServerAction());
      startStopServerButton.setText("Start Server");
      controlPanel.add(startStopServerButton);

      jButton2.setAction(controller.getStopServerAction());
      jButton2.setText("Stop Server");
      controlPanel.add(jButton2);

      modeSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Modes" }));
      controlPanel.add(modeSelector);

      pluginSelector.setText("Plugins...");
      pluginSelector.setComponentPopupMenu(pluginPopup);
      controlPanel.add(pluginSelector);

      loadButton.setAction(getLoadSessionAction());
      loadButton.setText("Load Session...");
      controlPanel.add(loadButton);

      saveButton.setAction(controller.getSaveAsAction());
      saveButton.setText("Save Session As...");
      controlPanel.add(saveButton);

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(controlPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 934, Short.MAX_VALUE)
        .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 934, Short.MAX_VALUE)
      );
      layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
          .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE)
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addComponent(controlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      );
    }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JMenuItem attachLabelItem;
  private javax.swing.JLabel configLabel;
  private javax.swing.JPanel controlPanel;
  private javax.swing.JMenuItem delete;
  private javax.swing.JMenuItem deleteLabelItem;
  private javax.swing.JMenuItem duplicate;
  private javax.swing.JTable exchangeTable;
  private javax.swing.JButton jButton1;
  private javax.swing.JButton jButton2;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JPopupMenu.Separator jSeparator1;
  private javax.swing.JSplitPane jSplitPane1;
  private javax.swing.JSplitPane jSplitPane2;
  private javax.swing.JButton loadButton;
  private javax.swing.ButtonGroup modeButtonGroup;
  private javax.swing.JComboBox modeSelector;
  private javax.swing.JPopupMenu pluginPopup;
  private javax.swing.JButton pluginSelector;
  private com.nwalex.sponge.gui.BodyPanel requestPanel;
  private javax.swing.JMenuItem resendRequestItem;
  private com.nwalex.sponge.gui.BodyPanel responsePanel;
  private javax.swing.JButton saveButton;
  private javax.swing.JMenuItem setDefaultResponseItem;
  private javax.swing.JButton startStopServerButton;
  private javax.swing.JPopupMenu tablePopup;
  // End of variables declaration//GEN-END:variables
}
