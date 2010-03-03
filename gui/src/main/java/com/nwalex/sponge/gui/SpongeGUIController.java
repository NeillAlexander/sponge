package com.nwalex.sponge.gui;

/**
 * This is the thin layer that the GUI will call. Can implement this from
 * Clojure, thereby providing the hook into the functionality
 * @author neill
 */
public interface SpongeGUIController {
    public void startServer(int port, String target);
    
}
