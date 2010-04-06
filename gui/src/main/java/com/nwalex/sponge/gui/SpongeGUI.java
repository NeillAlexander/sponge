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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.PatternPredicate;

/**
 *
 * @author neill
 */
public class SpongeGUI extends javax.swing.JFrame {

  private final SpongeGUIController controller;

  /** Creates new form SpongeGUI */
  public SpongeGUI(final SpongeGUIController controller) {
    this.controller = controller;
    initComponents();

    setExtendedState(JFrame.MAXIMIZED_BOTH);

    // set up the key handlers
    exchangeTable.getActionMap().put("LABEL_EX", controller.getLabelExchangeAction());
    exchangeTable.getActionMap().put("DELETE_LABEL", controller.getDeleteLabelAction());
    exchangeTable.getActionMap().put("DEFAULT_RESPONSE", controller.getSetDefaultResponseAction());
    exchangeTable.getActionMap().put("DELETE_ROW", getWrappedDeleteAction());
    exchangeTable.getActionMap().put("DUPLICATE", controller.getDuplicateRowAction());

    exchangeTable.getActionMap().put("PACK", new AbstractAction() {

      @Override
      public void actionPerformed(ActionEvent e) {
        ((JXTable) exchangeTable).packAll();
      }
    });

    exchangeTable.getActionMap().put("SEND", new AbstractAction() {

      @Override
      public void actionPerformed(ActionEvent e) {
        controller.getResendRequestAction().actionPerformed(e);
      }
    });

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

    // add highlighter to table
    ((JXTable) exchangeTable).addHighlighter(new ColorHighlighter(
            new PatternPredicate("R", 7),
            new Color(213, 234, 212), Color.BLACK));

    updateSelectedMode(controller);

    // set up listener to update display panels when data changes
    exchangeTable.getModel().addTableModelListener(new TableModelListener() {

      @Override
      public void tableChanged(TableModelEvent e) {
        if (!(e.getType() == TableModelEvent.DELETE)) {
          int rawIndex = exchangeTable.getSelectedRow();

          if (rawIndex > -1) {
            int selectedIndex = ((JXTable) exchangeTable).convertRowIndexToModel(rawIndex);
            requestPanel.setText(controller.getRequestDataForRow(selectedIndex));
            responsePanel.setText(controller.getResponseDataForRow(selectedIndex));
          }
        }
      }
    });
  }

