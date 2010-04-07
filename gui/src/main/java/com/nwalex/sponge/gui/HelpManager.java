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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * Simple class to provide support for help files
 * @author alexanc
 */
public class HelpManager {

  private final Map<String, HelpFrame> helpFrames = new HashMap<String, HelpFrame>();
  private final SpongeGUI parent;

  public HelpManager(SpongeGUI parent) {
    this.parent = parent;
  }

  public synchronized void displayHelp(String resourceName) throws IOException {
    if (!helpFrames.containsKey(resourceName)) {
      helpFrames.put(resourceName, new HelpFrame(loadHtml(resourceName)));
    }

    helpFrames.get(resourceName).setVisible(true);
  }

  public Action makeMenuAction(final String resourceName) {
    return new SafeAction(new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          displayHelp(resourceName);
        } catch (Exception ex) {
          ex.printStackTrace();
          throw new RuntimeException("Unable to display help: " + ex.getMessage());
        }
      }      
    }, parent);
  }

  private String loadHtml(String resourceName) throws IOException {
    InputStream in = this.getClass().getClassLoader().getResourceAsStream(resourceName);
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

    StringBuilder html = new StringBuilder();
    String line = null;

    while ((line = reader.readLine()) != null) {
      html.append(line);
    }

    return html.toString();
  }
}
