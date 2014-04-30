package org.controlsfx.control.docking;

import java.util.List;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 */
public class DockPane extends Region implements DockingContainer {
    
    private static final boolean DEBUG = true;
    
    private final BorderPane borderPane;
    private Rectangle topDragRect, bottomDragRect, leftDragRect, rightDragRect;
    
    private static final boolean MANAGED = true;
    
    /**
     * 
     */
    public DockPane(final Dock dock) {
        setDock(dock);
        
//        setStyle("-fx-background-color: red");
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
        this.borderPane = new BorderPane();
        // Add Listeners for drag rectangle
        borderPane.leftProperty().addListener((Observable o) -> {
            leftDragRect = getDragRectangle(Side.LEFT, borderPane.getLeft());
        });
        
        borderPane.rightProperty().addListener((Observable o) -> {
            rightDragRect = getDragRectangle(Side.RIGHT, borderPane.getRight());
        });
        
        borderPane.topProperty().addListener((Observable o) -> {
            topDragRect = getDragRectangle(Side.TOP, borderPane.getTop());
        });
        
        borderPane.bottomProperty().addListener((Observable o) -> {
            bottomDragRect = getDragRectangle(Side.BOTTOM, borderPane.getBottom());
        });
//        borderPane.setStyle("-fx-background-color: orange");
        getChildren().add(borderPane);
    }
    
    @Override public Node getNode() {
        return this;
    }
    
    Tab tab;
    ObservableList<Tab> tabs;
    @Override public ObservableList<Tab> getTabs() {
        if (tab == null) {
            tab = new Tab();
            tab.setContent(getNode());
            tabs = FXCollections.observableArrayList(tab);
        }
        return tabs;
    }
    
    
    // --- Dock
    // TODO make read-only property
    public final ObjectProperty<Dock> dockProperty() {
        if (dock == null) {
            dock = new SimpleObjectProperty<Dock>(this, "dock");
        }
        return dock;
    }
    private ObjectProperty<Dock> dock;
    private final void setDock(Dock value) { dockProperty().set(value); }
    public final Dock getDock() { return dock == null ? null : dock.get(); }
    
    

    // --- Center
    public final ObjectProperty<DockingContainer> centerProperty() {
        if (center == null) {
            center = new SimpleObjectProperty<DockingContainer>(this, "center") {
                @Override protected void invalidated() {
                    super.invalidated();
                    
                    Node n = get().getNode();
                    n.setManaged(MANAGED);
                    borderPane.setCenter(n);
                }
            };
        }
        return center;
    }
    private ObjectProperty<DockingContainer> center;
    public final void setCenter(DockingContainer value) { centerProperty().set(value); }
    public final DockingContainer getCenter() { return center == null ? null : center.get(); }
    
    
    // --- Top
    public final ObjectProperty<DockingContainer> topProperty() {
        if (top == null) {
            top = new SimpleObjectProperty<DockingContainer>(this, "top") {
                @Override protected void invalidated() {
                    super.invalidated();
                    
                    Node n = get().getNode();
                    n.setManaged(MANAGED);
                    borderPane.setTop(n);
                }
            };
        }
        return top;
    }
    private ObjectProperty<DockingContainer> top;
    public final void setTop(DockingContainer value) { topProperty().set(value); }
    public final DockingContainer getTop() { return top == null ? null : top.get(); }
    
    
    // --- Bottom
    public final ObjectProperty<DockingContainer> bottomProperty() {
        if (bottom == null) {
            bottom = new SimpleObjectProperty<DockingContainer>(this, "bottom") {
                @Override protected void invalidated() {
                    super.invalidated();
                    
                    Node n = get().getNode();
                    n.setManaged(MANAGED);
                    borderPane.setBottom(n);
                }
            };
        }
        return bottom;
    }
    private ObjectProperty<DockingContainer> bottom;
    public final void setBottom(DockingContainer value) { bottomProperty().set(value); }
    public final DockingContainer getBottom() { return bottom == null ? null : bottom.get(); }
    
    
    // --- Left
    public final ObjectProperty<DockingContainer> leftProperty() {
        if (left == null) {
            left = new SimpleObjectProperty<DockingContainer>(this, "left") {
                @Override protected void invalidated() {
                    super.invalidated();
                    
                    Node n = get().getNode();
                    n.setManaged(MANAGED);
                    borderPane.setLeft(n);
                }
            };
        }
        return left;
    }
    private ObjectProperty<DockingContainer> left;
    public final void setLeft(DockingContainer value) { leftProperty().set(value); }
    public final DockingContainer getLeft() { return left == null ? null : left.get(); }
    
    
    // --- Right
    public final ObjectProperty<DockingContainer> rightProperty() {
        if (right == null) {
            right = new SimpleObjectProperty<DockingContainer>(this, "right") {
                @Override protected void invalidated() {
                    super.invalidated();
                    
                    Node n = get().getNode();
                    n.setManaged(MANAGED);
                    borderPane.setRight(n);
                }
            };
        }
        return right;
    }
    private ObjectProperty<DockingContainer> right;
    public final void setRight(DockingContainer value) { rightProperty().set(value); }
    public final DockingContainer getRight() { return right == null ? null : right.get(); }
    
    
    // --- Top Collapsed
    private BooleanProperty topCollapsed = new SimpleBooleanProperty(this, "topCollapsed", false) {
        protected void invalidated() {
            if (isTopCollapsed()) {
                collapse(Side.TOP);
            } else {
                expand(Side.TOP);
            }
        }
    };
    public final BooleanProperty topCollapsedProperty() { return topCollapsed; }
    public final void setTopCollapsed(Boolean value) { topCollapsedProperty().set(value); }
    public final boolean isTopCollapsed() { return topCollapsedProperty().get(); }
    
    
    // --- Bottom Collapsed
    private BooleanProperty bottomCollapsed = new SimpleBooleanProperty(this, "bottomCollapsed", false) {
        protected void invalidated() {
            if (isBottomCollapsed()) {
                collapse(Side.BOTTOM);
            } else {
                expand(Side.BOTTOM);
            }
        }
    };
    public final BooleanProperty bottomCollapsedProperty() { return bottomCollapsed; }
    public final void setBottomCollapsed(Boolean value) { bottomCollapsedProperty().set(value); }
    public final boolean isBottomCollapsed() { return bottomCollapsedProperty().get(); }
    
    
    // --- Left Collapsed
    private BooleanProperty leftCollapsed = new SimpleBooleanProperty(this, "leftCollapsed", false) {
        protected void invalidated() {
            if (isLeftCollapsed()) {
                collapse(Side.LEFT);
            } else {
                expand(Side.LEFT);
            }
        }
    };
    public final BooleanProperty leftCollapsedProperty() { return leftCollapsed; }
    public final void setLeftCollapsed(Boolean value) { leftCollapsedProperty().set(value); }
    public final boolean isLeftCollapsed() { return leftCollapsedProperty().get(); }
    
    
    // --- Right Collapsed
    private BooleanProperty rightCollapsed = new SimpleBooleanProperty(this, "rightCollapsed", false) {
        protected void invalidated() {
            if (isRightCollapsed()) {
                collapse(Side.RIGHT);
            } else {
                expand(Side.RIGHT);
            }
        }
    };
    public final BooleanProperty rightCollapsedProperty() { return rightCollapsed; }
    public final void setRightCollapsed(Boolean value) { rightCollapsedProperty().set(value); }
    public final boolean isRightCollapsed() { return rightCollapsedProperty().get(); }
    
    
    // obviously doesn't return the center - that comes via getCenter()
    public DockingContainer getDockingContainer(Side side) {
        switch (side) {
            case LEFT:   return getLeft();
            case RIGHT:  return getRight();
            case TOP:    return getTop();
            case BOTTOM: return getBottom();
        }
        return null;
    }
    
    
    
