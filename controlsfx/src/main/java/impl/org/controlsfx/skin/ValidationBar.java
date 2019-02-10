package impl.org.controlsfx.skin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationMessage;

import java.util.List;

public abstract class ValidationBar extends NotificationBar {

    private static final Color WARN_COLOR = Color.web("#CF6F1E");
    private static final Color ERROR_COLOR = Color.web("#B94B4B");

    static {
//        GlyphFontRegistry.register("icomoon", ValidationBar.class.getResourceAsStream("icomoon.ttf") , 16);
//        Color.rgb(150,150,150, 0.8);
//        GridPane.setMargin(ERROR_ICON, new Insets(0, 8, 0, 16));
//        GridPane.setMargin(WARNING_FONT, new Insets(0, 8, 0, 16));
//        ERROR_ICON.setAlignment(Pos.CENTER);
//        WARNING_FONT.setAlignment(Pos.CENTER);
//        ERROR_ICON.setStyle(SHADOW_EFFECT);
    }

    VBox messagePane;

    public abstract List<ValidationMessage> getMessages();

    public ValidationBar() {
        super();
        messagePane.setPadding(new Insets(0));
        messagePane.setAlignment(Pos.CENTER_LEFT);
        messagePane.opacityProperty().bind(transition);
//        messagePane.setGridLinesVisible(true);
        label.setGraphicTextGap(16);
    }

    @Override
    void updatePane() {
        messagePane = new VBox();
        actionsBar = ActionUtils.createButtonBar(getActions());
        actionsBar.opacityProperty().bind(transition);
        GridPane.setHgrow(actionsBar, Priority.SOMETIMES);
        GridPane.setHgrow(messagePane, Priority.SOMETIMES);
        pane.getChildren().clear();

        int row = 0;

        if (title != null) {
            pane.add(title, 0, row++);
        }

        updateMessageList();
        pane.add(label, 0, row);
        pane.add(messagePane, 1, row);
        pane.add(actionsBar, 2, row);

        if (isCloseButtonVisible()) {
            pane.add(closeBtn, 3, 0, 1, row+1);
        }
    }

    void clearMesages() {
        messagePane.getChildren().clear();
    }

    void updateMessageList() {
        List<ValidationMessage> messages = getMessages();
        if(messages != null && !messages.isEmpty()) {
            for (int i = 0; i < messages.size(); i++) {
                ValidationMessage message =  messages.get(i);
                Label text = new Label(message.getText());
                GridPane.setValignment(text, VPos.CENTER);
                GridPane.setMargin(text, new Insets(0,8,0,8));
                text.setGraphicTextGap(8);
                if(Severity.ERROR == message.getSeverity()) {
                    text.setTextFill(ERROR_COLOR);
                    Glyph icon = new Glyph("FontAwesome", FontAwesome.Glyph.TIMES).color(ERROR_COLOR).size(16);
                    icon.setAlignment(Pos.BASELINE_CENTER);
                    icon.setMinWidth(20);
                    text.setGraphic(icon);
                    messagePane.getChildren().add(text);
                } else if(Severity.WARNING == message.getSeverity()) {
                    text.setTextFill(WARN_COLOR);
                    Glyph icon = new Glyph("FontAwesome", FontAwesome.Glyph.EXCLAMATION).color(WARN_COLOR).size(16);
                    icon.setMinWidth(20);
                    icon.setAlignment(Pos.BASELINE_CENTER);
                    text.setGraphic(icon);
                    messagePane.getChildren().add(text);
                }
            }
        }
    }
}
