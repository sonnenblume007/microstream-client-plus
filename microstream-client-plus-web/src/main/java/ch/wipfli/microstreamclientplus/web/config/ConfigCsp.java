package ch.wipfli.microstreamclientplus.web.config;

import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.csp.CSPDirectiveSrcValue;
import org.apache.wicket.protocol.http.WebApplication;

import com.giffing.wicket.spring.boot.context.extensions.ApplicationInitExtension;
import com.giffing.wicket.spring.boot.context.extensions.WicketApplicationInitConfiguration;

@ApplicationInitExtension
public class ConfigCsp implements WicketApplicationInitConfiguration {

    @Override
    public void init(WebApplication webApplication) {
        webApplication.getCspSettings().blocking().clear()
                .add(CSPDirective.DEFAULT_SRC, CSPDirectiveSrcValue.SELF)
                .add(CSPDirective.STYLE_SRC, CSPDirectiveSrcValue.SELF, CSPDirectiveSrcValue.UNSAFE_INLINE)
                .add(CSPDirective.SCRIPT_SRC, CSPDirectiveSrcValue.SELF, CSPDirectiveSrcValue.UNSAFE_EVAL, CSPDirectiveSrcValue.UNSAFE_INLINE)
                .add(CSPDirective.IMG_SRC, CSPDirectiveSrcValue.SELF)
                .add(CSPDirective.IMG_SRC, "self", "data:")
                .add(CSPDirective.FONT_SRC, CSPDirectiveSrcValue.SELF)
                .add(CSPDirective.FONT_SRC, "self", "data:")
                .add(CSPDirective.CONNECT_SRC, CSPDirectiveSrcValue.WILDCARD);
    }
}
