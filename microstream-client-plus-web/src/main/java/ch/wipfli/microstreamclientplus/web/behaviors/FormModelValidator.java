
package ch.wipfli.microstreamclientplus.web.behaviors;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.IFormValidator;

import ch.wipfli.microstreamclientplus.web.helper.ValidationUtil;

public class FormModelValidator implements IFormValidator {
    @Override
    public FormComponent<?>[] getDependentFormComponents() {
        return new FormComponent[0];
    }

    @Override
    public void validate(Form<?> form) {
        form.streamChildren().forEach(component -> {
            ValidationUtil.valid(component, null);
        });
    }
}
