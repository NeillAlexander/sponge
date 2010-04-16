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

import com.nwalex.sponge.plugin.Plugin;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.apache.log4j.Logger;

/**
 * Responsible for finding all plugins
 * @author alexanc
 */
public class PluginManager {

  private static final Logger log = Logger.getLogger(PluginManager.class);

  private List<LoadedPlugin> allPlugins = new ArrayList<LoadedPlugin>();
  private final PluginController controller;

  public PluginManager(PluginController controller) {
    this.controller = controller;
  }

  public PluginManager init() {
    findAllPlugins();
    return this;
  }

  private void findAllPlugins() {
    File pluginDir = new File(System.getProperty("sponge.home"), "plugins");
    log.info("Searching for plugins in: " + pluginDir.getAbsolutePath());

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

  private LoadedPlugin loadPlugin(String pluginId, Properties pluginProperties, URLClassLoader pluginClassLoader) {

    log.info("Trying to load plugin: " + pluginId);
    LoadedPlugin loadedPlugin = null;

    boolean enabled = Boolean.valueOf(pluginProperties.getProperty(pluginId + ".enabled", "false"));
    String name = pluginProperties.getProperty(pluginId + ".name", null);
    String pluginClassName = pluginProperties.getProperty(pluginId);

    try {
      Class pluginClass = pluginClassLoader.loadClass(pluginClassName);
      Plugin plugin = (Plugin) pluginClass.newInstance();

      loadedPlugin = new LoadedPlugin(plugin, name, enabled);

    } catch (Exception ex) {
      log.error("Failed to load plugin class: " + pluginClassName, ex);
    }

    if (loadedPlugin != null) {
      log.info("New plugin [" + pluginId + "]: " + loadedPlugin);
      if (loadedPlugin.isEnabled()) {
        controller.pluginEnabled(loadedPlugin.getPlugin());
      }
    }
    
    return loadedPlugin;
  }

  private void findPluginsInJarFile(File file) {
    try {
      URL jarFileUrl = file.toURI().toURL();
      log.info("Looking for plugins in: " + jarFileUrl);

      URLClassLoader pluginClassLoader = new URLClassLoader(new URL[] {jarFileUrl},
              getClass().getClassLoader());

      Properties pluginProperties = new Properties();
      pluginProperties.load(pluginClassLoader.getResourceAsStream("plugin.properties"));

      for (String pluginProperty : pluginProperties.stringPropertyNames()) {
        if (pluginProperty.endsWith(".plugin")) {
          LoadedPlugin newPlugin = loadPlugin(pluginProperty, pluginProperties, pluginClassLoader);
          if (newPlugin != null) {
            allPlugins.add(newPlugin);
          }
        }
      }

    } catch (IOException ex) {
      log.warn("Failed to find plugins in " + file.getAbsolutePath(), ex);
    }
  }

  /**
   * @return the allPlugins
   */
  public List<LoadedPlugin> getAllLoadedPlugins() {
    return allPlugins;
  }
}
