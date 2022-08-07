package ch.wipfli.microstreamclientplus.web.pages;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ch.wipfli.microstreamclientplus.web.behaviors.AjaxOnSubmitBehavior;
import ch.wipfli.microstreamclientplus.web.components.terminal.JShellService;
import ch.wipfli.microstreamclientplus.web.models.Settings;
import ch.wipfli.microstreamclientplus.web.services.SettingsService;

public class SettingPage extends BasePage {

    @SpringBean
    private SettingsService settingsService;

    @SpringBean
    private JShellService shellService;

    public SettingPage() {
        final Settings settings = settingsService.findSettings();
        if (settings.getImports().isEmpty()) {
            settings.getImports().add("");
        }
        if (settings.getClassPaths().isEmpty()) {
            settings.getClassPaths().add("");
        }
        final IModel<Settings> settingsModel = new Model<>(settings);

        final Form<Settings> form = new Form<>("form");
        form.setOutputMarkupId(true);
        add(form);

        //imports
        final ListView<String> importsListView = new ListView<>("imports", PropertyModel.of(settingsModel, Settings.Fields.imports)) {
            @Override
            protected void populateItem(ListItem<String> item) {
                final TextField<String> textField = new TextField<>("import", item.getModel());
                item.add(textField);
                textField.setOutputMarkupId(true);
                textField.add(new AjaxOnSubmitBehavior());
                textField.add(new UpdateTextFieldBehavior(settingsModel, form));
            }
        };
        form.add(importsListView);

        final AjaxLink<Void> addImportButton = new AjaxLink<>("addImport") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                final Settings object = settingsModel.getObject();
                object.getImports().add("");
                target.add(form);
            }
        };
        form.add(addImportButton);

        //classpath
        final ListView<String> classPathListView = new ListView<>("classPaths", PropertyModel.of(settingsModel, Settings.Fields.classPaths)) {
            @Override
            protected void populateItem(ListItem<String> item) {
                final TextField<String> textField = new TextField<>("classPath", item.getModel());
                item.add(textField);
                textField.setOutputMarkupId(true);
                textField.add(new AjaxOnSubmitBehavior());
                textField.add(new UpdateTextFieldBehavior(settingsModel, form));
            }
        };
        form.add(classPathListView);

        final AjaxLink<Void> addClassPathButton = new AjaxLink<>("addClassPath") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                final Settings object = settingsModel.getObject();
                object.getClassPaths().add("");
                target.add(form);
            }
        };
        form.add(addClassPathButton);

        //databaseRootClass
        final TextField<String> databaseRootClass = new TextField<>("databaseRootClass", PropertyModel.of(settingsModel, Settings.Fields.databaseRootClass));
        databaseRootClass.add(new AjaxOnSubmitBehavior());
        form.add(databaseRootClass);

        //databasePath
        final TextField<String> databasePath = new TextField<>("databasePath", PropertyModel.of(settingsModel, Settings.Fields.databasePath));
        databaseRootClass.add(new AjaxOnSubmitBehavior());
        form.add(databasePath);

        //Save
        final AjaxSubmitLink saveButton = new AjaxSubmitLink("save") {
            @Override
            public void onSubmit(AjaxRequestTarget target) {
                settingsModel.getObject().getImports().removeIf(f -> f == null || f.isEmpty());
                settingsModel.getObject().getClassPaths().removeIf(f -> f == null || f.isEmpty());
                settingsService.storeSettings(settingsModel.getObject());
                shellService.initialize();
                setResponsePage(new TerminalPage());
            }
        };
        form.add(saveButton);

        //Cancel
        final AjaxLink<Void> cancelButton = new AjaxLink<>("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(new TerminalPage());
            }
        };
        form.add(cancelButton);
    }

    static class UpdateTextFieldBehavior extends AjaxFormComponentUpdatingBehavior {

        private final IModel<Settings> model;
        private final Component component;

        public UpdateTextFieldBehavior(IModel<Settings> model, Component component) {
            super("keyup");
            this.model = model;
            this.component = component;
        }

        @Override
        protected void onUpdate(AjaxRequestTarget target) {
            final String content = getComponent().getDefaultModelObjectAsString();
            if (content == null || content.isEmpty()) {
                model.getObject().getImports().removeIf(f -> f == null || f.isEmpty());
                if (model.getObject().getImports().isEmpty()) {
                    model.getObject().getImports().add("");
                }
                model.getObject().getClassPaths().removeIf(f -> f == null || f.isEmpty());
                if (model.getObject().getClassPaths().isEmpty()) {
                    model.getObject().getClassPaths().add("");
                }
                target.add(component);
            }
        }
    }
}
