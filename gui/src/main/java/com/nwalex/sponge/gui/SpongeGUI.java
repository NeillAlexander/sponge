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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;

/**
 *
 * @author neill
 */
public class SpongeGUI extends javax.swing.JFrame {

  private int sessionCounter = 1;
  private final SpongeController controller;
  private HelpManager helper;
  private static final Logger log = Logger.getLogger(SpongeGUI.class);

  private Map<SpongeSessionController, SpongeSessionPanel> sessionMap =
          new WeakHashMap<SpongeSessionController, SpongeSessionPanel>();
  private Map<Component, SpongeSessionController> tabToControllerMap =
          new WeakHashMap<Component, SpongeSessionController>();
  private Map<SpongeSessionController, Component> controllerToTabMap =
          new WeakHashMap<SpongeSessionController, Component>();

  /** Creates new form SpongeGUI */
  public SpongeGUI(final SpongeController controller) {
    this.controller = controller;
    this.helper = new HelpManager(this);

    initComponents();

    setExtendedState(JFrame.MAXIMIZED_BOTH);
  }

  public void initialize() {
    for (SpongeSessionController sessionController : controller.initializeWorkspace()) {
      addSession(sessionController);
    }
  }

  public void updateSelectedMode(final SpongeSessionController controller) {
    if (sessionMap.containsKey(controller)) {
      sessionMap.get(controller).updateSelectedMode(controller);
    } else {
      log.warn("updateSelectedMode called for " + controller + " but I don't have a reference to it!");
    }
  }

  private void updateTabTitle(String title, SpongeSessionController sessionController) {
    sessionTabs.setTitleAt(sessionTabs.indexOfComponent(controllerToTabMap.get(sessionController)), title);
  }

  public void setSessionInfo(SpongeSessionController sessionController, String info, String title) {
    if (sessionMap.containsKey(sessionController)) {
      sessionMap.get(sessionController).setSessionInfo(info);
      updateTabTitle(title, sessionController);
    } else {
      log.warn("setSessionInfo called for " + sessionController + " but I don't have a reference to it!");
    }
  }

  private void deleteTab(Component tab) {
    sessionTabs.remove(tab);
    controller.deleteSession(tabToControllerMap.get(tab));
    sessionMap.remove(tabToControllerMap.get(tab));
    SpongeSessionController sessionController = tabToControllerMap.remove(tab);
    controllerToTabMap.remove(sessionController);
  }

  private void deleteCurrentWorkspaceData() {
    for (int i = sessionTabs.getTabCount() - 1; i >= 0; i--) {
      deleteTab(sessionTabs.getComponentAt(i));
    }
  }

  private Action getLoadWorkspaceAction() {
    return new AbstractAction("Load Workspace") {
      @Override
      public void actionPerformed(ActionEvent e) {
        SpongeSessionController[] sessionControllers = controller.loadWorkspace();

        if (sessionControllers.length > 0) {
          deleteCurrentWorkspaceData();
        }

        for (SpongeSessionController spongeSessionController : sessionControllers) {
          addSession(spongeSessionController);
        }
      }
    };
  }

  private void addSession(SpongeSessionController sessionController) {
    sessionMap.put(sessionController, new SpongeSessionPanel(this, sessionController));
    sessionTabs.add(sessionMap.get(sessionController), "Session " + (sessionCounter++));

    Component tab = sessionTabs.getComponentAt(sessionTabs.getTabCount() - 1);
    tabToControllerMap.put(tab, sessionController);
    controllerToTabMap.put(sessionController, tab);
    
    sessionController.updateSessionInfo();
    this.validate();
  }

