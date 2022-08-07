package ch.wipfli.microstreamclientplus.web.pages;

import com.giffing.wicket.spring.boot.context.scan.WicketHomePage;

import ch.wipfli.microstreamclientplus.web.components.terminal.Terminal;

@WicketHomePage
public class TerminalPage extends BasePage {

    public TerminalPage() {
        final Terminal terminal = new Terminal("terminal");
        add(terminal);
    }
}
