package impl.org.controlsfx.skin;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.controlsfx.control.ValidationPane;
import org.controlsfx.control.action.Action;
import org.controlsfx.validation.ValidationMessage;

import java.util.List;

public class ValidationPaneSkin extends NotificationPaneSkin<ValidationPane> {

    public ValidationPaneSkin(final ValidationPane control) {
        super(control);
    }

    @Override
    protected void registerListeners(ValidationPane control) {
        super.registerListeners(control);

        control.messagesProperty().addListener(new ListChangeListener<ValidationMessage>() {
            @Override
            public void onChanged(Change<? extends ValidationMessage> c) {
                while (c.next()) {
                    if (c.wasPermutated()) {

                    } else if (c.wasUpdated()) {
                        //update item
                    } else if(c.getRemovedSize() > 0){
                        ((ValidationBar)notificationBar).clearMesages();
                    } else {
                        ((ValidationBar)notificationBar).updateMessageList();
                    }
                }
            }
        });
    }

    @Override
    protected void createBar(ValidationPane control) {
        notificationBar = new ValidationBar() {
            @Override
            public List<ValidationMessage> getMessages() {
                return control.messagesProperty().get();
            }

            @Override public void requestContainerLayout() {
                control.requestLayout();
            }

            @Override public String getText() {
                return null;
            }

            @Override public Node getGraphic() {
                return control.getGraphic();
            }

            @Override public ObservableList<Action> getActions() {
                return control.getActions();
            }

            @Override public boolean isShowing() {
                return control.isShowing();
            }

            @Override public boolean isShowFromTop() {
                return control.isShowFromTop();
            }

            @Override public void hide() {
                control.hide();
            }

            @Override public boolean isCloseButtonVisible() {
                return control.isCloseButtonVisible();
            }

            @Override public double getContainerHeight() {
                return control.getHeight();
            }

            @Override public void relocateInParent(double x, double y) {
                notificationBar.relocate(x, y);
            }
        };
    }
}
