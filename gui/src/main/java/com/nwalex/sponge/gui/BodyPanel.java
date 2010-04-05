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

import java.awt.CardLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JTextArea;

/**
 *
 * @author alexanc
 */
public class BodyPanel extends javax.swing.JPanel {

  private final SaveAction saveAction;

  public BodyPanel() {
    saveAction = null;
  }

  /** Creates new form BodyPanel */
  public BodyPanel(SaveAction saveAction) {
    this.saveAction = saveAction;
    initComponents();
    saveAction.setCallbacks(editingArea, this);
  }

  public void setText(String text) {
    displayArea.setText(text);
    displayArea.setCaretPosition(0);
  }

  public String getText() {
    return displayArea.getText();
  }

  public void toggleView() {
    ((CardLayout) getLayout()).next(this);
  }

  public void displayReadOnlyView() {
    ((CardLayout) getLayout()).show(this, "readOnlyCard");
  }

  public void displayEditView() {
    ((CardLayout) getLayout()).show(this, "editCard");
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    readOnlyContainer = new javax.swing.JPanel();
    readOnlyPanel = new javax.swing.JScrollPane();
    displayArea = new javax.swing.JTextArea();
    editContainer = new javax.swing.JPanel();
    editPanel = new javax.swing.JScrollPane();
    editingArea = new javax.swing.JTextArea();
    buttonPanel = new javax.swing.JPanel();
    saveButton = new javax.swing.JButton();
    cancelButton = new javax.swing.JButton();

    setLayout(new java.awt.CardLayout());

    readOnlyContainer.setName("read"); // NOI18N

    displayArea.setColumns(20);
    displayArea.setEditable(false);
    displayArea.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
    displayArea.setRows(5);
    displayArea.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        displayAreaMouseClicked(evt);
      }
    });
    readOnlyPanel.setViewportView(displayArea);

    javax.swing.GroupLayout readOnlyContainerLayout = new javax.swing.GroupLayout(readOnlyContainer);
    readOnlyContainer.setLayout(readOnlyContainerLayout);
    readOnlyContainerLayout.setHorizontalGroup(
      readOnlyContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 400, Short.MAX_VALUE)
      .addGroup(readOnlyContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(readOnlyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE))
    );
    readOnlyContainerLayout.setVerticalGroup(
      readOnlyContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 327, Short.MAX_VALUE)
      .addGroup(readOnlyContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(readOnlyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE))
    );

    add(readOnlyContainer, "readOnlyCard");

    editContainer.setName("write"); // NOI18N

    editingArea.setColumns(20);
    editingArea.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
    editingArea.setRows(5);
    editPanel.setViewportView(editingArea);

    saveButton.setAction(this.saveAction);
    saveButton.setText("Save");
    buttonPanel.add(saveButton);

    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cancelButtonActionPerformed(evt);
      }
    });
    buttonPanel.add(cancelButton);

    javax.swing.GroupLayout editContainerLayout = new javax.swing.GroupLayout(editContainer);
    editContainer.setLayout(editContainerLayout);
    editContainerLayout.setHorizontalGroup(
      editContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(buttonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
      .addGroup(editContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(editPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE))
    );
    editContainerLayout.setVerticalGroup(
      editContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(editContainerLayout.createSequentialGroup()
        .addContainerGap(291, Short.MAX_VALUE)
        .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      .addGroup(editContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editContainerLayout.createSequentialGroup()
          .addComponent(editPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)
          .addGap(41, 41, 41)))
    );

    add(editContainer, "editCard");
  }// </editor-fold>//GEN-END:initComponents

  private void displayAreaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_displayAreaMouseClicked
    if (evt.getClickCount() > 1) {
      Rectangle visibleArea = displayArea.getVisibleRect();
      editingArea.setText(displayArea.getText());
      editingArea.setCaretPosition(editingArea.viewToModel(evt.getPoint()));      
      displayEditView();
      editingArea.scrollRectToVisible(visibleArea);
      editingArea.requestFocusInWindow();
    }
  }//GEN-LAST:event_displayAreaMouseClicked

  private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    toggleView();
  }//GEN-LAST:event_cancelButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel buttonPanel;
  private javax.swing.JButton cancelButton;
  private javax.swing.JTextArea displayArea;
  private javax.swing.JPanel editContainer;
  private javax.swing.JScrollPane editPanel;
  private javax.swing.JTextArea editingArea;
  private javax.swing.JPanel readOnlyContainer;
  private javax.swing.JScrollPane readOnlyPanel;
  private javax.swing.JButton saveButton;
  // End of variables declaration//GEN-END:variables


  public static abstract class SaveAction extends AbstractAction {

    private JTextArea textArea;
    private BodyPanel bodyPanel;

    @Override
    public void actionPerformed(ActionEvent ae) {
      saveText(textArea.getText());
      bodyPanel.toggleView();
    }

    protected void setCallbacks(JTextArea textArea, BodyPanel bp) {
      this.bodyPanel = bp;
      this.textArea = textArea;
    }
    
    public abstract void saveText(String text);
  }
}