  private void renameTab(Component tab) {
    String newTitle = (String)JOptionPane.showInputDialog(
                        this,
                        "Name: ",
                        "Rename Session",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        "");

    if (newTitle != null) {      
      tabToControllerMap.get(tab).setName(newTitle);
      tabToControllerMap.get(tab).updateSessionInfo();
    }
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    sessionTabs = new javax.swing.JTabbedPane();
    menuBar = new javax.swing.JMenuBar();
    jMenu1 = new javax.swing.JMenu();
    loadWorkspaceMenu = new javax.swing.JMenuItem();
    saveWorkspaceAsMenu = new javax.swing.JMenuItem();
    saveWorkspaceMenu = new javax.swing.JMenuItem();
    jSeparator1 = new javax.swing.JPopupMenu.Separator();
    newSessionMenu = new javax.swing.JMenuItem();
    replMenu = new javax.swing.JMenu();
    replMenuItem = new javax.swing.JMenuItem();
    replMenuItem.setAction(controller.getStartReplAction());
    jMenu2 = new javax.swing.JMenu();
    keyboardShortcutsHelp = new javax.swing.JMenuItem();
    jMenuItem1 = new javax.swing.JMenuItem();
    aboutMenuItem = new javax.swing.JMenuItem();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

    sessionTabs.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        displayTabPopup(evt);
      }
      public void mousePressed(java.awt.event.MouseEvent evt) {
        sessionTabsMousePressed(evt);
      }
      public void mouseReleased(java.awt.event.MouseEvent evt) {
        sessionTabsMouseReleased(evt);
      }
    });
    getContentPane().add(sessionTabs, java.awt.BorderLayout.CENTER);

    jMenu1.setText("Workspace");

    loadWorkspaceMenu.setAction(getLoadWorkspaceAction());
    loadWorkspaceMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
    loadWorkspaceMenu.setText("Load Workspace...");
    jMenu1.add(loadWorkspaceMenu);

    saveWorkspaceAsMenu.setAction(controller.getSaveWorkspaceAsAction());
    saveWorkspaceAsMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
    saveWorkspaceAsMenu.setText("Save Workspace As...");
    jMenu1.add(saveWorkspaceAsMenu);

    saveWorkspaceMenu.setAction(controller.getSaveWorkspaceAction());
    saveWorkspaceMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
    saveWorkspaceMenu.setText("Save Workspace");
    jMenu1.add(saveWorkspaceMenu);
    jMenu1.add(jSeparator1);

    newSessionMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
    newSessionMenu.setText("New Session");
    newSessionMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        addSession(evt);
      }
    });
    jMenu1.add(newSessionMenu);

    menuBar.add(jMenu1);

    replMenu.setText("REPL");

    replMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
    replMenuItem.setText("Start REPL");
    replMenu.add(replMenuItem);

    menuBar.add(replMenu);

    jMenu2.setText("Help");

    keyboardShortcutsHelp.setAction(helper.makeMenuAction("manual.html"));
    keyboardShortcutsHelp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
    keyboardShortcutsHelp.setText("Manual");
    jMenu2.add(keyboardShortcutsHelp);

    jMenuItem1.setAction(helper.makeMenuAction("plugins.html"));
    jMenuItem1.setText("Writing Plugins");
    jMenu2.add(jMenuItem1);

    aboutMenuItem.setText("About");
    aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        aboutMenuItemActionPerformed(evt);
      }
    });
    jMenu2.add(aboutMenuItem);

    menuBar.add(jMenu2);

    setJMenuBar(menuBar);

    pack();
  }// </editor-fold>//GEN-END:initComponents

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
      AboutDialog ad = new AboutDialog(this, true);
      ad.setLocationRelativeTo(null);
      ad.setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void addSession(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSession
      addSession(controller.createNewSession());
    }//GEN-LAST:event_addSession

    private void displayTabPopup(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_displayTabPopup
      if (evt.isPopupTrigger()) {
        final JPopupMenu menu = new JPopupMenu();
        int index = sessionTabs.indexAtLocation(evt.getX(), evt.getY());

        if (index < 0) return;

        final Component tab =
                sessionTabs.getComponentAt(index);

        menu.add(new AbstractAction("Rename") {
          @Override
          public void actionPerformed(ActionEvent e) {
            renameTab(tab);
          }
        });

        menu.add(new AbstractAction("Close") {
          @Override
          public void actionPerformed(ActionEvent e) {
            deleteTab(tab);
          }
        });

        menu.add(new AbstractAction("Close Others") {
          @Override
          public void actionPerformed(ActionEvent e) {
            List<Component> tabsToClose = new ArrayList<Component>();
            for (int i = 0; i < sessionTabs.getTabCount(); i++) {
              Component nextTab = sessionTabs.getComponentAt(i);
              if (!nextTab.equals(tab)) {
                tabsToClose.add(nextTab);
              }
            }

            for (Component tabToClose : tabsToClose) {
              deleteTab(tabToClose);
            }
          }
        });

        menu.show(sessionTabs, evt.getX(), evt.getY());
      }
    }//GEN-LAST:event_displayTabPopup

    private void sessionTabsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sessionTabsMousePressed
      displayTabPopup(evt);
    }//GEN-LAST:event_sessionTabsMousePressed

    private void sessionTabsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sessionTabsMouseReleased
      displayTabPopup(evt);
    }//GEN-LAST:event_sessionTabsMouseReleased
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JMenuItem aboutMenuItem;
  private javax.swing.JMenu jMenu1;
  private javax.swing.JMenu jMenu2;
  private javax.swing.JMenuItem jMenuItem1;
  private javax.swing.JPopupMenu.Separator jSeparator1;
  private javax.swing.JMenuItem keyboardShortcutsHelp;
  private javax.swing.JMenuItem loadWorkspaceMenu;
  private javax.swing.JMenuBar menuBar;
  private javax.swing.JMenuItem newSessionMenu;
  private javax.swing.JMenu replMenu;
  private javax.swing.JMenuItem replMenuItem;
  private javax.swing.JMenuItem saveWorkspaceAsMenu;
  private javax.swing.JMenuItem saveWorkspaceMenu;
  private javax.swing.JTabbedPane sessionTabs;
  // End of variables declaration//GEN-END:variables
}
