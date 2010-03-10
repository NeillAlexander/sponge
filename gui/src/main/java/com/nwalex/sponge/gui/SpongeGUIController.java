package com.nwalex.sponge.gui;

import javax.swing.Action;
import javax.swing.table.TableModel;

/**
 * This is the thin layer that the GUI will call. Can implement this from
 * Clojure, thereby providing the hook into the functionality
 * @author neill
 */
public interface SpongeGUIController {

  public static final String FORWARD_ALL = "forwardAll";
  public static final String REPLAY_OR_FORWARD = "replayOrForward";
  public static final String REPLAY_OR_FAIL = "replayOrFail";
  public static final String REPLAY_OR_PROMPT = "replayOrPrompt";

  public Action getStartServerAction();
  public Action getStopServerAction();
  public Action getConfigureAction();
  public Action getExitAction();
  public Action getStartReplAction();
  public Action getClearAllAction();
  public Action getLabelExchangeAction();
  public Action getDeleteLabelAction();
  public Action getLoadAction();
  public Action getSaveAction();
  public Action getSaveAsAction();
  public Action getSetDefaultResponseAction();
  public Action getDeleteRowAction();

  public String getMode();
  public void setMode(String mode);

  public TableModel getExchangeTableModel();

  public String getRequestDataForRow(int row);
  public String getResponseDataForRow(int row);

  public void setSelectedRow(int row);
}
