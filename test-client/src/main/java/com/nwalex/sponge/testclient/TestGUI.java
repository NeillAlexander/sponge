/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TestGUI.java
 *
 * Created on 06-Mar-2010, 20:45:37
 */
package com.nwalex.sponge.testclient;

import com.aonaware.services.webservices.Definition;
import com.aonaware.services.webservices.DictService;
import com.aonaware.services.webservices.DictServiceSoap;
import com.aonaware.services.webservices.WordDefinition;
import javax.swing.SwingUtilities;
import javax.xml.ws.BindingProvider;

/**
 *
 * @author neill
 */
public class TestGUI extends javax.swing.JFrame {

  /** Creates new form TestGUI */
  public TestGUI() {
    initComponents();
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jScrollPane1 = new javax.swing.JScrollPane();
    responseArea = new javax.swing.JTextArea();
    jButton1 = new javax.swing.JButton();
    viaSpongeTextBox = new javax.swing.JCheckBox();
    wordField = new javax.swing.JTextField();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

    responseArea.setColumns(20);
    responseArea.setRows(5);
    jScrollPane1.setViewportView(responseArea);

    jButton1.setText("Define Word");
    jButton1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        jButton1ActionPerformed(evt);
      }
    });

    viaSpongeTextBox.setText("Via Sponge");
    viaSpongeTextBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        viaSpongeTextBoxActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
      .addGroup(layout.createSequentialGroup()
        .addGap(34, 34, 34)
        .addComponent(wordField, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jButton1)
        .addGap(18, 18, 18)
        .addComponent(viaSpongeTextBox)
        .addContainerGap(46, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 247, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
          .addComponent(wordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jButton1)
          .addComponent(viaSpongeTextBox))
        .addContainerGap())
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

    private void viaSpongeTextBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viaSpongeTextBoxActionPerformed
      // TODO add your handling code here:
    }//GEN-LAST:event_viaSpongeTextBoxActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

      new Thread(new Runnable() {

        @Override
        public void run() {
          DictService ds = new DictService();

          DictServiceSoap soapService = ds.getDictServiceSoap12();

          if (viaSpongeTextBox.isSelected()) {
            // configure sponge to point to http://services.aonaware.com
            ((javax.xml.ws.BindingProvider) soapService).getRequestContext().put(javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                    "http://localhost:8139//DictService/DictService.asmx");
          }

          WordDefinition wd = soapService.define(wordField.getText());

          final StringBuilder text = new StringBuilder("");
          for (Definition definition : wd.getDefinitions().getDefinition()) {
            text.append(definition.getWordDefinition()).append("\n");
          }

          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              responseArea.setText(text.toString());
              responseArea.setCaretPosition(0);
            }
          });
        }
      }).start();
    }//GEN-LAST:event_jButton1ActionPerformed

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {

      public void run() {
        new TestGUI().setVisible(true);
      }
    });
  }
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton jButton1;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JTextArea responseArea;
  private javax.swing.JCheckBox viaSpongeTextBox;
  private javax.swing.JTextField wordField;
  // End of variables declaration//GEN-END:variables
}
