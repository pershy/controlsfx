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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import org.controlsfx.control.docking.model.DockTree;
import org.controlsfx.control.docking.model.DockTreeItem;
import org.controlsfx.control.docking.model.DockTreeItem.DockMode;
import org.controlsfx.control.docking.model.DockTreeItem.DockTreeChangeEvent;

/**
 */
public class Dock extends Region {
    
    private static final String TAB_KEY = "TAB";
    private static final String DOCK_PANE_KEY = "DOCK_PANE";
    private static final String SIDE_KEY = "SIDE";

    private final BorderPane borderPane;
    private DockingContainer rootContainer;

    // side panels
    private final ToolBar rightSidePanel;
    private final ToolBar leftSidePanel;
    private final ToolBar bottomSidePanel;
    
    private final WeakHashMap<DockTreeItem, Button> weakButtonMap = new WeakHashMap<>();
    private final WeakHashMap<DockTreeItem, DockingContainer> weakContainerMap = new WeakHashMap<>();

    // Root listener to listen for changes in the model
    private final EventHandler<DockTreeChangeEvent> treeModificationEventHandler = t -> {
        // TODO Listen for appropriate event types and layout only the 
        // item that was changed
        DockTreeItem affectedItem = (DockTreeItem) t.getSource();
        if (DockTreeItem.DOCK_MODE_CHANGE_EVENT == t.getEventType()) {
            switch (t.getNewMode()) {
                case FLOATING:
                    // TODO
                    break;
                case COLLAPSED:
                    collapse(affectedItem);
                    break;
                case DOCKED:
                    expand(affectedItem);
                    break;
            }
        }
    };

    private WeakReference<DockTree> oldDockTree;
    
    // --- Dock Tree Property
    private ObjectProperty<DockTree> dockTree;
    public final ObjectProperty<DockTree> dockTreeProperty() {
        if (dockTree == null) {
            dockTree = new SimpleObjectProperty<DockTree>(this, "dockTree") {
                @Override
                public void invalidated() {
                    if (oldDockTree != null && oldDockTree.get() != null) {
                        oldDockTree.get()
                                .removeEventHandler(DockTreeItem.TREE_MODIFICATION_EVENT, 
                                        treeModificationEventHandler);
                    }
                    setUpRootContainer(new DockArea(Dock.this, get()));
                    getDockTree()
                            .addEventHandler(DockTreeItem.TREE_MODIFICATION_EVENT,
                                    treeModificationEventHandler);
                    oldDockTree = new WeakReference<>(getDockTree());
                }
            };
        }
        return dockTree;
    }
    public final void setDockTree(DockTree tree) { dockTreeProperty().set(tree); }
    public final DockTree getDockTree() { return dockTreeProperty().get(); }

    /**
     * Only constructor for Dock
     * @param tree The model using which this Dock layout the view
     */
    public Dock(DockTree tree) {
        borderPane = new BorderPane();
        rightSidePanel = new ToolBar();
        leftSidePanel = new ToolBar();
        bottomSidePanel = new ToolBar();
        rightSidePanel.setOrientation(Orientation.VERTICAL);
        leftSidePanel.setOrientation(Orientation.VERTICAL);
        bottomSidePanel.setOrientation(Orientation.HORIZONTAL);
        getChildren().add(borderPane);
        setDockTree(tree);
    }
    
    private void setUpRootContainer(DockingContainer container) {
        rootContainer = container;
        borderPane.setCenter((Node) rootContainer.getViewComponent());
        layoutDockTreeItem(rootContainer, getDockTree().getChildren());
    }

    private void layoutDockTreeItem(DockingContainer parent, List<DockTreeItem> items) {
        if (items == null || parent == null) {
            return;
        }
        List<DockingContainer> containers = new ArrayList<>();
        items.stream().forEach((item) -> {
            DockingContainer container;
            boolean isCollapsed = DockMode.COLLAPSED == item.getDockMode();
            boolean isFloating = DockMode.FLOATING == item.getDockMode();
            if (!(isCollapsed || isFloating)) {
                if (DockTreeItem.State.COMPLEX == item.getState()) {
                    container = new DockArea(this, item);
                    layoutDockTreeItem(container, item.getChildren());
                } else {
                    container = new DockTab(this, item);
                }
                weakContainerMap.put(item, container);
                containers.add(container);
                // FIXME Indexing has to be done in children listener
                container.setIndex(containers.size() - 1);
            }
        });
        parent.getChildren().setAll(containers);
    }

