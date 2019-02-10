package org.controlsfx.control;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.validation.ValidationMessage;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TestValidationPane extends Application  {

    @Override
    public void start( Stage stage ) throws Exception {


        List<ValidationMessage> messages = new ArrayList<>();
        messages.add(ValidationMessage.error(null, "Blad wykonywania aplikacji"));
        messages.add(ValidationMessage.warning(null, "Nie podano numeru PESEL"));
        messages.add(ValidationMessage.warning(null, "Brak adresu"));
        messages.add(ValidationMessage.error(null, "Nie podano daty urodzenia"));

        VBox content = new VBox();
        for (ValidationMessage message : messages) {
            content.getChildren().add(new Label(message.getText()));
        }
        ValidationPane pane = new ValidationPane(content);

        VBox vbox = new VBox();

        Button b = new Button("show");
        b.setOnAction( s -> pane.show());

        Button c = new Button("set Messages");
        c.setOnAction( s -> pane.setMessages(messages));

        Button d = new Button("add Warning");
        d.setOnAction( s -> messages.add(ValidationMessage.warning(c, "W: " + LocalTime.now())));

        Button clearButton = new Button("Clear all");
        clearButton.setOnAction( s -> pane.messagesProperty().clear());

        Button statusButton = new Button("Status");
        statusButton.setOnAction( s -> pane.getMessages());

        vbox.getChildren().addAll(b, c, d, clearButton, statusButton);

        pane.setContent(vbox);
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(300);
        stage.setTitle("Validation Pane" + LocalTime.now());
//        pane.show();
        stage.show();
    }


    public static void main( String[] args ) {
        launch( args );
    }
}
