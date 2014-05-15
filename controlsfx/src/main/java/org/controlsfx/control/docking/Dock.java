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
    
    // Root listener to listen for changes in the model
    private final EventHandler<DockTreeChangeEvent> treeModificationEventHandler = t -> {
        // TODO Listen for appropriate event types and layout only the 
        // item that was changed
        DockTreeItem affectedItem = (DockTreeItem) t.getSource();
        if (t.getEventType().equals(DockTreeItem.DOCK_MODE_CHANGE_EVENT)) {
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
                    layoutDockTreeItem(get(), null);
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
        layoutDockTreeItem(getDockTree(), null);
    }

    private void layoutDockTreeItem(DockTreeItem item, DockingContainer parent) {
        if (item == null) {
            return;
        }

        boolean isCollapsed = item.getDockMode().equals(DockTreeItem.DockMode.COLLAPSED);
        boolean isFloating = item.getDockMode().equals(DockTreeItem.DockMode.FLOATING);
        if (isCollapsed || isFloating) {
            return;
        }
        final DockingContainer container;
        if (item.getState().equals(DockTreeItem.State.COMPLEX)) {
            container = new DockArea(this, item);
            if (parent == null) {
                borderPane.setCenter((Node) container.getViewComponent());
                rootContainer = container;
            } else {
                parent.getChildren().add(container);
            }
            item.getChildren().stream().forEach((treeItem) -> {
                layoutDockTreeItem(treeItem, (DockArea) container);
            });
        } else {
            container = new DockTab(this, item);
            parent.getChildren().add(container);
        }
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
        List<Button> newBtns = new ArrayList<>();
        if (item.getChildren().isEmpty()) {
            Button newBtn = createSidePanelButton(item);
            newBtns.add(newBtn);
            weakButtonMap.put(item, newBtn);
        } else {
            List<DockTreeItem> simpleItems = collectSimpleItems(item);
            simpleItems.stream().forEach(simpleItem -> {
                Button newBtn = createSidePanelButton(simpleItem);
                newBtns.add(newBtn);
                weakButtonMap.put(simpleItem, newBtn);
            });
        }
        getButtonsOnSide(itemSide).addAll(newBtns);
        // Relayout from Parent container
        // TODO Should move re-layout to single method
        DockingContainer parentContainer = getContainerForItem(null, item.getParent());
        parentContainer.getChildren().clear();
        layoutDockTreeItem(item.getParent(), parentContainer);
    }
    
    private void expand(DockTreeItem item) {
        Side itemSide = item.getSide();
        List<Button> btnsToRemove = new ArrayList<>();
        if (item.getChildren().isEmpty()) {
            btnsToRemove.add(weakButtonMap.get(item));
            weakButtonMap.remove(item);
        } else {
            List<DockTreeItem> simpleItems = collectSimpleItems(item);
            simpleItems.stream().forEach(simpleItem -> {
                Button btn = weakButtonMap.get(simpleItem);
                btnsToRemove.add(btn);
                weakButtonMap.remove(simpleItem);
            });
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
        
        // Relayout from parent
        DockingContainer parentContainer = getContainerForItem(null, item.getParent());
        parentContainer.getChildren().clear();
        layoutDockTreeItem(item.getParent(), parentContainer);
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