    @Override protected void layoutChildren() {
        borderPane.resizeRelocate(0, 0, getWidth(), getHeight());
    }
    
    /**
     * Creates a drag rectangle with appropriate bindings and adds it to children 
     * list
     * @param side The side to which the node belongs to
     * @param node The node which this drag rectangle manages
     * @return 
     */
    private Rectangle getDragRectangle(Side side, Node node) {
        final Rectangle dragRect = new Rectangle();
        if (node == null) {
            dragRect.setVisible(false);
            return dragRect;
        }
        dragRect.setFill(DEBUG ? Color.BLUE : Color.TRANSPARENT);
        final double WIDTH = 3.0;
        final double HALF_WIDTH = WIDTH / 2.0;
        Cursor cursor = Cursor.DEFAULT;
        ObservableValue<Number> x;
        ObservableValue<Number> y;
        ObservableValue<Number> width;
        ObservableValue<Number> height;
        switch (side) {
            case LEFT:
                x = Bindings.createDoubleBinding(() -> {
                    return node.getLayoutX() + node.getLayoutBounds().getWidth() - HALF_WIDTH;
                }, node.layoutXProperty(), node.layoutBoundsProperty());
                y = node.layoutYProperty();
                width = new SimpleDoubleProperty(WIDTH);
                height = Bindings.createDoubleBinding(() -> {
                    return node.getLayoutBounds().getHeight();
                }, node.layoutBoundsProperty());
                cursor  = Cursor.H_RESIZE;
                break;
            case RIGHT:
                x = Bindings.createDoubleBinding(() -> {
                    return node.getLayoutX() - HALF_WIDTH;
                }, node.layoutBoundsProperty());
                y = node.layoutYProperty();
                width = new SimpleDoubleProperty(WIDTH);
                height = Bindings.createDoubleBinding(() -> {
                    return node.getLayoutBounds().getHeight();
                }, node.layoutBoundsProperty());
                cursor = Cursor.H_RESIZE;
                break;
            case TOP:
                x = node.layoutXProperty();
                y = Bindings.createDoubleBinding(() -> {
                    return node.getLayoutY() + node.getLayoutBounds().getHeight() - HALF_WIDTH;
                }, node.layoutYProperty(), node.layoutBoundsProperty());
                width = Bindings.createDoubleBinding(() -> {
                    return node.getLayoutBounds().getWidth();
                }, node.layoutBoundsProperty());
                height = new SimpleDoubleProperty(WIDTH);
                cursor = Cursor.V_RESIZE;
                break;
            case BOTTOM:
                x = node.layoutXProperty();
                y = Bindings.createDoubleBinding(() -> {
                    return node.getLayoutY() - HALF_WIDTH;
                }, node.layoutBoundsProperty());
                width = Bindings.createDoubleBinding(() -> {
                    return node.getLayoutBounds().getWidth();
                }, node.layoutBoundsProperty());
                height = new SimpleDoubleProperty(WIDTH);
                cursor = Cursor.V_RESIZE;
                break;
            default:
                x = new SimpleDoubleProperty(0);
                y = new SimpleDoubleProperty(0);
                width = new SimpleDoubleProperty(0);
                height = new SimpleDoubleProperty(0);
                break;
        }
        dragRect.layoutXProperty().bind(x);
        dragRect.layoutYProperty().bind(y);
        dragRect.widthProperty().bind(width);
        dragRect.heightProperty().bind(height);
        dragRect.setCursor(cursor);
        
        dragRect.setOnMouseDragged((MouseEvent event) -> {
            Bounds bounds = node.getLayoutBounds();
            final double nodeWidth = bounds.getWidth();
            final double nodeHeight = bounds.getHeight();
            double newWidth, newHeight;
            switch (side) {
                case LEFT:
                    newWidth = nodeWidth + event.getX();
                    newHeight = nodeHeight;
                    break;

                case RIGHT:
                    newWidth = nodeWidth - event.getX();
                    newHeight = nodeHeight;
                    break;

                case TOP:
                    newWidth = nodeWidth;
                    newHeight = nodeHeight + event.getY();
                    break;

                case BOTTOM:
                    newWidth = nodeWidth;
                    newHeight = nodeHeight - event.getY();
                    break;

                default:
                    newWidth = newHeight = 0;
            }

            if (node instanceof Region) {
                ((Region) node).setPrefSize(newWidth, newHeight);
            } else {
                node.resize(newWidth, newHeight);
            }
        });
        getChildren().add(dragRect);
        return dragRect;
    }
    
