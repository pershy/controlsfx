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

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;

/**
 * A DockTreeItem is the model used to represent the hierachy of the ControlsFX
 * docking framework. A DockTreeItem can be in one of two states, for now known
 * as 'simple' and 'complex'. In the 'simple' state, the DockTreeItem is essentially
 * a Tab consisting of a title and content, shown relative to its parent (which
 * is necessarily complex by definition of having children). It follows then
 * that the 'complex' state, a DockTreeItem is a container consisting of multiple
 * regions (top, right, bottom, left and center), into which children
 * DockTreeItem elements can be inserted.
 */
public class DockTreeItem {
    
    /**************************************************************************
     * 
     * Static enumerations
     * 
     **************************************************************************/
    
    public static enum State {
        /**
         * Represents the state when the DockTreeItem consists of a single
         * {@link DockTreeItem#contentProperty() content node}, and no children
         * elements in the children lists.
         */
        SIMPLE,
        
        /**
         * Represents the state when the DockTreeItem has one or more items in
         * the children lists.
         */
        COMPLEX
    }
    
    
    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    // Each DockTreeItem can contain children elements. If any of these lists is
    // non-empty, the DockTreeItem changes state from simple to complex.
    private final ObservableList<DockTreeItem> topItems = FXCollections.<DockTreeItem>observableArrayList();
    private final ObservableList<DockTreeItem> rightItems = FXCollections.observableArrayList();
    private final ObservableList<DockTreeItem> bottomItems = FXCollections.observableArrayList();
    private final ObservableList<DockTreeItem> leftItems = FXCollections.observableArrayList();
    private final ObservableList<DockTreeItem> centerItems = FXCollections.observableArrayList();
    
    private final InvalidationListener itemsListListener = o -> checkState();
    
    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    /**
     * Creates a defult DockTreeItem instance
     */
    public DockTreeItem() {
        topItems.addListener(itemsListListener);
        rightItems.addListener(itemsListListener);
        bottomItems.addListener(itemsListListener);
        leftItems.addListener(itemsListListener);
        centerItems.addListener(itemsListListener);
    }
    

    private void checkState() {
        // if any of the items lists are not empty, we are in the complex state
        setState((!topItems.isEmpty() || 
                  !rightItems.isEmpty() || 
                  !bottomItems.isEmpty() || 
                  !leftItems.isEmpty() || 
                  !centerItems.isEmpty()) ? State.COMPLEX : State.SIMPLE);
    }


    /**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/
    
    // miscellaneous properties of this specific tree item
    // e.g. text, graphic, context menu, content, collapsed
    // Conceptually this should be thought of as a Tab, but we are not constraining
    // ourselves to being a Tab.
    
    // --- text
    private StringProperty text = new SimpleStringProperty(this, "text");
    public final StringProperty textProperty() { return text; }
    public final void setText(String value) { textProperty().set(value); }
    public final String getText() { return text.get(); }
    
    
    // --- graphic
    private ObjectProperty<Node> graphic;

    /**
     * <p>Sets the graphic to show in the visual representation of this DockTreeItem,
     * to allow the user to differentiate between the function of each DockTreeItem.</p>
     */
    public final void setGraphic(Node value) {
        graphicProperty().set(value);
    }

    /**
     * The graphic shown in the DockTreeItem.
     *
     * @return The graphic shown in the DockTreeItem.
     */
    public final Node getGraphic() {
        return graphic == null ? null : graphic.get();
    }

    /**
     * The graphic in the DockTreeItem.
     * 
     * @return The graphic in the DockTreeItem.
     */
    public final ObjectProperty<Node> graphicProperty() {
        if (graphic == null) {
            graphic = new SimpleObjectProperty<Node>(this, "graphic");
        }
        return graphic;
    }
    
    
    // --- context menu
    /**
     * The ContextMenu to show for this DockTreeItem.
     */
    private ObjectProperty<ContextMenu> contextMenu = new SimpleObjectProperty<>(this, "contextMenu");
    public final ObjectProperty<ContextMenu> contextMenuProperty() { return contextMenu; }
    public final void setContextMenu(ContextMenu value) { contextMenu.setValue(value); }
    public final ContextMenu getContextMenu() { return contextMenu.getValue(); }
    
    
    // --- content
    private ObjectProperty<Node> content;

    /**
     * <p>The content to show within this DockTreeItem. The content
     * can be any Node such as UI controls or groups of nodes added
     * to a layout container.</p>
     */
    public final void setContent(Node value) {
        contentProperty().set(value);
    }

    /**
     * <p>The content associated with the DockTreeItem.</p>
     *
     * @return The content associated with the DockTreeItem.
     */
    public final Node getContent() {
        return content == null ? null : content.get();
    }

    /**
     * <p>The content associated with the DockTreeItem.</p>
     */
    public final ObjectProperty<Node> contentProperty() {
        if (content == null) {
            content = new SimpleObjectProperty<Node>(this, "content");
        }
        return content;
    }
    
    
    // --- state
    private ObjectProperty<State> state = new SimpleObjectProperty<State>(this, "state", State.SIMPLE);
    private final void setState(State value) { stateProperty().set(value);  }
    public final State getState() { return state.get(); }
    public final ObjectProperty<State> stateProperty() { return state; }
    
    
    // --- collapsed
    private BooleanProperty collapsed = new SimpleBooleanProperty(this, "collapsed", false);
    public final void setCollapsed(boolean value) { collapsedProperty().set(value);  }
    public final boolean isCollapsed() { return collapsed.get(); }
    public final BooleanProperty collapsedProperty() { return collapsed; }
    
    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    public final ObservableList<DockTreeItem> getTopItems() {
        return topItems;
    }
    
    public final ObservableList<DockTreeItem> getRightItems() {
        return rightItems;
    }
    
    public final ObservableList<DockTreeItem> getBottomItems() {
        return bottomItems;
    }
    
    public final ObservableList<DockTreeItem> getLeftItems() {
        return leftItems;
    }
    
    public final ObservableList<DockTreeItem> getCenterItems() {
        return centerItems;
    }
}