  private Action getWrappedDeleteAction() {
    return new AbstractAction() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (controller.getDeleteRowAction().isEnabled()) {
          int selectedRow = exchangeTable.getSelectedRow();
          controller.getDeleteRowAction().actionPerformed(e);

          int numRows = exchangeTable.getRowCount();

          if (selectedRow > 0) {
            selectedRow = selectedRow - 1;
          }

          if (numRows > 0) {
            if (selectedRow < numRows) {
              exchangeTable.setRowSelectionInterval(selectedRow, selectedRow);
              updateDisplayedData(selectedRow);
            } else {
              exchangeTable.setRowSelectionInterval(numRows - 1, numRows - 1);
              updateDisplayedData(numRows - 1);
            }
          } else {
            requestPanel.setText("");
            responsePanel.setText("");
            controller.setSelectedRow(-1);
          }
        }
      }
    };
  }

  private void updateSelectedMode(final SpongeGUIController controller) {
    // set the mode
    Enumeration<AbstractButton> en = modeButtonGroup.getElements();
    while (en.hasMoreElements()) {
      AbstractButton button = en.nextElement();
      if (button.getActionCommand().equals(controller.getMode())) {
        modeButtonGroup.setSelected(button.getModel(), true);
      }
    }
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

  private void updateDisplayedData(int rawIndex) {
    int selectedIndex = ((JXTable) exchangeTable).convertRowIndexToModel(rawIndex);

    controller.setSelectedRow(selectedIndex);
    requestPanel.setText(controller.getRequestDataForRow(selectedIndex));
    responsePanel.setText(controller.getResponseDataForRow(selectedIndex));
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
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
    jSplitPane1 = new javax.swing.JSplitPane();
    jScrollPane2 = new javax.swing.JScrollPane();
    exchangeTable = new JXTable(controller.getExchangeTableModel());
    ((JXTable) exchangeTable).setHighlighters(new Highlighter[] {HighlighterFactory.createSimpleStriping()});
    ((JXTable) exchangeTable).setColumnControlVisible(true);
    jPanel1 = new javax.swing.JPanel();
    requestPanel = new BodyPanel(controller.getUpdateRequestBodyAction());
    responsePanel = new BodyPanel(controller.getUpdateResponseBodyAction());
    menuBar = new javax.swing.JMenuBar();
    jMenu1 = new javax.swing.JMenu();
    loadMenuItem = new javax.swing.JMenuItem();
    saveMenuItem = new javax.swing.JMenuItem();
    saveAsMenuItem = new javax.swing.JMenuItem();
    serverMenu = new javax.swing.JMenu();
    startServerMenuItem = new javax.swing.JMenuItem();
    startServerMenuItem.setAction(controller.getStartServerAction());
    stopServerMenuItem = new javax.swing.JMenuItem();
    stopServerMenuItem.setAction(controller.getStopServerAction());
    configureMenuItem = new javax.swing.JMenuItem();
    configureMenuItem.setAction(controller.getConfigureAction());
    modeMenu = new javax.swing.JMenu();
    forwardAllMenuItem = new javax.swing.JRadioButtonMenuItem();
    replayOrForwardMenuItem = new javax.swing.JRadioButtonMenuItem();
    replayOrFailMenuItem = new javax.swing.JRadioButtonMenuItem();
    deleteLabelMenuItem = new javax.swing.JMenu();
    clearAllMenuItem = new javax.swing.JMenuItem();
    replMenu = new javax.swing.JMenu();
    replMenuItem = new javax.swing.JMenuItem();
    replMenuItem.setAction(controller.getStartReplAction());

    attachLabelItem.setAction(controller.getLabelExchangeAction());
    attachLabelItem.setText("Label...");
    tablePopup.add(attachLabelItem);

    deleteLabelItem.setAction(controller.getDeleteLabelAction());
    deleteLabelItem.setText("Unlabel");
    tablePopup.add(deleteLabelItem);

    setDefaultResponseItem.setAction(controller.getSetDefaultResponseAction());
    setDefaultResponseItem.setText("Replay");
    tablePopup.add(setDefaultResponseItem);

    resendRequestItem.setAction(controller.getResendRequestAction());
    resendRequestItem.setText("Resend");
    tablePopup.add(resendRequestItem);

    duplicate.setAction(controller.getDuplicateRowAction());
    duplicate.setText("Duplicate");
    tablePopup.add(duplicate);
    tablePopup.add(jSeparator1);

    delete.setAction(getWrappedDeleteAction());
    delete.setText("Delete");
    tablePopup.add(delete);

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

    jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

    exchangeTable.setModel(controller.getExchangeTableModel());
    exchangeTable.setComponentPopupMenu(tablePopup);
    exchangeTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    exchangeTable.getSelectionModel().addListSelectionListener(
      new ListSelectionListener() {

        public void valueChanged(ListSelectionEvent e) {
          if (e.getValueIsAdjusting()) {
            return;
          }

          ListSelectionModel rowSM = (ListSelectionModel) e.getSource();
          int rawIndex = rowSM.getMinSelectionIndex();         

          if (rawIndex > -1) {                  
            updateDisplayedData(rawIndex); 
            requestPanel.displayReadOnlyView();
            responsePanel.displayReadOnlyView();
          } else {
            controller.setSelectedRow(-1);
            requestPanel.setText("");
            responsePanel.setText("");
          }
        }
      });

      jScrollPane2.setViewportView(exchangeTable);

      jSplitPane1.setLeftComponent(jScrollPane2);

      javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
          .addComponent(requestPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addComponent(responsePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE))
      );
      jPanel1Layout.setVerticalGroup(
        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(requestPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE)
        .addComponent(responsePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE)
      );

      jSplitPane1.setRightComponent(jPanel1);

      jMenu1.setText("File");

      loadMenuItem.setAction(getLoadSessionAction());
      loadMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
      loadMenuItem.setText("Load Session...");
      jMenu1.add(loadMenuItem);

      saveMenuItem.setAction(controller.getSaveAction());
      saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
      saveMenuItem.setText("Save Session");
      jMenu1.add(saveMenuItem);

      saveAsMenuItem.setAction(controller.getSaveAsAction());
      saveAsMenuItem.setText("Save Session As...");
      jMenu1.add(saveAsMenuItem);

      menuBar.add(jMenu1);

      serverMenu.setText("Server");

      startServerMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, java.awt.event.InputEvent.CTRL_MASK));
      startServerMenuItem.setText("Start Server");
      serverMenu.add(startServerMenuItem);

      stopServerMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
      stopServerMenuItem.setText("Stop Server");
      serverMenu.add(stopServerMenuItem);

      configureMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PERIOD, java.awt.event.InputEvent.CTRL_MASK));
      configureMenuItem.setText("Configure...");
      serverMenu.add(configureMenuItem);

      menuBar.add(serverMenu);

      modeMenu.setText("Mode");

      forwardAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_1, java.awt.event.InputEvent.CTRL_MASK));
      modeButtonGroup.add(forwardAllMenuItem);
      forwardAllMenuItem.setSelected(true);
      forwardAllMenuItem.setText("Forward All");
      forwardAllMenuItem.setActionCommand(SpongeGUIController.FORWARD_ALL);
      forwardAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          modeSelected(evt);
        }
      });
      modeMenu.add(forwardAllMenuItem);

      replayOrForwardMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, java.awt.event.InputEvent.CTRL_MASK));
      modeButtonGroup.add(replayOrForwardMenuItem);
      replayOrForwardMenuItem.setText("Replay or Forward");
      replayOrForwardMenuItem.setActionCommand(SpongeGUIController.REPLAY_OR_FORWARD);
      replayOrForwardMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          replayOrForwardMenuItemActionPerformed(evt);
        }
      });
      modeMenu.add(replayOrForwardMenuItem);

      replayOrFailMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_3, java.awt.event.InputEvent.CTRL_MASK));
      modeButtonGroup.add(replayOrFailMenuItem);
      replayOrFailMenuItem.setText("Replay or Fail");
      replayOrFailMenuItem.setActionCommand(SpongeGUIController.REPLAY_OR_FAIL);
      replayOrFailMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          replayOrFailMenuItemActionPerformed(evt);
        }
      });
      modeMenu.add(replayOrFailMenuItem);

      menuBar.add(modeMenu);

      deleteLabelMenuItem.setText("Action");

      clearAllMenuItem.setText("Clear All");
      clearAllMenuItem.setAction(controller.getClearAllAction());
      deleteLabelMenuItem.add(clearAllMenuItem);

      menuBar.add(deleteLabelMenuItem);

      replMenu.setText("REPL");

      replMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
      replMenuItem.setText("Start REPL");
      replMenu.add(replMenuItem);

      menuBar.add(replMenu);

      setJMenuBar(menuBar);

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING)
      );
      layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
          .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addContainerGap())
      );

      pack();
    }// </editor-fold>//GEN-END:initComponents

    private void modeSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modeSelected
      controller.setMode(modeButtonGroup.getSelection().getActionCommand());
    }//GEN-LAST:event_modeSelected

    private void replayOrForwardMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replayOrForwardMenuItemActionPerformed
      modeSelected(evt);
    }//GEN-LAST:event_replayOrForwardMenuItemActionPerformed

    private void replayOrFailMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replayOrFailMenuItemActionPerformed
      modeSelected(evt);
    }//GEN-LAST:event_replayOrFailMenuItemActionPerformed
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JMenuItem attachLabelItem;
  private javax.swing.JMenuItem clearAllMenuItem;
  private javax.swing.JMenuItem configureMenuItem;
  private javax.swing.JMenuItem delete;
  private javax.swing.JMenuItem deleteLabelItem;
  private javax.swing.JMenu deleteLabelMenuItem;
  private javax.swing.JMenuItem duplicate;
  private javax.swing.JTable exchangeTable;
  private javax.swing.JRadioButtonMenuItem forwardAllMenuItem;
  private javax.swing.JMenu jMenu1;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JPopupMenu.Separator jSeparator1;
  private javax.swing.JSplitPane jSplitPane1;
  private javax.swing.JMenuItem loadMenuItem;
  private javax.swing.JMenuBar menuBar;
  private javax.swing.ButtonGroup modeButtonGroup;
  private javax.swing.JMenu modeMenu;
  private javax.swing.JMenu replMenu;
  private javax.swing.JMenuItem replMenuItem;
  private javax.swing.JRadioButtonMenuItem replayOrFailMenuItem;
  private javax.swing.JRadioButtonMenuItem replayOrForwardMenuItem;
  private com.nwalex.sponge.gui.BodyPanel requestPanel;
  private javax.swing.JMenuItem resendRequestItem;
  private com.nwalex.sponge.gui.BodyPanel responsePanel;
  private javax.swing.JMenuItem saveAsMenuItem;
  private javax.swing.JMenuItem saveMenuItem;
  private javax.swing.JMenu serverMenu;
  private javax.swing.JMenuItem setDefaultResponseItem;
  private javax.swing.JMenuItem startServerMenuItem;
  private javax.swing.JMenuItem stopServerMenuItem;
  private javax.swing.JPopupMenu tablePopup;
  // End of variables declaration//GEN-END:variables
}
