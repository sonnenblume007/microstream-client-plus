package ch.wipfli.microstreamclientplus.web.behaviors;

import static ch.wipfli.microstreamclientplus.web.helper.ValidationUtil.valid;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;

public class AjaxValidationBehavior<T> extends AjaxFormComponentUpdatingBehavior {

    public AjaxValidationBehavior() {
        super("change");
    }

    @Override
    protected void onUpdate(AjaxRequestTarget target) {
        valid(getComponent(), target);
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);

        if(component.hasErrorMessage()) {
            final String componentId = component.getMarkupId();
            final String feedbackId = componentId + "-feedback";
            final List<String> errorList = new ArrayList<>(new LinkedHashSet<>(component.getFeedbackMessages().toList().stream()
                    .map(FeedbackMessage::getMessage)
                    .map(Object::toString)
                    .collect(Collectors.toList())
            ));
            final String errors = String.join("<br>", errorList);

            final String div = "<div id=\"" + feedbackId + "\" class=\"invalid-feedback\">" + errors + "</div>";

            String removeFeedbackScript = "$('#" + feedbackId + "').remove();";
            String appendFeedbackscript = "$('#" + componentId + "').after('" + div + "');";
            response.render(OnLoadHeaderItem.forScript(removeFeedbackScript + appendFeedbackscript));
        }
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        if(getComponent().hasErrorMessage()) {
            tag.append("class", "is-invalid", " ");
        }
    }
}
