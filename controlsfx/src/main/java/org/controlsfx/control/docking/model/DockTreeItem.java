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

import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import org.controlsfx.control.docking.Dock;

/**
 * A DockTreeItem is the model used to represent the hierarchy of the ControlsFX
 * docking framework. A DockTreeItem can be in one of two states, for now known
 * as 'simple' and 'complex'. In the 'simple' state, the DockTreeItem is essentially
 * a Tab consisting of a title and content, shown relative to its parent (which
 * is necessarily complex by definition of having children). It follows then
 * that in 'complex' state, a DockTreeItem is a SplitPane with orientation
 * opposite to that of its parent, into which children DockTreeItem elements 
 * can be inserted.
 * 
 * DockTreeItem will not allow registering event handlers and events occuring 
 * on DockTreeItem will be bubbled up to the root {@link DockTree}
 */
public class DockTreeItem implements EventTarget {

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
         * the children list.
         */
        COMPLEX
    }

    public static enum DockMode {
        /**
         * Represents the DockMode when the node represented by the DockTreeItem 
         * is collapsed. In this mode, the node will not be visible in the Dock 
         * and a toggle button will appear on the respective side bar
         */
        COLLAPSED,
        
        /**
         * Represents the DockMode when the node represented by the DockTreeItem
         * is previewed above other nodes in its side as a result of user 
         * clicking on the toggle button in the side bar
         */
        FLOATING,
        
        /**
         * Represents the DockMode when the node represented by the DockTreeItem
         * is visible inside the {@link Dock} in its respective position. This 
         * is the default DockMode of a DockTreeItem
         */
        DOCKED
    }
    
    /**************************************************************************
     * 
     * Event Definitions and Event Management
     * 
     *************************************************************************/

    public static final EventType<DockTreeChangeEvent> TREE_MODIFICATION_EVENT
            = new EventType<>(Event.ANY, "TreeModificationEvent");
    
    public static final EventType<DockTreeChangeEvent> DOCK_MODE_CHANGE_EVENT
            = new EventType<>(TREE_MODIFICATION_EVENT, "DockModeChangeEvent");

    public static final EventType<DockTreeChangeEvent> CONTENT_CHANGE_EVENT
            = new EventType<>(TREE_MODIFICATION_EVENT, "ContentChangeEvent");

    public static final EventType<DockTreeChangeEvent> GRAPHIC_CHANGE_EVENT
            = new EventType<>(TREE_MODIFICATION_EVENT, "GraphicChangeEvent");

    public static final EventType<DockTreeChangeEvent> TEXT_CHANGE_EVENT
            = new EventType<>(TREE_MODIFICATION_EVENT, "TextChangeEvent");

    public static final EventType<DockTreeChangeEvent> CHILDREN_MODIFICATION_EVENT
            = new EventType<>(TREE_MODIFICATION_EVENT, "ChildrenModificationEvent");

    public static final EventType<DockTreeChangeEvent> STATE_CHANGE_EVENT
            = new EventType<>(CHILDREN_MODIFICATION_EVENT, "StateChangeEvent");

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain edc) {
        if (getParent() != null) {
            getParent().buildEventDispatchChain(edc);
        }
        return edc;
    }

    private void fireEvent(DockTreeChangeEvent event) {
        Event.fireEvent(this, event);
    }

    /**
     * Event class that contains data about what changes happened in the
     * DockTreeItem
     */
    public static class DockTreeChangeEvent extends Event {

        private DockTreeItem dockTreeItem;
        private Node newContent;
        private List<? extends DockTreeItem> addedItems;
        private List<? extends DockTreeItem> removedItems;
        private DockMode newMode;
        private State newState;
        private String newText;

        /**
         * Constructs a default DockTreeChangeEvent without any specific 
         * information
         * @param eventType Type of event occured. Mostly EventType.ANY in this case
         * @param dockTreeItem The Item on which the event occurred
         */
        public DockTreeChangeEvent(EventType<? extends Event> eventType, DockTreeItem dockTreeItem) {
            super(eventType);
            this.dockTreeItem = dockTreeItem;
            this.newContent = null;
            this.addedItems = null;
            this.removedItems = null;
            this.newState = null;
            this.newMode = null;
            this.newText = null;
        }

        /**
         * Constructs a DockTreeChangeEvent which can be used when there is a 
         * change in the children list of {@link DockTreeItem}
         * @param eventType The type of event that has occured
         * @param dockTreeItem The Item on which the event occured
         * @param addedItems A list items which were added to the DockTreeItem
         * @param removedItems A list of item which were removed from DockTreeItem
         */
        public DockTreeChangeEvent(EventType<? extends Event> eventType, DockTreeItem dockTreeItem,
                List<? extends DockTreeItem> addedItems, List<? extends DockTreeItem> removedItems) {
            this(eventType, dockTreeItem);
            this.addedItems = addedItems;
            this.removedItems = removedItems;
        }

        /**
         * Constructs a DockTreeChangeEvent which can be used when state of the 
         * DockTreeItem changes
         * @param eventType The type of event that has occured
         * @param dockTreeItem The Item on which the event occured
         * @param state The new state of the DockTreeItem
         */
        public DockTreeChangeEvent(EventType<? extends Event> eventType, DockTreeItem dockTreeItem,
                State state) {
            this(eventType, dockTreeItem);
            this.newState = state;
        }

        /**
         * Constructs a DockTreeChangeEvent which can be used when mode of the
         * DockTreeItem changes
         * @param eventType The type of event that has occured
         * @param dockTreeItem The Item on which the event occured
         * @param mode The new DockMode of this item
         */
        public DockTreeChangeEvent(EventType<? extends Event> eventType, DockTreeItem dockTreeItem,
                DockMode mode) {
            this(eventType, dockTreeItem);
            this.newMode = mode;
        }

        /**
         * Constructs a DockTreeChangeEvent which can be used when content of the
         * DockTreeItem changes
         * @param eventType The type of event that has occured
         * @param dockTreeItem The Item on which the event occured
         * @param content The new content
         */
        public DockTreeChangeEvent(EventType<? extends Event> eventType, DockTreeItem dockTreeItem,
                Node content) {
            this(eventType, dockTreeItem);
            this.newContent = content;
        }

        /**
         * Constructs a DockTreeChangeEvent which can be used when text of the 
         * DockTreeItem changes
         * @param eventType The type of event that has occured
         * @param dockTreeItem The Item on which the event occured
         * @param text The new Text
         */
        public DockTreeChangeEvent(EventType<? extends Event> eventType, DockTreeItem dockTreeItem,
                String text) {
            this(eventType, dockTreeItem);
            this.newText = text;
        }

        /**
         * Gets the DockTreeItem on which the event initially occurred
         * @return {@link DockTreeItem}
         */
        @Override
        public Object getSource() {
            return dockTreeItem;
        }

        public Node getNewContent() {
            return newContent;
        }

        public List<? extends DockTreeItem> getAddedItems() {
            return addedItems;
        }

        public List<? extends DockTreeItem> getRemovedItems() {
            return removedItems;
        }

        public DockMode getNewMode() {
            return newMode;
        }

        public State getNewState() {
            return newState;
        }

        public String getNewText() {
            return newText;
        }

    }
    
    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/

    // Each DockTreeItem can contain children items. If this list is
    // non-empty, the DockTreeItem changes state from simple to complex.
    private final ObservableList<DockTreeItem> children = FXCollections.observableArrayList();
    
    private final InvalidationListener itemsListListener = o -> checkState();
    
    // Listens Addition or Removal from children list and updates the parent for each child
    // added or removed
    private final ListChangeListener<DockTreeItem> childrenListener = (Change<? extends DockTreeItem> change) -> {
        while (change.next()) {
            List<? extends DockTreeItem> addedItems  = change.getAddedSubList();
            List<? extends DockTreeItem> removedItems = change.getRemoved();
            if (addedItems != null) {
                addedItems.stream().filter((treeItem) -> (treeItem != null))
                    .forEach((treeItem) -> (treeItem.setParent(this)));
            }
            if (removedItems != null) {
                removedItems.stream().filter((treeItem) -> (treeItem != null))
                    .forEach((treeItem) -> (treeItem.setParent(null)));
            }
            fireEvent(new DockTreeChangeEvent(CHILDREN_MODIFICATION_EVENT, this, addedItems, removedItems));
        }
    };

    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    /**
     * Creates a default DockTreeItem instance
     */
    public DockTreeItem() {
        this("New Tab");
    }

    /**
     * Creates a DockTreeItem with title text
     * @param text The title for this item 
     */
    public DockTreeItem(String text) {
        this(text, null);
    }
    
    public DockTreeItem(String text, Node content) {
        setText(text);
        setContent(content);
        children.addListener(itemsListListener);
        children.addListener(childrenListener);
    }

    private void checkState() {
        // if children list is not empty, we are in the complex state
        setState(!children.isEmpty() ? State.COMPLEX : State.SIMPLE);
    }

    /**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/
    
    // miscellaneous properties of this specific tree item
    // e.g. text, graphic, context menu, content, state
    // Conceptually this should be thought of as a Tab, but we are not constraining
    // ourselves to being a Tab.
    
    // --- text
    private StringProperty text;
    public final StringProperty textProperty() {
        if(text == null) {
            text = new StringPropertyBase() {
                @Override
                public void invalidated() {
                    fireEvent(new DockTreeChangeEvent(TEXT_CHANGE_EVENT, DockTreeItem.this, get()));
                }

                @Override
                public Object getBean() {
                    return DockTreeItem.this;
                }

                @Override
                public String getName() {
                    return "text";
                }
                
            };
        }
        return text;
    }
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
            graphic = new ObjectPropertyBase<Node>() {
                @Override
                public void invalidated() {
                    fireEvent(new DockTreeChangeEvent(GRAPHIC_CHANGE_EVENT, DockTreeItem.this, get()));
                }

                @Override
                public Object getBean() {
                    return DockTreeItem.this;
                }

                @Override
                public String getName() {
                    return "graphic";
                }
                
            };
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
            content = new ObjectPropertyBase<Node>() {
                @Override
                public void invalidated() {
                    fireEvent(new DockTreeChangeEvent(CONTENT_CHANGE_EVENT, DockTreeItem.this, get()));
                }

                @Override
                public Object getBean() {
                    return DockTreeItem.this;
                }

                @Override
                public String getName() {
                    return "content";
                }
                
            };
        }
        return content;
    }
    
    // --- parent
    private ReadOnlyObjectWrapper<DockTreeItem> parent = new ReadOnlyObjectWrapper<>(this, "parent");
    void setParent(DockTreeItem value) { parent.setValue(value); }
    /**
     * The parent of this DockTreeItem. A DockTreeItem can have no more than one parent at a time
     * @return The parent of this DockTreeItem or null if this is a root item
     */
    public DockTreeItem getParent() { return parent.getValue(); }
    
    // --- state
    private ObjectProperty<State> state = new ObjectPropertyBase<State>(State.SIMPLE) {
        @Override
        public void invalidated() {
            fireEvent(new DockTreeChangeEvent(STATE_CHANGE_EVENT, DockTreeItem.this, get()));
        }

        @Override
        public Object getBean() {
            return DockTreeItem.this;
        }

        @Override
        public String getName() {
            return "state";
        }
        
    };
    private final void setState(State value) { stateProperty().set(value);  }
    public final State getState() { return state.get(); }
    public final ObjectProperty<State> stateProperty() { return state; }
    
    // --- Collapse Side
    private ObjectProperty<Side> side;
    public final ObjectProperty<Side> sideProperty() {
        if (side == null) {
            side = new SimpleObjectProperty(this, "side");
        }
        return side;
    }
    public final void setSide(Side side) { sideProperty().set(side); }
    /**
     * The side to which this DockTreeItem will collpase to. By default,
     * DockTreeItems will have the side of its parent.
     * @return 
     */
    public final Side getSide() {
        if (sideProperty().get() == null) {
            return getParent().getSide();
        }
        return sideProperty().get();
    }
    
    // --- dock mode
    private ObjectProperty<DockMode> dockMode = new ObjectPropertyBase<DockMode>(DockMode.DOCKED) {
        @Override
        public void invalidated() {
            // If this item is COMPLEX, and it is COLLAPSED, all of its
            // children are collapsed
            if (DockMode.COLLAPSED == get() && !getChildren().isEmpty()) {
                getChildren().stream().forEach((DockTreeItem item) -> {
                    item.setDockMode(get());
                });
            }

            // Check siblings of this item. If all siblings are COLLAPSED,
            // parent must be collapsed.
            DockTreeItem parent = getParent();
            if (parent != null && DockMode.COLLAPSED == get()) {
                int size = parent.getChildren()
                        .filtered(item -> item.getDockMode() == get())
                        .size();
                if (size == parent.getChildren().size()) {
                    parent.setDockMode(get());
                }
            // If this item is DOCKED, the parent must also be docked
            // if it not already DOCKED
            } else if (parent != null && DockMode.DOCKED == get()) {
                if (parent.getDockMode() != get()) {
                     parent.setDockMode(get());
                }
            }

            fireEvent(new DockTreeChangeEvent(DOCK_MODE_CHANGE_EVENT, DockTreeItem.this, get()));
        }

        @Override
        public Object getBean() {
            return DockTreeItem.this;
        }

        @Override
        public String getName() {
            return "dockMode";
        }
    };
    public final void setDockMode(DockMode mode) { dockMode.set(mode); }
    public final DockMode getDockMode() { return dockMode.get(); }
    public final ObjectProperty<DockMode> dockModeProperty() { return dockMode; }

    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/

    /**
     * Children of this DockTreeItem
     * @return All the children of this DockTreeItem
     */
    public final ObservableList<DockTreeItem> getChildren() {
        return children;
    }

}
