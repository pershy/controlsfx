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
package org.controlsfx.dialog;

import impl.org.controlsfx.ImplUtils;
import impl.org.controlsfx.i18n.Localization;

import java.util.HashMap;
import java.util.List;

import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.image.ImageView;

import org.controlsfx.control.ButtonBar.ButtonType;
import org.controlsfx.control.action.Action;
import org.controlsfx.tools.ValueExtractor;

public class Wizard {
    
    
    /**************************************************************************
     * 
     * Static fields
     * 
     **************************************************************************/
    
    public static final Action ACTION_FINISH = new DialogAction(Localization.asKey("wizard.finish.button"), ButtonType.FINISH, false, true, true) { //$NON-NLS-1$
        { lock(); }
        public String toString() { return "Wizard.ACTION_FINISH";} //$NON-NLS-1$
    };
    
    
    
    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    private Dialog dialog;
    
    private Object owner;
    private String title;    
    
    private int previousPageIndex = 0;
    private int currentPageIndex = 0;
    
    private final ObservableList<WizardPage> pages = FXCollections.observableArrayList();
    private final ObservableMap<String, Object> settings = FXCollections.observableHashMap();
    
    // TODO these should be public static actions
    private final Action ACTION_PREVIOUS = new DialogAction(Localization.asKey("wizard.previous.button"), ButtonType.BACK_PREVIOUS, false, false, false) { //$NON-NLS-1$
        @Override public void handle(ActionEvent ae) {
            previousPageIndex = currentPageIndex;
            currentPageIndex--;
            validateCurrentPageIndex();
            updatePage(dialog);
            validateActionState();
        }
    };
    private final Action ACTION_NEXT = new DialogAction(Localization.asKey("wizard.next.button"), ButtonType.NEXT_FORWARD, false, false, true) { //$NON-NLS-1$
        @Override public void handle(ActionEvent ae) {
            previousPageIndex = currentPageIndex;
            currentPageIndex++;
            validateCurrentPageIndex();
            updatePage(dialog);
            validateActionState();
        }
    };
    
    
    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    /**
     * 
     */
    public Wizard() {
        this(null);
    }
    
    /**
     * 
     * @param owner
     */
    public Wizard(Object owner) {
        this(owner, "");
    }
    
    /**
     * 
     * @param owner
     * @param title
     */
    public Wizard(Object owner, String title) {
        this.owner = owner;
        this.title = title;
    }
    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    
    // --- pages
    public final ObservableList<WizardPage> getPages() {
        return pages;
    }
    
    
    // --- settings
    public final ObservableMap<String, Object> getSettings() {
        return settings;
    }
    
    public Action show() {
        dialog = new Dialog(owner, title);
        dialog.getActions().setAll(ACTION_PREVIOUS, ACTION_NEXT, ACTION_FINISH, Dialog.ACTION_CANCEL);
        
        updatePage(dialog);
        validateActionState();
        
        // --- show the wizard!
        return dialog.show();
    }
    
    
    
    /**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/
    
    
    // --- Properties
    private static final Object USER_DATA_KEY = new Object();
    
    // A map containing a set of properties for this Wizard
    private ObservableMap<Object, Object> properties;

    /**
      * Returns an observable map of properties on this Wizard for use primarily
      * by application developers.
      *
      * @return an observable map of properties on this Wizard for use primarily
      * by application developers
     */
     public final ObservableMap<Object, Object> getProperties() {
        if (properties == null) {
            properties = FXCollections.observableMap(new HashMap<Object, Object>());
        }
        return properties;
    }
    
    /**
     * Tests if this Wizard has properties.
     * @return true if this Wizard has properties.
     */
     public boolean hasProperties() {
        return properties != null && !properties.isEmpty();
    }

     
    // --- UserData
    /**
     * Convenience method for setting a single Object property that can be
     * retrieved at a later date. This is functionally equivalent to calling
     * the getProperties().put(Object key, Object value) method. This can later
     * be retrieved by calling {@link Wizard#getUserData()}.
     *
     * @param value The value to be stored - this can later be retrieved by calling
     *          {@link Wizard#getUserData()}.
     */
    public void setUserData(Object value) {
        getProperties().put(USER_DATA_KEY, value);
    }

    /**
     * Returns a previously set Object property, or null if no such property
     * has been set using the {@link Wizard#setUserData(java.lang.Object)} method.
     *
     * @return The Object that was previously set, or null if no property
     *          has been set or if null was set.
     */
    public Object getUserData() {
        return getProperties().get(USER_DATA_KEY);
    }
    
    
    
    /**************************************************************************
     * 
     * Private implementation
     * 
     **************************************************************************/
    
