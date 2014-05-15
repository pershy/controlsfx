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

import javafx.collections.ObservableList;
import org.controlsfx.control.docking.model.DockTreeItem;

/**
 * A DockingContainer is the representation of DockTreeItem in View. 
 * DockingContainers will be used internally by the framework to layout view
 * components
 */
public interface DockingContainer {

    /**
     * Update the view component that this container holds with data from the
     * model DockTreeItem
     * @param item Model using which the view will be updated
     */
    public void updateView(DockTreeItem item);

    /**
     * DockingContainer can contain many other DockingContainers as its children
     * @return Children of this container
     */
    public ObservableList<DockingContainer> getChildren();

    /**
     * Holds the actual control / Pane used by the view. This need not be a
     * Node, as this method can also return specific model used by the View.
     * For example, Tab
     * @return The View object that this container holds.
     */
    public Object getViewComponent();

    /**
     * The DockTreeItem for which this container tries to create the view
     * @return The model DockTreeItem
     */
    public DockTreeItem getDockTreeItem();
}
