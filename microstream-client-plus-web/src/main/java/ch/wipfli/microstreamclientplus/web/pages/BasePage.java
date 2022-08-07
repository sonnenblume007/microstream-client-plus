package ch.wipfli.microstreamclientplus.web.pages;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.string.StringValue;

import ch.wipfli.microstreamclientplus.web.helper.AbsoluteResourceReference;

public abstract class BasePage extends WebPage {

    public BasePage() {

        final WebMarkupContainer header = new WebMarkupContainer("header");
        header.add(new Behavior() {
            @Override
            public void onConfigure(Component component) {
                super.onConfigure(component);
                final StringValue variable = getPage().getPageParameters().get("navbar");
                if (!variable.isEmpty() && variable.toString().equals("false")) {
                    component.setVisible(false);
                }
            }
        });
        add(header);

        final AjaxLink<Void> settings = new AjaxLink<>("settings") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(SettingPage.class);
            }
        };
        header.add(settings);

        final AjaxLink<Void> exit = new AjaxLink<>("exit") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                target.appendJavaScript("document.close();");
            }
        };
        header.add(exit);

    }

    @Override
    public void renderHead(IHeaderResponse response) {
        PackageResourceReference jsFile = new AbsoluteResourceReference("public/scripts.js");
        JavaScriptHeaderItem headerJsFile = JavaScriptHeaderItem.forReference(jsFile);
        response.render(headerJsFile);

        PackageResourceReference cssFile = new AbsoluteResourceReference("public/scripts.css");
        CssHeaderItem headercssFile = CssHeaderItem.forReference(cssFile);
        response.render(headercssFile);
    }
}
