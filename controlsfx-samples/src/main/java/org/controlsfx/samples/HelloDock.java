package org.controlsfx.samples;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.docking.Dock;
import org.controlsfx.control.docking.model.DockTree;
import org.controlsfx.control.docking.model.DockTreeItem;

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
        DockTree tree = buildDockTree();
        Dock dock = new Dock(tree);

        StackPane root = new StackPane();
        root.getChildren().add(dock);
        
        return root;
    }
    
    private DockTree buildDockTree() {
        DockTree tree = new DockTree();
        
        // --- center
        DockTreeItem center = new DockTreeItem();
        center.getChildren().add(new DockTreeItem("CENTER", new Label("Center")));
        tree.setCenter(center);

        // --- left
        DockTreeItem left = new DockTreeItem();

        DockTreeItem left1 = new DockTreeItem("LEFT1");
        DockTreeItem left2 = new DockTreeItem("LEFT2");
        Button collapsebtn1 = new Button("Left 1");
        collapsebtn1.setOnAction(event -> left1.setDockMode(DockTreeItem.DockMode.COLLAPSED));
        Button collapsebtn2 = new Button("Left 2");
        collapsebtn2.setOnAction(event -> left.setDockMode(DockTreeItem.DockMode.COLLAPSED));
        left1.setContent(collapsebtn1);
        left2.setContent(collapsebtn2);
        
        left.getChildren().addAll(left1, left2);
        tree.setLeft(left);
        
        // --- bottom
        DockTreeItem bottom = new DockTreeItem();
        bottom.getChildren().add(new DockTreeItem("BOTTOM", new Label("Bottom")));
        tree.setBottom(bottom);
        
        // --- right
        DockTreeItem right = new DockTreeItem();
        right.getChildren().add(new DockTreeItem("RIGHT", new Label("Right")));
        tree.setRight(right);
        
        return tree;
    }
    
    @Override public String getSampleDescription() {
        return "";
    }

    @Override public Node getControlPanel() {
        return null;
    }

}
