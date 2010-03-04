package com.nwalex.sponge.gui;

/**
 *
 * @author neill
 */
public interface ConfigurationDialogController {
  public void setConfiguration(int port, String target);
  public int getCurrentPort();
  public String getCurrentTarget();
}
