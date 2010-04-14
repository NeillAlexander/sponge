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
import com.nwalex.sponge.plugin.RequestPlugin;
import org.apache.log4j.Logger;

/**
 *
 * @author alexanc
 */
public class DemoRequestPlugin extends RequestPlugin {

  private static final Logger log = Logger.getLogger(DemoRequestPlugin.class);

  public Object preProcessRequest(String soapRequest, PluginContext context) {
    log.info("preProcessRequest() called for request: \n" + soapRequest);
    return context.getResponseBuilder().buildContinueResponse(soapRequest);
  }

  public String getName() {
    return "Demo";
  }

  public void enabled(PluginContext context) {
    log.info("enabled() called");
  }

  public void disabled(PluginContext context) {
    log.info("disabled() called");
  }
}
