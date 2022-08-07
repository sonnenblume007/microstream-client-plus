package ch.wipfli.microstreamclientplus.web.helper;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;

public class BasicAuthenticationSession extends AuthenticatedWebSession {

        public BasicAuthenticationSession(Request request) {
                super(request);
        }

        @Override
        public boolean authenticate(String username, String password) {
              //user is authenticated if both username and password are equal to 'wicketer'
                return true;
                //return username.equals("") && password.equals("");
        }



        @Override
        public Roles getRoles() {
                return new Roles();
        }
}
