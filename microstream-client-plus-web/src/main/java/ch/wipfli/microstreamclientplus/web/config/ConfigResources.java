
package ch.wipfli.microstreamclientplus.web.config;

import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.protocol.http.WebApplication;
import org.springframework.web.context.request.RequestContextListener;

import com.giffing.wicket.spring.boot.context.extensions.ApplicationInitExtension;
import com.giffing.wicket.spring.boot.context.extensions.WicketApplicationInitConfiguration;

import ch.wipfli.microstreamclientplus.web.helper.AbsoluteResourceReference;
import ch.wipfli.microstreamclientplus.web.pages.SettingPage;
import ch.wipfli.microstreamclientplus.web.pages.TerminalPage;

@ApplicationInitExtension
public class ConfigResources implements WicketApplicationInitConfiguration {

    @Override
    public void init(WebApplication webApplication) {
        final String[] allowPattern = {"+*.pdf"};
        final String[] resources = {
            "public/editor.worker.js",
            "public/node_modules_monaco-editor_esm_vs_basic-languages_java_java_js.js"

        };

        webApplication.getServletContext().addListener(new RequestContextListener());
        webApplication.mountPage("/terminal", TerminalPage.class);
        webApplication.mountPage("/settings", SettingPage.class);

        allowGuardPatter(webApplication, allowPattern);
        mountResources(webApplication, resources);
    }

    private void mountResources(WebApplication webApplication, String[] values) {
        for (String item : values) {
            webApplication.mountResource(item, new AbsoluteResourceReference(item));
        }
    }

    private void allowGuardPatter(WebApplication webApplication, String[] values) {
        final IPackageResourceGuard packageResourceGuard = webApplication.getResourceSettings().getPackageResourceGuard();
        if (packageResourceGuard instanceof SecurePackageResourceGuard) {
            SecurePackageResourceGuard guard = (SecurePackageResourceGuard) packageResourceGuard;
            for (String item : values) {
                guard.addPattern(item);
            }
        }
    }
}