    private void updatePage(Dialog dialog) {
        WizardPage previousPage;
        WizardPage newPage;
        
        final boolean goingForward = currentPageIndex > previousPageIndex;
        
        if (previousPageIndex >= 0 && previousPageIndex < getPages().size()) {
            previousPage = getPages().get(previousPageIndex);
            
            // if we are going forward in the wizard, we read in the settings 
            // from the page and store them in the settings map.
            // If we are going backwards, we do nothing
            if (goingForward) {
                readSettings(previousPage);
            }
            
            // give the previous wizard page a chance to update the pages list
            // based on the settings it has received
            if (previousPage != null) {
                previousPage.onExitingPage(this);
            }
        }
        
        // we get the previous page (i.e. the one we were just on before the
        // previous / next button was clicked), and we ask it what page
        // should be previous / next. Then we look up that index and record
        // that for next time this method is called.
        final boolean firstCall = currentPageIndex == 0 && previousPageIndex == 0;
        
        WizardPage currentPage = getPages().get(currentPageIndex);
        if (firstCall) {
            newPage = currentPage;
        } else {
            // now that the pages are updated, we need to re-find the current page
            // so that we know its new index.
            // then we update the page index to be based off the new index
            int newPageIndex = getPages().indexOf(currentPage);
            
            currentPageIndex = newPageIndex;
    
            // and we go get that page
            newPage = getPages().get(newPageIndex);
        }
        
        if (newPage == null) {
            dialog.setContent((Node)null);
        } else {
            newPage.onEnteringPage(this);
            
            dialog.setMasthead(newPage.getMasthead());
            dialog.setGraphic(newPage.getGraphic());
            
            Node content = newPage.getContent();
            dialog.setContent(content);
        }
    }
    
    private void validateCurrentPageIndex() {
        final int pageCount = getPages().size();
    
        if (currentPageIndex < 0) {
            currentPageIndex = 0;
        } else if (currentPageIndex > pageCount - 1) {
            currentPageIndex = pageCount - 1;
        }
    }
    
    private void validateActionState() {
        final int pageCount = getPages().size();
        final boolean atEndOfWizard = currentPageIndex == pageCount - 1;
        final List<Action> actions = dialog.getActions();
        
        ACTION_PREVIOUS.setDisabled(currentPageIndex == 0);
        
        // Note that we put the 'next' and 'finish' actions at the beginning of 
        // the actions list, so that it takes precedence as the default button, 
        // over, say, cancel. We will probably want to handle this better in the
        // future...
        
        if (atEndOfWizard) {
            actions.remove(ACTION_NEXT);
            
            actions.add(0, ACTION_FINISH);
        } else {
            if (! actions.contains(ACTION_NEXT)) {
                actions.add(0, ACTION_NEXT);
            }
            actions.remove(ACTION_FINISH);
        }
        
        // remove actions from the previous page
        WizardPage previousPage = getPages().get(previousPageIndex);
        actions.removeAll(previousPage.getActions());
        
        // add in the actions for the new page
        WizardPage currentPage = getPages().get(currentPageIndex);
        actions.addAll(currentPage.getActions());
    }
    
    private int settingCounter;
    private void readSettings(WizardPage page) {
        // for now we cannot know the structure of the page, so we just drill down
        // through the entire scenegraph (from page.content down) until we get
        // to the leaf nodes. We stop only if we find a node that is a
        // ValueContainer (either by implementing the interface), or being 
        // listed in the internal valueContainers map.
        
        settingCounter = 0;
        checkNode(page.getContent());
    }
    
    private boolean checkNode(Node n) {
        boolean success = readSetting(n);
        
        if (success) {
            // we've added the setting to the settings map and we should stop drilling deeper
            return true;
        } else {
            // go into children of this node (if possible) and see if we can get
            // a value from them (recursively)
            List<Node> children = ImplUtils.getChildren(n, false);
            
            // we're doing a depth-first search, where we stop drilling down
            // once we hit a successful read
            boolean childSuccess = false;
            for (Node child : children) {
                childSuccess |= checkNode(child);
            }
            return childSuccess;
        }
    }
    
    private boolean readSetting(Node n) {
        if (n == null) {
            return false;
        }
        
        Object setting = ValueExtractor.getValue(n);
        
        if (setting != null) {
            // save it into the settings map.
            // if the node has an id set, we will use that as the setting name
            String settingName = n.getId();
            
            // but if the id is not set, we will use a generic naming scheme
            if (settingName == null || settingName.isEmpty()) {
                settingName = "page_" + previousPageIndex + ".setting_" + settingCounter; 
            }
            
            getSettings().put(settingName, setting);
            
            settingCounter++;
        }
        
        return setting != null;
    }
    
    
    
    /**************************************************************************
     * 
     * Support classes
     * 
     **************************************************************************/
    
    /**
     * 
     */
    // TODO this should just contain a ControlsFX Form, but for now it is hand-coded
    public static class WizardPage {
        private final ObservableList<Action> actions = FXCollections.observableArrayList();

        private Node content;
        private String masthead;
        private Node graphic;
        
        public WizardPage() {
            this(null);
        }
        
        public WizardPage(@NamedArg("content") Node content) {
            this(content, null);
        }
        
        public WizardPage(@NamedArg("content") Node content, 
                         @NamedArg("masthead") String masthead) {
            this(content, masthead, null);
        }
        
        public WizardPage(@NamedArg("content") Node content, 
                          @NamedArg("masthead") String masthead,
                          @NamedArg("graphic") Node graphic) {
           this.content = content;
           this.masthead = masthead;
           this.graphic = graphic != null ? graphic : new ImageView(DialogResources.getImage("confirm.image"));
        }
        
        public final Node getContent() {
            return content;
        }
        
        public final ObservableList<Action> getActions() {
            return actions;
        }

        // TODO we want to change this to an event-based API eventually
        public void onEnteringPage(Wizard wizard) {
            
        }
        
        // TODO same here - replace with events
        public void onExitingPage(Wizard wizard) {
            
        }
        
        public void setGraphic(Node graphic) {
            this.graphic = graphic;
        }
        
        public final Node getGraphic() {
            return graphic;
        }
        
        public void setMasthead(String masthead) {
            this.masthead = masthead;
        }
        
        public final String getMasthead() {
            return masthead;
        }
    }
}
