package org.controlsfx.samples;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.docking.Dock;
import org.controlsfx.control.docking.DockPane;
import org.controlsfx.control.docking.Util;

/**
 */
public class HelloDock extends ControlsFXSample {
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public String getSampleName() {
        return "Docking";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/docking/Dock.html";
    }
    
    @Override public boolean isVisible() {
        return true;
    }
    
    @Override public Node getPanel(Stage stage) {
        Dock dock = new Dock();
        
        // create default layout
        createInitialLayout(dock);
        
        StackPane root = new StackPane();
        root.getChildren().add(dock);
        
        return root;
    }
    
    @Override public String getSampleDescription() {
        return "";
    }
    
    @Override public Node getControlPanel() {
        return null;
    }
    
    private void createInitialLayout(final Dock dock) {
        final DockPane root = dock.getRootDockPane();
        
        // Center
        Button centerButton = new Button("Center Button");
        centerButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        centerButton.setId("center");
        root.setCenter(Util.wrap(centerButton));
        
        // Top
        Button topButton = new Button("Top Button");
        topButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        topButton.setId("top");
        root.setTop(Util.wrap(topButton));
        
        // Bottom
        Button bottomButton = new Button("Bottom Button");
        bottomButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        bottomButton.setId("bottom");
        root.setBottom(Util.wrap(bottomButton));
        
        // Left
        TabPane leftTabs = new TabPane();
        leftTabs.setMinWidth(200);
        Tab tab1 = new Tab("Tab 1");
        Button collapseTabPaneBtn = new Button("Collapse Left");
        collapseTabPaneBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                root.setLeftCollapsed(true);
            }
        });
        tab1.setContent(collapseTabPaneBtn);
        Tab tab2 = new Tab("Tab 2");
        tab2.setContent(new Button("Second Tab"));
        Tab tab3 = new Tab("Tab 3");
        tab3.setContent(new Button("Third Tab"));
        leftTabs.getTabs().addAll(tab1, tab2, tab3);
        root.setLeft(Util.wrap(leftTabs));
        
        // Right
        TabPane rightTabs = new TabPane();
        rightTabs.setMinWidth(200);
        Tab righTab1 = new Tab("Tab 1");
        Button collapseRightTabPaneBtn = new Button("Collapse Right");
        collapseRightTabPaneBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                root.setRightCollapsed(true);
            }
        });
        righTab1.setContent(collapseRightTabPaneBtn);
        rightTabs.getTabs().addAll(righTab1);
        root.setRight(Util.wrap(rightTabs));
    }
}
