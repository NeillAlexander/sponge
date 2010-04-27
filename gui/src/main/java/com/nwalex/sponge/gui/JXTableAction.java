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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.JXTable;

/**
 *
 * @author alexanc
 */
public abstract class JXTableAction extends AbstractAction {

  private final JXTable table;

  public JXTableAction(JXTable table, final boolean multipleSelectionAllowed) {
    this.table = table;

    table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

      @Override
      public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
          return;
        }

        ListSelectionModel rowSM = (ListSelectionModel) e.getSource();
        int numRows = rowSM.getMaxSelectionIndex() - rowSM.getMinSelectionIndex() + 1;
        setEnabled(!rowSM.isSelectionEmpty()
                && (multipleSelectionAllowed || (!multipleSelectionAllowed && numRows == 1)));
      }
    });
  }

  @Override
  // TODO: change this - need to have separate method for multi-row (see ticket)
  public final void actionPerformed(ActionEvent e) {
    try {
      if (table.getSelectedRowCount() > 1) {
        int[] indices = table.getSelectedRows();
        int[] modelIndices = new int[indices.length];

        for (int i = 0; i < indices.length; i++) {
          modelIndices[i] = table.convertRowIndexToModel(indices[i]);
        }

        multiRowActionPerformed(modelIndices);

      } else if (table.getSelectedRowCount() == 1) {
        singleRowActionPerformed(table.convertRowIndexToModel(table.getSelectedRow()));
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(table, ex.getClass().getName() + ": " + ex.getMessage(),
              "Exception", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Designed to be overridden by child class
   * @param row
   */
  protected abstract void multiRowActionPerformed(int[] rowIndices);

  /**
   * Designed to be overridden by child class
   * @param row
   */
  protected abstract void singleRowActionPerformed(int index);
}
