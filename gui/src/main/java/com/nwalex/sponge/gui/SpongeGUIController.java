package com.nwalex.sponge.gui;

import javax.swing.Action;

/**
 * This is the thin layer that the GUI will call. Can implement this from
 * Clojure, thereby providing the hook into the functionality
 * @author neill
 */
public interface SpongeGUIController {
  public Action getStartServerAction();
  public Action getStopServerAction();
  public Action getConfigureAction();
  public Action getExitAction();  
}
