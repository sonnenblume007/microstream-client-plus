package ch.wipfli.microstreamclientplus.web.pages;

import static ch.wipfli.microstreamclientplus.web.components.feedback.MessagePanel.MessageType.ERROR;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;

import ch.wipfli.microstreamclientplus.web.components.feedback.MessagePanel;
import ch.wipfli.microstreamclientplus.web.helper.ValidationUtil;
import ch.wipfli.microstreamclientplus.web.behaviors.AjaxValidationBehavior;
import ch.wipfli.microstreamclientplus.web.behaviors.FormModelValidator;
import ch.wipfli.microstreamclientplus.web.models.LoginModel;

public class LoginPage extends BasePage {

    public LoginPage() {

        final LoginModel loginModel = new LoginModel();

        final Form<Void> form = new Form<>("form");
        form.add(new FormModelValidator());

        final MessagePanel feedbackPanel = new MessagePanel("feedbackMessage", ERROR);
        form.add(feedbackPanel);

        final AjaxSubmitLink submitButton = new AjaxSubmitLink("submit") {

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                if (ValidationUtil.valid(form, target)) {
                    target.add(form);
                    if (!AuthenticatedWebSession.get().signIn(loginModel.getUsername(), loginModel.getPassword())) {
                        feedbackPanel.addError(getString("validation.login.usernamepassword"));
                    }
                    else {
                        //setResponsePage(new SecuredPage());
                    }
                }
                target.add(feedbackPanel);
            }
        };
        form.add(submitButton);
        form.setDefaultButton(submitButton);

        final TextField<String> username = new TextField<>("username", PropertyModel.of(loginModel, LoginModel.Fields.username));
        username.add(new AjaxValidationBehavior<>());
        form.add(username);
        final PasswordTextField password = new PasswordTextField("password", PropertyModel.of(loginModel, LoginModel.Fields.password));
        password.add(new AjaxValidationBehavior<>());
        password.setRequired(false);
        password.setResetPassword(false);
        form.add(password);

        add(form);
    }
}
