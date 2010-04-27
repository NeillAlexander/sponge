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

/**
 * Called before the request is sent
 * @author alexanc
 */
public abstract class RequestPlugin extends Plugin {

  @Override
  public final LifecyclePoint getLifecyclePoint() {
    return LifecyclePoint.BEFORE_REQUEST;
  }

  @Override
  public final Object doPluginTask(String data, PluginContext context) {
    return context.getResponseBuilder().buildContinueResponse(processRequest(data, context));
  }

  /**
   * This will be called just before the soap request is sent
   * @param soapRequest
   * @param context
   * @return
   */
  public abstract String processRequest(String soapRequest, PluginContext context);
}
