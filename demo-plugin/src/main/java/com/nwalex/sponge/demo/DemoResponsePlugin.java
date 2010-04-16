/**
* Copyright (c) Neill Alexander. All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software
*/
package com.nwalex.sponge.demo;

import com.nwalex.sponge.plugin.PluginContext;
import com.nwalex.sponge.plugin.ResponsePlugin;
import org.apache.log4j.Logger;

/**
 *
 * @author alexanc
 */
public class DemoResponsePlugin extends ResponsePlugin {

  private static final Logger log = Logger.getLogger(DemoResponsePlugin.class);

  @Override
  protected String processResponse(String response, PluginContext context) {
    log.info("processResponse() called");
    return response;
  }

  @Override
  public void onEnabled(PluginContext context) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void onDisabled(PluginContext context) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
