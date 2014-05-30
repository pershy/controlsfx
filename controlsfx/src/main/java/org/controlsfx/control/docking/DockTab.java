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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.controlsfx.control.docking.model.DockTreeItem;

/**
 * Represents a Simple DockTree item in View as a Tab. This container will not 
 * have any children.
 */
public class DockTab extends DockingContainer {

    // PRIVATE DECLARATIONS
    private Tab tab;

    /**
     * Constructor
     *
     * @param dock
     * @param item
     */
    public DockTab(Dock dock, DockTreeItem item) {
        if (DockTreeItem.State.COMPLEX == item.getState()) {
            throw new IllegalStateException("Cannot use COMPLEX DockTreeItem in a DockTab");
        }
        tab = new Tab();
        setDockTreeItem(item);
    }

    @Override
    public void updateView(DockTreeItem item) {
        // Assuming that DockTreeItem is in SIMPLE State
        tab.setText(item.getText());
        tab.setContent(item.getContent());
        tab.setGraphic(item.getGraphic());
    }

    @Override
    public Object getViewComponent() {
        return tab;
    }

    @Override
    public ObservableList<DockingContainer> getChildren() {
        // Ideally DockTab should not have any children
        return FXCollections.observableArrayList();
    }

    @Override
    public void collapse() {
        getParent().getChildren().remove(this);
    }

    @Override
    public void expand() {
        getParent().getChildren().add(this);
        TabPane pane = tab.getTabPane();
        pane.getSelectionModel().select(tab);
    }
}