    void expand(final Side side) {
        if (side == null || ! isExpanded(side)) return;
        
        // add the element to the given side, and, if necessary, remove from
        // the dock side panel
        switch (side) {
            case LEFT:   borderPane.setLeft(getLeft().getNode()); 
                         updateDockSideTabs(Side.LEFT, getLeft().getTabs(), false);
                         break;
            case RIGHT:  borderPane.setRight(getRight().getNode());
                         updateDockSideTabs(Side.RIGHT, getRight().getTabs(), false);
                         break;
            case TOP:    borderPane.setTop(getTop().getNode());
                         updateDockSideTabs(Side.TOP, getTop().getTabs(), false);
                         break;
            case BOTTOM: borderPane.setBottom(getBottom().getNode());
                         updateDockSideTabs(Side.BOTTOM, getBottom().getTabs(), false);
                         break;
        }
        
        requestLayout();
    }
    
    private void collapse(Side side) {
        if (side == null) return;
        
        // remove the element from the given side
        switch (side) {
            case LEFT:   borderPane.setLeft(null);
                         updateDockSideTabs(Side.LEFT, getLeft().getTabs(), true);
                         break;
            case RIGHT:  borderPane.setRight(null); 
                         updateDockSideTabs(Side.RIGHT, getRight().getTabs(), true); 
                         break;
            case TOP:    borderPane.setTop(null);
                         updateDockSideTabs(Side.TOP, getTop().getTabs(), true);
                         break;
            case BOTTOM: borderPane.setBottom(null); 
                         updateDockSideTabs(Side.BOTTOM, getBottom().getTabs(), true);
                         break;
        }
        
        // add the element to the side panel of the dock
        
        requestLayout();
    }
    
    private void updateDockSideTabs(final Side side, final List<Tab> tabs, final boolean add) {
        if (tabs == null || tabs.isEmpty()) return;
        Dock dock = getDock();
        
        if (add) {
            switch (side) {
                case LEFT:   dock.collapseDockingContainer(Side.LEFT, this); break;
                case RIGHT:  dock.collapseDockingContainer(Side.RIGHT, this); break;
                case TOP:    dock.collapseDockingContainer(Side.TOP, this); break;
                case BOTTOM: dock.collapseDockingContainer(Side.BOTTOM, this); break;
            }
        } else {
//            switch (side) {
//                case LEFT:   dock.restoreDockingContainer(Side.LEFT, this); break;
//                case RIGHT:  dock.restoreDockingContainer(Side.RIGHT, this); break;
//                case TOP:    dock.restoreDockingContainer(Side.TOP, this); break;
//                case BOTTOM: dock.restoreDockingContainer(Side.BOTTOM, this); break;
//            }
        }
    }
    
    private boolean isExpanded(Side side) {
        switch (side) {
            case LEFT:   return getLeft() != null;
            case RIGHT:  return getRight() != null;
            case TOP:    return getTop() != null;
            case BOTTOM: return getBottom() != null;
        }
        return false;
    }
}
