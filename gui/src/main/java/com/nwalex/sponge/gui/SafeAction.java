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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;

/**
 * Wraps an action to catch any exceptions and display a nice dialog
 * @author neill
 */
public class SafeAction extends AbstractAction {

  private static final Logger log = Logger.getLogger(SafeAction.class);
  private final Frame parent;
  private final Action targetAction;

  public SafeAction(String name, Action targetAction, Frame parent) {
    super(name);
    this.targetAction = targetAction;
    this.parent = parent;
    setEnabled(targetAction.isEnabled());

    targetAction.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("enabled".equals(evt.getPropertyName())) {
          setEnabled((Boolean) evt.getNewValue());
        }
      }
    });
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    try {
      targetAction.actionPerformed(e);
    } catch (Exception ex) {
      log.error("An unexpected exception occured: " + ex.getMessage(), ex);
      JOptionPane.showMessageDialog(parent, ex.getClass().getName() + ": " + ex.getMessage(),
              "Exception", JOptionPane.ERROR_MESSAGE);
    }
  }
}
