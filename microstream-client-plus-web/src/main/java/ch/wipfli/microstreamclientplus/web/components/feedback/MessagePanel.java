package ch.wipfli.microstreamclientplus.web.components.feedback;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.util.ListModel;

public class MessagePanel extends Panel {
    public MessagePanel(String id, MessageType messageType) {
        super(id, new ListModel<String>(new ArrayList<>()));
        setOutputMarkupId(true);
        final WebMarkupContainer feedbackBox = new WebMarkupContainer("feedbackBox") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!getList().isEmpty());
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.remove("class");
                tag.put("class", messageType.getValue());
            }
        };
        final Label feedbackMessage = new Label("feedbackMessage", () -> {
            final String output = String.join("<br>", getList());
            clear();
            return output;
        });
        feedbackMessage.setEscapeModelStrings(false);
        feedbackBox.add(feedbackMessage);
        add(feedbackBox);
    }

    public void addError(String error) {
        getList().add(error);
    }

    public void clear() {
        getList().clear();
    }

    private List<String> getList() {
        return (List<String>) getDefaultModel().getObject();
    }

    public enum MessageType {
        ERROR("alert alert-danger"),
        INFO("alert alert-info");

        private final String value;

        MessageType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
