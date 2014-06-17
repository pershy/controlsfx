package org.controlsfx.dialog;

import java.util.HashMap;
import java.util.List;

import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.scene.Node;

import org.controlsfx.control.ButtonBar.ButtonType;
import org.controlsfx.control.action.Action;

public class Wizard {
    
    private Dialog dialog;
    
    private int previousPageIndex = 0;
    private int currentPageIndex = 0;
    
    private final ObservableList<WizardPage> pages = FXCollections.observableArrayList();
    
    private final Action ACTION_PREVIOUS = new DialogAction(/*Localization.asKey("wizard.previous.button")*/"Previous", ButtonType.BACK_PREVIOUS, false, false, false) { //$NON-NLS-1$
        @Override public void handle(ActionEvent ae) {
            previousPageIndex = currentPageIndex;
            currentPageIndex--;
            validateCurrentPageIndex();
            updatePage(dialog);
            validateActionState();
        }
    };
    private final Action ACTION_NEXT = new DialogAction(/*Localization.asKey("wizard.next.button")*/"Next", ButtonType.NEXT_FORWARD, false, false, true) { //$NON-NLS-1$
        @Override public void handle(ActionEvent ae) {
            previousPageIndex = currentPageIndex;
            currentPageIndex++;
            validateCurrentPageIndex();
            updatePage(dialog);
            validateActionState();
        }
    };
    private final Action ACTION_FINISH = new DialogAction(/*Localization.asKey("wizard.finish.button")*/"Finish", ButtonType.FINISH, false, false, true) { //$NON-NLS-1$
        @Override public void handle(ActionEvent ae) {
            // TODO
            System.out.println("Finish");
        }
    };
    
    
    // --- pages
    public final ObservableList<WizardPage> getPages() {
        return pages;
    }
    
    
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
    
    
    
    public void show() {
        dialog = new Dialog(null, "WIZARD POWER!");
        dialog.setMasthead("Masthead text");
        dialog.getActions().setAll(ACTION_PREVIOUS, ACTION_NEXT, ACTION_FINISH, Dialog.ACTION_CANCEL);
        
        updatePage(dialog);
        validateActionState();
        
        // --- show the wizard!
        dialog.show();
    }
    
    private void updatePage(Dialog dialog) {
        WizardPage previousPage;
        WizardPage newPage;
        
        if (previousPageIndex >= 0 && previousPageIndex < getPages().size()) {
            previousPage = getPages().get(previousPageIndex);
            
            if (previousPage != null) {
                previousPage.updatePages(this);
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
        
        dialog.setContent(newPage == null ? null : newPage.getContent());
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
        
        if (atEndOfWizard) {
            actions.remove(ACTION_NEXT);
            actions.add(ACTION_FINISH);
        } else {
            if (! actions.contains(ACTION_NEXT)) {
                actions.add(ACTION_NEXT);
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
    
    
    // TODO this should just contain a ControlsFX Form, but for now it is hand-coded
    public static class WizardPage {
        private Node content;
        private final ObservableList<Action> actions;
        
        public WizardPage() {
            this(null);
        }
        
        public WizardPage(@NamedArg("content") Node content) {
            this(content, (Action[]) null);
        }
        
        public WizardPage(@NamedArg("content") Node content, Action... actions) {
            this.content = content;
            this.actions = actions == null ? 
                    FXCollections.observableArrayList() : FXCollections.observableArrayList(actions);
        }
        
        public Node getContent() {
            return content;
        }
        
        public ObservableList<Action> getActions() {
            return actions;
        }
        
        public void updatePages(Wizard wizard) {
            // no-op
        }
    }
}
