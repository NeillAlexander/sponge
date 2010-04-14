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
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
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
    File pluginDir = new File(System.getProperty("sponge.home"), "plugins");
    System.out.println("ready to find all plugins in: " + pluginDir.getAbsolutePath());

    if (!pluginDir.exists()) {
      throw new RuntimeException("Plugin directory does not exist: " + pluginDir.getAbsolutePath());
    }

    File[] jarFiles = pluginDir.listFiles();
    for (int i = 0; i < jarFiles.length; i++) {
      findPluginsInJarFile(jarFiles[i]);
    }
  }

  public Action getFindPluginsAction() {
    return new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        findAllPlugins();
      }
    };
  }

  private void findPluginsInJarFile(File file) {
    
    try {
      System.out.println("Checking for plugins in: " + file.getAbsolutePath());
      JarFile jarFile = new JarFile(file);

      Enumeration<JarEntry> entries = jarFile.entries();
      
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        System.out.println("Checking entry: " + entry.getName());
      }

      // TODO: create a classloader for every plugin jar
      // TODO: use the classloader to try to load every class
      // TODO: once class loaded check that it's non-abstract, and implements Plugin
      // TODO: if so add to the list of available plugins
      // TODO: also register the name of the plugin with the classloader
      // TODO: give it a unique internal id to reference it by?
      // TODO: then when enabled use the classloader to load it (or just load once found)
      // TODO: would this be really slow for large jar files???

    } catch (IOException ex) {
      log.warn("Failed to load plugins from: " + file.getAbsolutePath(), ex);
    }
  }
}
