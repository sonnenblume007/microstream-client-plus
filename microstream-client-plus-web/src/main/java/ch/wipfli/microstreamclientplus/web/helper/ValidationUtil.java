package ch.wipfli.microstreamclientplus.web.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.PropertyModel;

public class ValidationUtil {

    public static boolean valid(Form<?> form, AjaxRequestTarget target)
    {
        final List<Boolean> booleanList = new ArrayList<>();
        form.streamChildren().forEach(component -> {
            booleanList.add(ValidationUtil.valid(component, target));
        });

        return booleanList.stream().reduce(true, (aBoolean, aBoolean2) -> aBoolean && aBoolean2);
    }

    public static boolean valid(Component component, AjaxRequestTarget target) {
        if(component instanceof AbstractTextComponent) {
            final PropertyModel<?> model = (PropertyModel<?>) component.getDefaultModel();
            final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            final Validator validator = factory.getValidator();

            final String expression = model.getPropertyExpression();
            final Object targetModel = model.getTarget();
            final Set<ConstraintViolation<Object>> result = validator.validateProperty(targetModel, expression);
            for (ConstraintViolation<Object> item : result) {
                final String template = item.getMessageTemplate().replace("{", "").replace("}", "");
                String errorMessage = component.getPage().getString(template);
                final Collection<Object> attributes = item.getConstraintDescriptor().getAttributes().values();
                for (int i = 0; i < attributes.size(); i++) {
                    errorMessage = errorMessage.replace("{" + i + "}", attributes.toArray(new Object[0])[i].toString());
                }
                component.error(errorMessage);
            }

            if (!result.isEmpty() && target != null) {
                target.add(component);
            }
            return result.isEmpty();
        }
        return true;
    }
}
