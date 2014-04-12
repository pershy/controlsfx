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
package org.controlsfx.control;

import impl.org.controlsfx.skin.SegmentedBarSkin;
import javafx.beans.DefaultProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * The SegmentedBar is a simple control that forces together a group of Nodes
 * such that they appear as one collective bar.
 *
 * This is better clarified with a picture:
 * <br/>
 * <center>
 * <img src="segmentedBar.png"/>
 * </center>
 *
 *
 * <h3>Which child nodes are supported?</h3>
 * The SegmentedBar will dynamically add the correct pill styles (left-pill, center-pill, right-pill)
 * to each child node depending on its position. That means the only thing a controls must have is a
 * matching CSS which defines the look for each of those styles.
 *
 * JavaFX supports all Button based controls by default. We additionally support ComboBox and SplitMenuButton.
 *
 *
 * <h3>Code Samples</h3>
 *
 * TODO
 *
 *
 */
@DefaultProperty("items")
public class SegmentedBar<T extends Node> extends Control {

    /***************************************************************************
     *                                                                         *
     * Static fields                                                           *
     *                                                                         *
     **************************************************************************/


    /***************************************************************************
     *                                                                         *
     * Private fields                                                          *
     *                                                                         *
     **************************************************************************/

    private final ObservableList<T> items;

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a default SegmentedBar instance with no items.
     */
    public SegmentedBar() {
        this((ObservableList<T>)null);
    }

    /**
     * Creates a default SegmentedBar instance with the provided buttons
     * inserted into it.
     *
     * @param items A varargs array of nodes to add into the SegmentedBar
     *      instance.
     */
    public SegmentedBar(T... items) {
        this(items == null ?
                FXCollections.<T>observableArrayList() :
                FXCollections.observableArrayList(items));
    }

    /**
     * Creates a default SegmentedBar instance with the provided nodes
     * inserted into it.
     *
     * @param items A list of nodes to add into the SegmentedBar instance.
     */
    public SegmentedBar(ObservableList<T> items) {
        getStyleClass().add("segmented-bar"); //$NON-NLS-1$
        this.items = items == null ? FXCollections.<T>observableArrayList() : items;

        // Fix for Issue #87:
        // https://bitbucket.org/controlsfx/controlsfx/issue/87/segmentedbutton-keyboard-focus-traversal
        setFocusTraversable(false);
    }



    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /** {@inheritDoc} */
    @Override protected Skin<?> createDefaultSkin() {
        return new SegmentedBarSkin<T>(this);
    }

    /**
     * Returns the list of nodes that this SegmentedBar will draw together
     * into one 'grouped bar'. It is possible to modify this list to add or
     * remove {@link javafx.scene.Node} instances, as shown in the javadoc
     * documentation for this class.
     */
    public final ObservableList<T> getItems() {
        return items;
    }


    /***************************************************************************
     *                                                                         *
     * CSS                                                                     *
     *                                                                         *
     **************************************************************************/
    
    /**
     * {@inheritDoc}
     */
    @Override protected String getUserAgentStylesheet() {
        return SegmentedBar.class.getResource("segmentedbar.css").toExternalForm(); //$NON-NLS-1$
    }
}