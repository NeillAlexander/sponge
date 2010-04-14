/**
* Copyright (c) Neill Alexander. All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software
*/
package com.nwalex.sponge.gui.plugins;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.apache.log4j.Logger;

/**
 * Responsible for finding all plugins
 * @author alexanc
 */
public class PluginManager {

  private static final Logger log = Logger.getLogger(PluginManager.class);

  public PluginManager init() {
    findAllPlugins();
    return this;
  }

  private void findAllPlugins() {
    System.out.println("ready to find all plugins");
  }

  public Action getFindPluginsAction() {
    return new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        findAllPlugins();
      }
    };
  }
}
