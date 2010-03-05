package com.nwalex.sponge.gui;

import javax.swing.Action;
import javax.swing.table.TableModel;

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
  public Action getStartReplAction();

  public TableModel getExchangeTableModel();
}
