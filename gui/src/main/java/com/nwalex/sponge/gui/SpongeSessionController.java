/**
* Copyright (c) Neill Alexander. All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software
*/

package com.nwalex.sponge.gui;

import com.nwalex.sponge.gui.plugins.PluginController;
import javax.swing.Action;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;

/**
 * This is the thin layer that the GUI will call. Can implement this from
 * Clojure, thereby providing the hook into the functionality
 * @author neill
 */
public interface SpongeSessionController {

  public static final String FORWARD_ALL = "Forward All";
  public static final String REPLAY_OR_FORWARD = "Replay or Forward";
  public static final String REPLAY_OR_FAIL = "Replay or Fail";
  public static final String REPLAY_OR_PROMPT = "Replay or Prompt";

  public PluginController getPluginController();

  // multi-row actions
  public Action getLabelExchangeAction(JXTable table);
  public Action getDeleteLabelAction(JXTable table);
  public Action getSetDefaultResponseAction(JXTable table);
  public Action getDeleteRowAction(JXTable table);
  public Action getDuplicateRowAction(JXTable table);
  public BodyPanel.SaveAction getUpdateRequestBodyAction(JXTable table);
  public BodyPanel.SaveAction getUpdateResponseBodyAction(JXTable table);
  public Action getResendRequestAction(JXTable table);

  // non row specific actions
  public Action getStartServerAction();
  public Action getStopServerAction();
  public Action getConfigureAction();
  public Action getExitAction();  
  public Action getLoadAction();
  public Action getSaveAction();
  public Action getSaveAsAction();       

  public String getMode();
  public void setMode(String mode);

  public TableModel getExchangeTableModel();

  public String getRequestDataForRow(int row);
  public String getResponseDataForRow(int row);
}
