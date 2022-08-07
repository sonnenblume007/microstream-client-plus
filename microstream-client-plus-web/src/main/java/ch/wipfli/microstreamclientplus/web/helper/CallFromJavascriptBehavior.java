package ch.wipfli.microstreamclientplus.web.helper;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.StringValue;

public class CallFromJavascriptBehavior extends AbstractDefaultAjaxBehavior {
        @Override
        protected void respond(AjaxRequestTarget target) {
            final StringValue parameterValue = RequestCycle.get().getRequest().getQueryParameters().getParameterValue("yourName");
            System.out.println(String.format("Hello %s", parameterValue.toString()));
        }

        @Override
        public void renderHead(Component component, IHeaderResponse response) {
            super.renderHead(component, response);
            response.render(OnLoadHeaderItem.forScript(String.format("nameOfFunction=%s", getCallbackFunction(CallbackParameter.explicit("yourName")))));
        }
    }
