package org.controlsfx.control.docking;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 */
public class Util {

    public static DockingContainer wrap(final TabPane tabPane) {
        return new DockingContainer() {
            @Override public Node getNode() {
                return tabPane;
            }
            
            @Override public ObservableList<Tab> getTabs() {
                return tabPane.getTabs();
            }
        };
    }
    
    public static DockingContainer wrap(final Node node) {
        return new DockingContainer() {
            Tab tab;
            ObservableList<Tab> tabs;
            
            @Override public Node getNode() {
                return node;
            }
            
            @Override public ObservableList<Tab> getTabs() {
                if (tab == null) {
                    tab = new Tab();
                    tab.setContent(getNode());
                    tabs = FXCollections.observableArrayList(tab);
                }
                return tabs;
            }
        };
    }
}
