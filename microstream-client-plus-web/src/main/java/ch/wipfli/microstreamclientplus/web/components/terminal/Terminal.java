package ch.wipfli.microstreamclientplus.web.components.terminal;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ch.wipfli.microstreamclientplus.web.helper.CallFromJavascriptBehavior;
import ch.wipfli.microstreamclientplus.web.helper.JavaScriptListener;

public class Terminal extends Panel {

    private final InvokeListener invokeListener;
    private final SuggestListener suggestListener;
    private final WebMarkupContainer output;
    private final WebMarkupContainer editor;
    private final AjaxLink<Void> invokeButton;

    @SpringBean
    private JShellService jShellService;

    public Terminal(String id) {
        super(id);
        setOutputMarkupId(true);
        add(new CallFromJavascriptBehavior());
        this.suggestListener = new SuggestListener();
        this.suggestListener.setDisableCache(true);
        this.invokeListener = new InvokeListener();
        this.invokeListener.setDisableCache(true);

        editor = new WebMarkupContainer("editor");
        editor.setOutputMarkupId(true);
        add(editor);

        output = new WebMarkupContainer("output");
        output.setOutputMarkupId(true);
        add(output);

        invokeButton = new AjaxLink<>("invokeButton") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                invoke(target);
            }
        };
        add(invokeButton);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(OnLoadHeaderItem.forScript("document.buildConsole(\"" + editor.getMarkupId() + "\", \"" + output.getMarkupId() + "\")"));
    }

    public void invoke(AjaxRequestTarget ajaxRequestTarget) {
        final String code = "document.monaco.invoke()";
        ajaxRequestTarget.appendJavaScript(code);
    }

    public final class InvokeListener extends JavaScriptListener<JShellService.Result> {

        @Override
        public String getPath() {
            return "/terminal/invoke";
        }

        @Override
        protected JShellService.Result listener(List<INamedParameters.NamedPair> parameter) {
            final String value = parameter.get(0).getValue();
            final String content = URLDecoder.decode(value, StandardCharsets.UTF_8);
            final JShellService.Result result = jShellService.invokeCode(content);
            System.out.println("response /terminal/invoke");
            return result;
        }
    }

    private final class SuggestListener extends JavaScriptListener<List<SuggestListener.Suggest>> {

        @Override
        public String getPath() {
            return "/terminal/suggest";
        }

        @Override
        protected List<Suggest> listener(List<INamedParameters.NamedPair> parameter) {
            final String value = parameter.get(0).getValue();
            final String content = URLDecoder.decode(value, StandardCharsets.UTF_8);
            final List<String> suggestions = jShellService.suggest(content);
            final List<Suggest> suggestList = suggestions.stream()
                .map(f -> {
                    final Suggest s = new Suggest();
                    s.label = f;
                    s.insertText = f;
                    return s;
                })
                .collect(Collectors.toList());

            return suggestList;
        }

        private class Range {
            public int startLineNumber;
            public int endLineNumber;
            public int startColumn;
            public int endColumn;
        }

        private class Suggest {
            public String label;
            public String kind;
            public String documentation;
            public String insertText;
            public String insertTextRules;
            public Range range;
        }
    }
}
