package ch.wipfli.microstreamclientplus.web.behaviors;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;

public class AjaxOnSubmitBehavior extends AjaxFormSubmitBehavior {
    public AjaxOnSubmitBehavior() {
        super("keyup");
    }

    @Override
    protected void onSubmit(AjaxRequestTarget target) {
        super.onSubmit(target);
    }
}