    // get container for a specific DockTreeItem
    private DockingContainer getContainerForItem(DockingContainer start, DockTreeItem treeItem) {
        DockingContainer container;
        if (start == null) {
            container = rootContainer;
        } else {
            container = start;
        }
        if (container.getDockTreeItem().equals(treeItem)) {
            return container;
        } else {
            for (DockingContainer child : container.getChildren()) {
                if (getContainerForItem(child, treeItem) != null) {
                    return child;
                }
            }
            return null;
        }
    }
    
    private void collapse(DockTreeItem item) {
        Side itemSide = item.getSide();
        List<Button> btnList = getButtonsOnSide(itemSide);
        
        if (btnList == null || btnList.isEmpty()) {
            switch (itemSide) {
                case LEFT:
                    borderPane.setLeft(leftSidePanel);
                    break;
                case RIGHT:
                    borderPane.setRight(rightSidePanel);
                    break;
                case BOTTOM:
                    borderPane.setBottom(bottomSidePanel);
                    break;
            }
        }
        
        // FIXME No need of a list here. We will have it until we make sure that
        // everything is working as expected
        List<Button> newBtns = new ArrayList<>();
        if (item.getChildren().isEmpty()) {
            Button newBtn = createSidePanelButton(item);
            newBtns.add(newBtn);
            weakButtonMap.put(item, newBtn);
        }
        getButtonsOnSide(itemSide).addAll(newBtns);

        DockingContainer container = weakContainerMap.get(item);
        container.collapse();
    }

    private void expand(DockTreeItem item) {
        Side itemSide = item.getSide();
        List<Button> btnsToRemove = new ArrayList<>();
        // Only simple items will have button that has to be removed.
        // Even when complex items are expanded, seperate events will be
        // fired for each simple item within the complex item
        if (item.getChildren().isEmpty()) {
            btnsToRemove.add(weakButtonMap.get(item));
            weakButtonMap.remove(item);
        }
        getButtonsOnSide(itemSide).removeAll(btnsToRemove);
        List<Button> btnList = getButtonsOnSide(itemSide);
        if (btnList == null || btnList.isEmpty()) {
            switch (itemSide) {
                case LEFT:
                    borderPane.setLeft(null);
                    break;
                case RIGHT:
                    borderPane.setRight(null);
                    break;
                case BOTTOM:
                    borderPane.setBottom(null);
                    break;
            }
        }

        DockingContainer container = weakContainerMap.get(item);
        container.expand();
    }

    private List<DockTreeItem> collectSimpleItems(DockTreeItem parent) {
        final List<DockTreeItem> items = new ArrayList<>();
        if (parent.getChildren().isEmpty()) {
            items.add(parent);
        } else {
            parent.getChildren().stream()
                    .forEach(item -> {
                        items.addAll(collectSimpleItems(item));
                    });
        }
        return items;
    }

    private Button createSidePanelButton(DockTreeItem item) {
        Button btn = new Button(item.getText());
        btn.setGraphic(item.getGraphic());
        btn.setOnAction(event -> item.setDockMode(DockTreeItem.DockMode.DOCKED));
        return btn;
    }
    
    @Override protected void layoutChildren() {
        borderPane.resizeRelocate(0, 0, getWidth(), getHeight());
    }
    
    private List<Button> getButtonsOnSide(Side side) {
        @SuppressWarnings("unchecked")
        List<Button> buttonsOnSide = null;
        switch (side) {
            case LEFT:
                buttonsOnSide = (List<Button>) (Object) leftSidePanel.getItems();
                break;
            case RIGHT:
                buttonsOnSide = (List<Button>) (Object) rightSidePanel.getItems();
                break;
            case BOTTOM:
                buttonsOnSide = (List<Button>) (Object) bottomSidePanel.getItems();
                break;
        }

        return buttonsOnSide;
    }
}
