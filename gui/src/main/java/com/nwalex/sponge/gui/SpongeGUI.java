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
import java.awt.Component;
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
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
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
    exchangeTable.getActionMap().put("DELETE_ROW", controller.getDeleteRowAction());

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
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "DELETE_LABEL");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "DEFAULT_RESPONSE");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_ROW");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "PACK");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "SEND");

    // allow to move up / down with k / j
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_K, 0), im.get(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0)));
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_J, 0), im.get(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0)));
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), im.get(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)));

    // add highlighter to table
    ((JXTable) exchangeTable).addHighlighter(new ColorHighlighter(
            new PatternPredicate("R", 7),
            new Color(213, 234, 212), Color.BLACK));
    
    updateSelectedMode(controller);
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
    modeButtonGroup = new javax.swing.ButtonGroup();
    jScrollPane2 = new javax.swing.JScrollPane();
    exchangeTable = new JXTable(controller.getExchangeTableModel());
    ((JXTable) exchangeTable).setHighlighters(new Highlighter[] {HighlighterFactory.createSimpleStriping()});
    ((JXTable) exchangeTable).setColumnControlVisible(true);
    jScrollPane1 = new javax.swing.JScrollPane();
    requestTextArea = new javax.swing.JTextArea();
    jScrollPane3 = new javax.swing.JScrollPane();
    responseArea = new javax.swing.JTextArea();
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
    exitMenuItem = new javax.swing.JMenuItem();
    exitMenuItem.setAction(controller.getExitAction());
    modeMenu = new javax.swing.JMenu();
    forwardAllMenuItem = new javax.swing.JRadioButtonMenuItem();
    replayOrForwardMenuItem = new javax.swing.JRadioButtonMenuItem();
    replayOrFailMenuItem = new javax.swing.JRadioButtonMenuItem();
    deleteLabelMenuItem = new javax.swing.JMenu();
    attachLabelMenuItem = new javax.swing.JMenuItem();
    jMenuItem1 = new javax.swing.JMenuItem();
    clearAllMenuItem = new javax.swing.JMenuItem();
    resendRequestMenuItem = new javax.swing.JMenuItem();
    replMenu = new javax.swing.JMenu();
    replMenuItem = new javax.swing.JMenuItem();
    replMenuItem.setAction(controller.getStartReplAction());

    attachLabelItem.setAction(controller.getLabelExchangeAction());
    attachLabelItem.setText("Attach Label...");
    tablePopup.add(attachLabelItem);

    deleteLabelItem.setAction(controller.getDeleteLabelAction());
    deleteLabelItem.setText("Delete Label");
    tablePopup.add(deleteLabelItem);

    setDefaultResponseItem.setAction(controller.getSetDefaultResponseAction());
    setDefaultResponseItem.setText("Use this Response");
    tablePopup.add(setDefaultResponseItem);

    resendRequestItem.setAction(controller.getResendRequestAction());
    resendRequestItem.setText("Resend Request");
    tablePopup.add(resendRequestItem);

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

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
            int selectedIndex = ((JXTable) exchangeTable).convertRowIndexToModel(rawIndex);

            controller.setSelectedRow(selectedIndex);
            requestTextArea.setText(controller.getRequestDataForRow(selectedIndex)); 
            requestTextArea.setCaretPosition(0);

            responseArea.setText(controller.getResponseDataForRow(selectedIndex)); 
            responseArea.setCaretPosition(0);
          } else {
            controller.setSelectedRow(-1);
          }
        }
      });

      jScrollPane2.setViewportView(exchangeTable);

      requestTextArea.setColumns(20);
      requestTextArea.setEditable(false);
      requestTextArea.setRows(5);
      jScrollPane1.setViewportView(requestTextArea);

      responseArea.setColumns(20);
      responseArea.setEditable(false);
      responseArea.setRows(5);
      jScrollPane3.setViewportView(responseArea);

      jMenu1.setText("File");

      loadMenuItem.setAction(getLoadSessionAction());
      loadMenuItem.setText("Load Session...");
      jMenu1.add(loadMenuItem);

      saveMenuItem.setAction(controller.getSaveAction());
      saveMenuItem.setText("Save Session");
      jMenu1.add(saveMenuItem);

      saveAsMenuItem.setAction(controller.getSaveAsAction());
      saveAsMenuItem.setText("Save Session As...");
      jMenu1.add(saveAsMenuItem);

      menuBar.add(jMenu1);

      serverMenu.setText("Server");

      startServerMenuItem.setText("Start Server");
      serverMenu.add(startServerMenuItem);

      stopServerMenuItem.setText("Stop Server");
      serverMenu.add(stopServerMenuItem);

      configureMenuItem.setText("Configure...");
      serverMenu.add(configureMenuItem);

      exitMenuItem.setText("Exit");
      exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          exitMenuItemActionPerformed(evt);
        }
      });
      serverMenu.add(exitMenuItem);

      menuBar.add(serverMenu);

      modeMenu.setText("Mode");

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

      modeButtonGroup.add(replayOrForwardMenuItem);
      replayOrForwardMenuItem.setText("Replay or Forward");
      replayOrForwardMenuItem.setActionCommand(SpongeGUIController.REPLAY_OR_FORWARD);
      replayOrForwardMenuItem.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
          replayOrForwardMenuItemActionPerformed(evt);
        }
      });
      modeMenu.add(replayOrForwardMenuItem);

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

      attachLabelMenuItem.setText("Attach Label...");
      attachLabelMenuItem.setAction(controller.getLabelExchangeAction());
      deleteLabelMenuItem.add(attachLabelMenuItem);

      jMenuItem1.setAction(controller.getDeleteLabelAction());
      jMenuItem1.setText("Delete Label");
      deleteLabelMenuItem.add(jMenuItem1);

      clearAllMenuItem.setText("Clear All");
      clearAllMenuItem.setAction(controller.getClearAllAction());
      deleteLabelMenuItem.add(clearAllMenuItem);

      resendRequestMenuItem.setAction(controller.getResendRequestAction());
      resendRequestMenuItem.setText("Resend Request");
      deleteLabelMenuItem.add(resendRequestMenuItem);

      menuBar.add(deleteLabelMenuItem);

      replMenu.setText("Repl");

      replMenuItem.setText("Start Repl");
      replMenu.add(replMenuItem);

      menuBar.add(replMenu);

      setJMenuBar(menuBar);

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
          .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE))
        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
      );
      layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
          .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)))
      );

      pack();
    }// </editor-fold>//GEN-END:initComponents
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
      this.dispose();
    }//GEN-LAST:event_exitMenuItemActionPerformed

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
  private javax.swing.JMenuItem attachLabelMenuItem;
  private javax.swing.JMenuItem clearAllMenuItem;
  private javax.swing.JMenuItem configureMenuItem;
  private javax.swing.JMenuItem deleteLabelItem;
  private javax.swing.JMenu deleteLabelMenuItem;
  private javax.swing.JTable exchangeTable;
  private javax.swing.JMenuItem exitMenuItem;
  private javax.swing.JRadioButtonMenuItem forwardAllMenuItem;
  private javax.swing.JMenu jMenu1;
  private javax.swing.JMenuItem jMenuItem1;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JScrollPane jScrollPane3;
  private javax.swing.JMenuItem loadMenuItem;
  private javax.swing.JMenuBar menuBar;
  private javax.swing.ButtonGroup modeButtonGroup;
  private javax.swing.JMenu modeMenu;
  private javax.swing.JMenu replMenu;
  private javax.swing.JMenuItem replMenuItem;
  private javax.swing.JRadioButtonMenuItem replayOrFailMenuItem;
  private javax.swing.JRadioButtonMenuItem replayOrForwardMenuItem;
  private javax.swing.JTextArea requestTextArea;
  private javax.swing.JMenuItem resendRequestItem;
  private javax.swing.JMenuItem resendRequestMenuItem;
  private javax.swing.JTextArea responseArea;
  private javax.swing.JMenuItem saveAsMenuItem;
  private javax.swing.JMenuItem saveMenuItem;
  private javax.swing.JMenu serverMenu;
  private javax.swing.JMenuItem setDefaultResponseItem;
  private javax.swing.JMenuItem startServerMenuItem;
  private javax.swing.JMenuItem stopServerMenuItem;
  private javax.swing.JPopupMenu tablePopup;
  // End of variables declaration//GEN-END:variables
}
