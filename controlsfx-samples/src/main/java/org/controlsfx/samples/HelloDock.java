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
        Button collapseCenter = new Button("Collapse Center");
        DockTreeItem center = new DockTreeItem("CENTER", collapseCenter);
        collapseCenter.setOnAction(event -> center.setDockMode(DockTreeItem.DockMode.COLLAPSED));
        tree.setCenter(center);

        // --- left
        DockTreeItem left = new DockTreeItem();
        
        DockTreeItem leftTop = new DockTreeItem();
        DockTreeItem leftTop1 = new DockTreeItem("Collapse this");
        DockTreeItem leftTop2 = new DockTreeItem("Collapse LEFT");
        Button collapsebtn1 = new Button("Left 11");
        collapsebtn1.setOnAction(event -> leftTop1.setDockMode(DockTreeItem.DockMode.COLLAPSED));
        Button collapsebtn2 = new Button("Left 12");
        collapsebtn2.setOnAction(event -> left.setDockMode(DockTreeItem.DockMode.COLLAPSED));
        leftTop1.setContent(collapsebtn1);
        leftTop2.setContent(collapsebtn2);
        leftTop.getChildren().addAll(leftTop1, leftTop2);
        
        DockTreeItem leftBottom = new DockTreeItem();
        DockTreeItem leftBottom1 = new DockTreeItem("LEFT21");
        leftBottom1.setContent(new Label("Left 2"));
        
        leftBottom.getChildren().add(leftBottom1);
        
        left.getChildren().addAll(leftTop, leftBottom);
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
