package ch.wipfli.microstreamclientplus.web.config;

import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.markup.html.WebPage;
import org.springframework.stereotype.Component;

import com.giffing.wicket.spring.boot.starter.app.WicketBootSecuredWebApplication;

import ch.wipfli.microstreamclientplus.web.helper.BasicAuthenticationSession;
import ch.wipfli.microstreamclientplus.web.pages.LoginPage;

@Component
public class WicketWebApplication extends WicketBootSecuredWebApplication {
    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected Class<? extends AbstractAuthenticatedWebSession> getWebSessionClass() {
        return BasicAuthenticationSession.class;
    }

    @Override
    protected Class<? extends WebPage> getSignInPageClass() {
        return LoginPage.class;
    }
}
