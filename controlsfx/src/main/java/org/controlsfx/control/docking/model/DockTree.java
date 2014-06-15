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

package org.controlsfx.control.docking.model;

import com.sun.javafx.event.EventHandlerManager;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Side;

/**
 * DockTree is the root DockTreeItem in the tree hierarchy of the Docking
 * Framework. DockTree has four children LEFT, RIGHT, BOTTOM and CENTER each of 
 * which is a {@link DockTreeItem}. So, DockTree will always be in COMPLEX state.
 * Children can be added to each child to place contents on each side of the 
 * Dock. DockTree will allow adding Event Handlers. Event that was triggered in 
 * any level of DockTree's children will be bubbled up to DockTree. 
 */
public class DockTree extends DockTreeItem {

    private final DockTreeItem centerContainer = new DockTreeItem("CENTER PARENT");

    // properties
    // --- left
    private ReadOnlyObjectWrapper<DockTreeItem> left;
    public final ReadOnlyObjectProperty<DockTreeItem> leftProperty() {
        if (left == null) {
            DockTreeItem leftItem = new DockTreeItem("LEFT");
            leftItem.setSide(Side.LEFT);
            left = new ReadOnlyObjectWrapper<>(this, "left", leftItem);
        }
        return left.getReadOnlyProperty();
    }
    public final DockTreeItem getLeft() {
        return leftProperty().get();
    }

    // --- right
    private ReadOnlyObjectWrapper<DockTreeItem> right;
    public final ReadOnlyObjectProperty<DockTreeItem> rightProperty() {
        if (right == null) {
            DockTreeItem rightItem = new DockTreeItem("RIGHT");
            rightItem.setSide(Side.RIGHT);
            right = new ReadOnlyObjectWrapper<>(this, "right", rightItem);
        }
        return right.getReadOnlyProperty();
    }
    public final DockTreeItem getRight() {
        return rightProperty().get();
    }

    // --- center
    private ReadOnlyObjectWrapper<DockTreeItem> center;
    public final ReadOnlyObjectProperty<DockTreeItem> centerProperty() {
        if (center == null) {
            DockTreeItem centerItem = new DockTreeItem("CENTER");
            centerItem.setSide(Side.LEFT);
            center = new ReadOnlyObjectWrapper<>(this, "center", centerItem);
        }
        return center.getReadOnlyProperty();
    }
    public final DockTreeItem getCenter() {
        return centerProperty().get();
    }

    // --- bottom
    private ReadOnlyObjectWrapper<DockTreeItem> bottom;
    public final ReadOnlyObjectProperty<DockTreeItem> bottomProperty() {
        if (bottom == null) {
            DockTreeItem bottomItem = new DockTreeItem("BOTTOM");
            bottomItem.setSide(Side.BOTTOM);
            bottom = new ReadOnlyObjectWrapper<>(this, "bottom", bottomItem);
        }
        return bottom.getReadOnlyProperty();
    }
    public final DockTreeItem getBottom() {
        return bottomProperty().get();
    }

    /**
     * Constructs a default instance of DockTree
     */
    public DockTree() {
        // For dev purpose we will have some default instantiations
        this(null, null, null, null);
    }

    /**
     * Constructs an instance of DockTree with a DockTreeItem specified for each 
     * side. Null can be passed to denote an empty side. In that case, framework 
     * will act just like a BorderPane and will expand other sides to fill in
     * the space.
     * @param left
     * @param right
     * @param center
     * @param bottom 
     */
    public DockTree(DockTreeItem left, DockTreeItem right, DockTreeItem center, 
            DockTreeItem bottom) {
        setText("DOCK TREE");
        centerContainer.setSide(Side.LEFT);
        super.getChildren().add(centerContainer);

        if (left != null) {
            leftProperty().get().getChildren().add(left);
        }

        if (right != null) {
            rightProperty().get().getChildren().add(right);
        }

        if (center != null) {
            centerProperty().get().getChildren().add(center);
        }

        if (bottom != null) {
            bottomProperty().get().getChildren().add(bottom);
        }
        
        leftProperty().get().getChildren().addListener((Observable o) -> {
            if (getLeft().getChildren().isEmpty()) {
                super.getChildren().remove(getLeft());
            } else if (!super.getChildren().contains(getLeft())) {
                super.getChildren().add(0, getLeft());
            }
        });

        rightProperty().get().getChildren().addListener((Observable o) -> {
            if (getRight().getChildren().isEmpty()) {
                super.getChildren().remove(getRight());
            } else if (!super.getChildren().contains(getRight())) {
                int index = super.getChildren().size();
                super.getChildren().add(index, getRight());
            }
        });
        
        centerProperty().get().getChildren().addListener((Observable o) -> {
            if (getCenter().getChildren().isEmpty()) {
                centerContainer.getChildren().remove(getCenter());
            } else if (!super.getChildren().contains(getCenter())) {
                centerContainer.getChildren().add(0, getCenter());
            }
        });
        
        bottomProperty().get().getChildren().addListener((Observable o) -> {
            if (getCenter().getChildren().isEmpty()) {
                centerContainer.getChildren().remove(getBottom());
            } else if (!super.getChildren().contains(getBottom())) {
                int index = centerContainer.getChildren().size();
                centerContainer.getChildren().add(index, getBottom());
            }
        });
    }

    // Event Management
    private final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);

    /**
     * Registers an event handler to this DockTree. Events triggered in any level
     * of children will be bubbled up to this DockTree and can be handled by 
     * registering a handler
     * @param type EventType to be handled
     * @param handler Handler to be registered 
     */
    public void addEventHandler(EventType<DockTreeChangeEvent> type, 
            EventHandler<DockTreeChangeEvent> handler) {
        eventHandlerManager.addEventHandler(type, handler);
    }

    /**
     * Removes a previously registered handler
     * @param type EventType for which the handler was registered
     * @param handler Handler to be unregistered
     */
    public void removeEventHandler (EventType<DockTreeChangeEvent> type, 
            EventHandler<DockTreeChangeEvent> handler) {
        eventHandlerManager.removeEventHandler(type, handler);
    }

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain edc) {
        // handler manager can be null because we start to do modifications 
        // in constructor of DockTreeItem and we do not need those events anyway
        if (eventHandlerManager != null) {
            return edc.append(eventHandlerManager);
        }
        return edc;
    }
    
    @Override
    public final ObservableList<DockTreeItem> getChildren() {
        return FXCollections.unmodifiableObservableList(super.getChildren());
    }
}
