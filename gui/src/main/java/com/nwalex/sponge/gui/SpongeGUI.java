/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SpongeGUI.java
 *
 * Created on 03-Mar-2010, 20:35:20
 */
package com.nwalex.sponge.gui;

import java.awt.event.KeyEvent;
import java.io.StringWriter;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

/**
 *
 * @author neill
 */
public class SpongeGUI extends javax.swing.JFrame {

  private final SpongeGUIController controller;

  /** Creates new form SpongeGUI */
  public SpongeGUI(SpongeGUIController controller) {
    this.controller = controller;    
    initComponents();

    setExtendedState(JFrame.MAXIMIZED_BOTH);

    // set up the key handlers
    exchangeTable.getActionMap().put("LABEL_EX", controller.getLabelExchangeAction());
    exchangeTable.getActionMap().put("DELETE_LABEL", controller.getDeleteLabelAction());

    InputMap im = exchangeTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0), "LABEL_EX");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "DELETE_LABEL");
  }

  public String prettyPrint(final String xml) {
    final StringWriter sw;

    try {
      final OutputFormat format = OutputFormat.createPrettyPrint();
      final org.dom4j.Document document = DocumentHelper.parseText(xml);
      sw = new StringWriter();
      final XMLWriter writer = new XMLWriter(sw, format);
      writer.write(document);
    } catch (Exception e) {
      throw new RuntimeException("Error pretty printing xml:\n" + xml, e);
    }
    return sw.toString();
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
    jScrollPane2 = new javax.swing.JScrollPane();
    exchangeTable = new JXTable(controller.getExchangeTableModel());
    ((JXTable) exchangeTable).setHighlighters(new Highlighter[] {HighlighterFactory.createSimpleStriping()});
    ((JXTable) exchangeTable).setColumnControlVisible(true);
    jScrollPane1 = new javax.swing.JScrollPane();
    requestTextArea = new javax.swing.JTextArea();
    jScrollPane3 = new javax.swing.JScrollPane();
    responseArea = new javax.swing.JTextArea();
    menuBar = new javax.swing.JMenuBar();
    serverMenu = new javax.swing.JMenu();
    startServerMenuItem = new javax.swing.JMenuItem();
    startServerMenuItem.setAction(controller.getStartServerAction());
    stopServerMenuItem = new javax.swing.JMenuItem();
    stopServerMenuItem.setAction(controller.getStopServerAction());
    configureMenuItem = new javax.swing.JMenuItem();
    configureMenuItem.setAction(controller.getConfigureAction());
    exitMenuItem = new javax.swing.JMenuItem();
    exitMenuItem.setAction(controller.getExitAction());
    deleteLabelMenuItem = new javax.swing.JMenu();
    attachLabelMenuItem = new javax.swing.JMenuItem();
    jMenuItem1 = new javax.swing.JMenuItem();
    clearAllMenuItem = new javax.swing.JMenuItem();
    replMenu = new javax.swing.JMenu();
    replMenuItem = new javax.swing.JMenuItem();
    replMenuItem.setAction(controller.getStartReplAction());

    attachLabelItem.setAction(controller.getLabelExchangeAction());
    attachLabelItem.setText("jMenuItem2");
    tablePopup.add(attachLabelItem);

    deleteLabelItem.setAction(controller.getDeleteLabelAction());
    deleteLabelItem.setText("jMenuItem2");
    tablePopup.add(deleteLabelItem);

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
          int selectedIndex = rowSM.getMinSelectionIndex();

          controller.setSelectedRow(selectedIndex);

          if (selectedIndex > -1) {                  

            requestTextArea.setText(prettyPrint(controller.getRequestDataForRow(selectedIndex))); 
            requestTextArea.setCaretPosition(0);

            responseArea.setText(prettyPrint(controller.getResponseDataForRow(selectedIndex))); 
            responseArea.setCaretPosition(0);
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

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JMenuItem attachLabelItem;
  private javax.swing.JMenuItem attachLabelMenuItem;
  private javax.swing.JMenuItem clearAllMenuItem;
  private javax.swing.JMenuItem configureMenuItem;
  private javax.swing.JMenuItem deleteLabelItem;
  private javax.swing.JMenu deleteLabelMenuItem;
  private javax.swing.JTable exchangeTable;
  private javax.swing.JMenuItem exitMenuItem;
  private javax.swing.JMenuItem jMenuItem1;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JScrollPane jScrollPane3;
  private javax.swing.JMenuBar menuBar;
  private javax.swing.JMenu replMenu;
  private javax.swing.JMenuItem replMenuItem;
  private javax.swing.JTextArea requestTextArea;
  private javax.swing.JTextArea responseArea;
  private javax.swing.JMenu serverMenu;
  private javax.swing.JMenuItem startServerMenuItem;
  private javax.swing.JMenuItem stopServerMenuItem;
  private javax.swing.JPopupMenu tablePopup;
  // End of variables declaration//GEN-END:variables
}
