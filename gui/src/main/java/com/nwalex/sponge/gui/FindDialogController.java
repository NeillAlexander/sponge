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

import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;

/**
 * Simple way to search for text
 * @author alexanc
 */
public class FindDialogController {

  private final JFrame parent;
  private final List<Searchable> targets = new ArrayList<Searchable>();
  private FindDialog dialog;

  public FindDialogController(JFrame parent) {
    this.parent = parent;
  }

  public synchronized void displayDialog() {
    if (dialog == null) {
      dialog = new FindDialog(this, parent, false);
    }

    if (!dialog.isVisible()) {
      dialog.setLocationRelativeTo(parent);
      dialog.setVisible(true);
    }
  }

  public void doFind(String text) {
    System.out.println("Ready to find: " + text);
  }

  public void addTarget(Searchable target) {
    this.targets.add(target);
  }
}
