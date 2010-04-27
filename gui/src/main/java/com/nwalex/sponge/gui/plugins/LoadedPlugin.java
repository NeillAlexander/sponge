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

/**
 *
 * @author alexanc
 */
public class LoadedPlugin {

  private final Plugin plugin;
  private final String name;
  private final boolean enabled;

  public LoadedPlugin(Plugin plugin, String name, boolean enabled) {
    this.plugin = plugin;
    this.name = name;
    this.enabled = enabled;
  }

  /**
   * @return the plugin
   */
  public Plugin getPlugin() {
    return plugin;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the enabled
   */
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public String toString() {
    return super.toString() + ": [plugin = " + plugin + "] " +
            "[enabled = " + enabled + "] [name = " + name + "]";
  }
}
