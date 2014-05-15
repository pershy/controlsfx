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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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

    private final DockTreeItem centerContainer = new DockTreeItem();

    // properties
    // --- left
    private ObjectProperty<DockTreeItem> left;
    public final ObjectProperty<DockTreeItem> leftProperty() {
        if (left == null) {
            left = new SimpleObjectProperty<>(this, "left");
        }
        return left;
    }
    public final void setLeft(DockTreeItem left) {
        if (left != null) {
            left.setSide(Side.LEFT);
        }
        leftProperty().set(left);
    }
    public final DockTreeItem getLeft() { return leftProperty().get(); }

    // --- right
    private ObjectProperty<DockTreeItem> right;
    public final ObjectProperty<DockTreeItem> rightProperty() {
        if (right == null) {
            right = new SimpleObjectProperty<>(this, "right");
        }
        return right;
    }
    public final void setRight(DockTreeItem right) {
        if (right != null) {
            right.setSide(Side.RIGHT);
        }
        rightProperty().set(right);
    }
    public final DockTreeItem getRight() { return rightProperty().get(); }

    // --- center
    private ObjectProperty<DockTreeItem> center;
    public final ObjectProperty<DockTreeItem> centerProperty() {
        if (center == null) {
            center = new SimpleObjectProperty<>(this, "center");
        }
        return center;
    }
    public final void setCenter(DockTreeItem center) {
        // By default center will collpase to LEFT
        if (center != null) {
            center.setSide(Side.LEFT);
        }
        centerProperty().set(center);
    }
    public final DockTreeItem getCenter() { return centerProperty().get(); }

    // --- bottom
    private ObjectProperty<DockTreeItem> bottom;
    public final ObjectProperty<DockTreeItem> bottomProperty() {
        if (bottom == null) {
            bottom = new SimpleObjectProperty<>(this, "bottom");
        }
        return bottom;
    }
    public final void setBottom(DockTreeItem bottom) {
        if (bottom != null) {
            bottom.setSide(Side.BOTTOM);
        }
        bottomProperty().set(bottom);
    }
    public final DockTreeItem getBottom() { return bottomProperty().get(); }

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
        setLeft(left);
        setRight(right);
        setCenter(center);
        setBottom(bottom);

        // We will not check null here. Will do it in View during layout
        centerContainer.getChildren().addAll(getCenter(), getBottom());
        getChildren().addAll(getLeft(), centerContainer, getRight());

        leftProperty().addListener((ov, oldItem, newItem) -> {
            getChildren().remove(oldItem);
            getChildren().add(0, newItem);
        });
        
        rightProperty().addListener((ov, oldItem, newItem) -> {
            getChildren().remove(oldItem);
            getChildren().add(2, newItem);
        });
        
        centerProperty().addListener((ov, oldItem, newItem) -> {
            centerContainer.getChildren().remove(oldItem);
            centerContainer.getChildren().add(0, newItem);
        });
        
        bottomProperty().addListener((ov, oldItem, newItem) -> {
            centerContainer.getChildren().remove(oldItem);
            centerContainer.getChildren().add(1, newItem);
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
}
