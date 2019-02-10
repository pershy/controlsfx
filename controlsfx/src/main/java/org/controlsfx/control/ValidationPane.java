package org.controlsfx.control;

import impl.org.controlsfx.skin.ValidationPaneSkin;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import org.controlsfx.validation.ValidationMessage;

import java.util.Collections;
import java.util.List;

public class ValidationPane extends NotificationPane {

    private final ListProperty<ValidationMessage> messages = new SimpleListProperty<>(FXCollections.observableArrayList());

    public ValidationPane() {
        super(null);
    }

    public ValidationPane(Node content) {
        super(content);
    }

    @Override protected Skin<?> createDefaultSkin() {
        return new ValidationPaneSkin(this);
    }


    public void setMessages(List<ValidationMessage> messages) {
        this.messages.clear();
        Collections.sort(messages);
        this.messages.addAll(messages);
        if(!messages.isEmpty()) {
            show();
        }
    }

    public List<ValidationMessage> getMessages() {
        return messages.get();
    }

    public ListProperty<ValidationMessage> messagesProperty() {
        return messages;
    }
}
