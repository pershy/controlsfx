package org.controlsfx.control.docking;

import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

/**
 */
public class Dock extends Region {
    
    private static final String TAB_KEY = "TAB";
    private static final String DOCK_PANE_KEY = "DOCK_PANE";
    private static final String SIDE_KEY = "SIDE";
    
    // The Dock has a single root DockPane (of which additional DockPanes can
    // of course be added as children).
    private DockPane rootDockPane;
    
    private BorderPane borderPane;
    
    // side panels
    private final ToolBar rightSidePanel;
    private final ToolBar leftSidePanel;
    
//    private ListChangeListener<Tab> collapsedTabsListener = new ListChangeListener<Tab>() {
//        @Override public void onChanged(javafx.collections.ListChangeListener.Change<? extends Tab> c) {
//            requestLayout();
//        }
//    };

    /**
     * 
     */
    public Dock() {
        rootDockPane = new DockPane(this);
        borderPane = new BorderPane(rootDockPane);
        
        rightSidePanel = new ToolBar();
        leftSidePanel = new ToolBar();
        rightSidePanel.setOrientation(Orientation.VERTICAL);
        leftSidePanel.setOrientation(Orientation.VERTICAL);
        
        getChildren().add(borderPane);
        
//        setStyle("-fx-background-color: green;");
//        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }
    
    public DockPane getRootDockPane() {
        return rootDockPane;
    }

    public void collapseDockingContainer(final Side side, final DockPane dockPane) {
        if (dockPane == null || dockPane.getTabs().isEmpty()) return;
        
        // if the side == Side.LEFT, collapse into the left,
        // else collapse into the RIGHT side panel.
        
        // get the tabs on the given side of the given dockPane - this is the
        // dockingContainer we want to collapse
        DockingContainer dc = dockPane.getDockingContainer(side);
        List<Tab> tabs = dc.getTabs();
        
        // get a list of the existing buttons on this side
        List<Button> buttonsOnSide = getButtonsOnSide(side);
        for (Tab tab : tabs) {
            Button btn = createSidePanelButton(side, dockPane, tab);
            buttonsOnSide.add(btn);
        }
        
        if (! leftSidePanel.getItems().isEmpty()) {
            borderPane.setLeft(leftSidePanel);
        }
        
        if (! rightSidePanel.getItems().isEmpty()) {
            borderPane.setRight(rightSidePanel);
        }
    }
    
    /**
     * Restores the tabs to the given side.
     */
    public void restoreDockingContainer(final Side side, final DockPane dockPane) {
        if (dockPane == null || dockPane.getTabs().isEmpty()) return;
        
        // if the side == Side.LEFT, collapse into the left,
        // else collapse into the RIGHT side panel.
        
        // get the tabs on the given side of the given dockPane - this is the
        // dockingContainer we want to collapse
        DockingContainer dc = dockPane.getDockingContainer(side);
        List<Tab> tabs = dc.getTabs();
        
        // get a list of the existing buttons on this side
        List<Button> buttonsOnSide = getButtonsOnSide(side);
        buttonsOnSide.clear();
        
        dockPane.expand(side);
        
        borderPane.setLeft(leftSidePanel.getItems().isEmpty() ? null : leftSidePanel);
        borderPane.setRight(rightSidePanel.getItems().isEmpty() ? null : rightSidePanel);
    }
    
    private Button createSidePanelButton(final Side side, final DockPane dockPane, final Tab tab) {
        final Button btn = new Button(tab.getText(), tab.getGraphic());
        
        btn.getProperties().put(TAB_KEY, tab);
        btn.getProperties().put(DOCK_PANE_KEY, dockPane);
        btn.getProperties().put(SIDE_KEY, side);
        
        // TODO on click a floating tab pane should appear that contains all
        // tabs in the side panel
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                // FIXME for now we just restore all tabs on that side
                
//                btn.getProperties().put(TAB_KEY, tab);
                final DockPane _dockPane = (DockPane) btn.getProperties().get(DOCK_PANE_KEY);
                final Side _side = (Side) btn.getProperties().get(SIDE_KEY);
                
                restoreDockingContainer(_side, _dockPane);
                
                
//                List<Button> buttonsOnSide = getButtonsOnSide(side);
//                        
//                List<Tab> tabsToRestore = new ArrayList<>();
//                for (Button _btn : buttonsOnSide) {
//                    Tab tab = (Tab) _btn.getUserData();
//                    tabsToRestore.add(tab);
//                }
//                restoreTabs(side, tabsToRestore);
            }
        });
        
        return btn;
    }
    
    
    @Override protected void layoutChildren() {
        // TODO put in the tab bars around the outside edge of the dock
        
        // fill remainder of space with the root DockPane
        borderPane.resizeRelocate(0, 0, getWidth(), getHeight());
    }
    
    private List<Button> getButtonsOnSide(Side side) {
        @SuppressWarnings("unchecked")
        List<Button> buttonsOnSide = (List<Button>) (Object) (side == Side.LEFT ?
                leftSidePanel.getItems() : rightSidePanel.getItems());
        
        return buttonsOnSide;
    }
}
