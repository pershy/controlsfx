package org.controlsfx.samples;

import javafx.scene.Node;
import javafx.scene.control.Button;
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
        DockTreeItem center = new DockTreeItem("CENTER");
        center.setContent(getCollapseButton(center));
        DockTreeItem centerParent = new DockTreeItem("CENTER PARENT");
        centerParent.getChildren().addAll(center);
        tree.setCenter(centerParent);

        // --- left
        DockTreeItem left = new DockTreeItem("LEFT PARENT");
        
        DockTreeItem leftTop = new DockTreeItem("LEFT TOP");
        DockTreeItem leftTop1 = new DockTreeItem("LEFT TOP 1");
        DockTreeItem leftTop2 = new DockTreeItem("LEFT TOP 2");

        leftTop1.setContent(getCollapseButton(leftTop1));
        leftTop2.setContent(getCollapseButton(leftTop2));
        leftTop.getChildren().addAll(leftTop1, leftTop2);
        
        DockTreeItem leftBottom = new DockTreeItem("LEFT BOT");
        DockTreeItem leftBottom1 = new DockTreeItem("LEFT BOT 1");
        leftBottom1.setContent(getCollapseButton(leftBottom1));
        
        leftBottom.getChildren().addAll(leftBottom1);
        
        left.getChildren().addAll(leftTop, leftBottom);
        tree.setLeft(left);

        // --- bottom
        DockTreeItem bottomParent = new DockTreeItem("BOTTOM PARENT");
        DockTreeItem bottom = new DockTreeItem("BOTTOM");
        bottom.setContent(getCollapseButton(bottom));
        bottomParent.getChildren().add(bottom);
        tree.setBottom(bottomParent);
        
        // --- right
        DockTreeItem rightParent = new DockTreeItem("RIGHT PARENT");
        DockTreeItem right = new DockTreeItem("RIGHT");
        right.setContent(getCollapseButton(right));
        rightParent.getChildren().add(right);
        tree.setRight(rightParent);
        
        return tree;
    }
    
    @Override public String getSampleDescription() {
        return "";
    }

    @Override public Node getControlPanel() {
        return null;
    }
    
    private Button getCollapseButton(DockTreeItem item) {
        Button btn = new Button("Collapse");
        btn.setOnAction(event -> item.setDockMode(DockTreeItem.DockMode.COLLAPSED));
        return btn;
    }

}
