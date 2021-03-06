/**
* Copyright (c) Neill Alexander. All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software
*/
package com.nwalex.sponge.plugin;

import org.apache.log4j.Logger;

/**
 * top level for all plugins
 * @author alexanc
 */
public abstract class Plugin {

  private static final Logger log = Logger.getLogger(Plugin.class);

  // lifecycle methods
  public abstract LifecyclePoint getLifecyclePoint();
  public abstract void onEnabled();
  public abstract void onDisabled();

  /**
   * this is called by the application
   * @param data
   * @param context
   * @return
   */
  public final Object execute(String data, PluginContext context) {
    Object response = doPluginTask(data, context);
    
    if (!context.isValidResponse(response)) {
      log.warn("Invalid response from plugin - returning unmodified data");
      response = context.getResponseBuilder().buildContinueResponse(data);
    }

    return response;
  }

  public abstract Object doPluginTask(String data, PluginContext context);
}
