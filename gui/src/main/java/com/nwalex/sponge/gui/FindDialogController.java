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
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

/**
 * Simple way to search for text
 * @author alexanc
 */
public class FindDialogController {

  private final JFrame parent;
  private final List<Searchable> targets = new ArrayList<Searchable>();
  private FindDialog dialog;
  private int hasFindNextFocusIndex = 0;
  private String lastSearchText = null;
  private boolean lastSearchCaseSensitity = false;

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

  public void initFindActionOn(final JComponent component) {
    component.getActionMap().put("FIND", new AbstractAction() {

      @Override
      public void actionPerformed(ActionEvent e) {
        displayDialog();
      }
    });

    component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK),
            "FIND");

    if (component instanceof Searchable) {
      ((Searchable) component).registeredBy(this);
      addTarget((Searchable) component);
    }
  }

  public void doFind(String text, boolean caseSensitive) {
    if (!text.equals(lastSearchText) || caseSensitive != lastSearchCaseSensitity) {
      hasFindNextFocusIndex = 0;

      for (Searchable searchable : targets) {
        searchable.clearHighlights();

        // only search if we have text
        if (text.trim().length() > 0) {
          lastSearchText = text;
          lastSearchCaseSensitity = caseSensitive;
          searchable.highlightAll(text, caseSensitive);
        }
      }
    }

    if (text.trim().length() > 0) {
      findNext();
    }
  }

  public void findNext() {
    for (int i = hasFindNextFocusIndex; i < targets.size(); i++) {
      Searchable searchable = targets.get(i);
      if (searchable.findNext()) {
        hasFindNextFocusIndex = i;
        return;
      }
    }

    // nothing found so search from the start
    for (int i = 0; i < hasFindNextFocusIndex; i++) {
      Searchable searchable = targets.get(i);
      if (searchable.findNext()) {
        hasFindNextFocusIndex = i;
        return;
      }
    }
  }

  public void addTarget(Searchable target) {
    this.targets.add(target);
  }
}
