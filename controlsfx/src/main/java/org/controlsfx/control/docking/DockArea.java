/**
 * Copyright (c) 2014, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.controlsfx.control.docking;

import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.docking.model.DockTreeItem;

/**
 * Represents a Complex DockTreeItem in view. Children of this container are
 * placed according to their type. If a child is COMPLEX it is added to the 
 * SplitPane. If a child is SIMPLE, a TabPane is created and all SIMPLE children
 * are placed into the TabPane. The TabPane in-turn will be placed into the 
 * SplitPane
 */
class DockArea extends DockingContainer {
    
    private Dock dock;
    // root view component that contains the split pane and overlay
    private StackPane root;
    private SplitPane splitPane;
    private TabPane tabPane;
    
    private boolean isTabPaneAdded = false;
    // Used when collapsing and expanding. Holds the position of the container
    private int index;
    // Overlay container to show different indications during drop
    private StackPane overlay;
    private final ObservableList<DockingContainer> children = FXCollections.observableArrayList();

    private Orientation getOppositeOrientation(Orientation orientation) {
        if (Orientation.HORIZONTAL == orientation) {
            return Orientation.VERTICAL;
        }
        if (Orientation.VERTICAL == orientation) {
            return Orientation.HORIZONTAL;
        }
        return null;
    }
    
    private final ListChangeListener<DockingContainer> childListener = (ListChangeListener.Change<? extends DockingContainer> change) -> {
        while (change.next()) {
            change.getAddedSubList().stream()
                    .forEach((container) -> {
                        if (container instanceof DockArea) {
                            ((DockArea) container).setOrientation(getOppositeOrientation(getOrientation()));
                        }
                        container.setParent(this);
                    });
        }
        updateView(getDockTreeItem());
    };
    

    // Wrapper for SplitPane's orientation
    public final ObjectProperty<Orientation> orientationProperty() {
        return splitPane.orientationProperty();
    }
    public void setOrientation(Orientation orientation) {
        orientationProperty().set(orientation);
    }
    public Orientation getOrientation() {
        return orientationProperty().get();
    }
            
    
    /**
     * Default constructor
     * @param dock Dock to which this DockArea belongs
     */
    public DockArea(Dock dock, DockTreeItem treeItem) {
        this.dock = dock;
        root = new StackPane();
        splitPane = new SplitPane();
        tabPane = new TabPane();
        overlay = new StackPane();
        overlay.setVisible(false);
        // TODO Add implemntation to show overlay. Currently I have rectangles
        // in mind as shown in eclipse's docking

        root.getChildren().addAll(splitPane, overlay);
        setDockTreeItem(treeItem);
        children.addListener(childListener);
    }

    @Override
    public final void updateView(DockTreeItem item) {
        // FIXME Add/Remove only the changed items to the view rather than
        // clearing everything and adding again
        splitPane.getItems().clear();
        tabPane.getTabs().clear();
        isTabPaneAdded = false;
        splitPane.setStyle("-fx-border-style: solid");
        
        // Iterate through children of this container. Place all DockTabs into
        // a TabPane and all other children into SplitPane
        getChildren().stream().forEach(container -> {
            if (container instanceof DockTab) {
                if (!isTabPaneAdded) {
                    splitPane.getItems().add(tabPane);
                    isTabPaneAdded = true;
                }
                tabPane.getTabs().add((Tab) container.getViewComponent());
            } else {
                splitPane.getItems().add((Node) container.getViewComponent());
            }
        });
        // Keep divider position in equal distance for now
        // TODO use weight of DockTreeItem to determine the divider position
        double size = splitPane.getItems().size();
        if (size > 0) {
            double divPos = 1.0/size;
            double[] positions = new double[splitPane.getItems().size() - 1];

            for (int i=1; i<size; i++) {
                positions[i -1] = i * divPos;
            }
            splitPane.setDividerPositions(positions);
        }
    }

    @Override
    public ObservableList<DockingContainer> getChildren() {
        return children;
    }

    @Override
    public Object getViewComponent() {
        return root;
    }

    @Override
    public void collapse() {
        DockingContainer parent = getParent();
        index = parent.getChildren().indexOf(this);
        parent.getChildren().removeAll(this);
        ((Parent) parent.getViewComponent()).layout();
    }

    @Override
    public void expand() {
        getParent().getChildren().add(index, this);
        ((Parent) getParent().getViewComponent()).layout();
    }
}
