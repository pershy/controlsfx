package org.controlsfx.control.docking;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Tab;

/**
 * A DockingContainer can be placed inside a DockPane in one of its five sides
 * or in the centre area. The two most common DockingContainers are TabPane
 * and DockPane.
 */
public interface DockingContainer {
    
    public Node getNode();
    
    public ObservableList<Tab> getTabs();
    
}
