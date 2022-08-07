package ch.wipfli.microstreamclientplus.web.behaviors;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.wicket.ClassAttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public class AjaxChangePanelLink extends AjaxLink<Void> {

        final IModel<Panel> currentPanelModel;
        final Panel replacePanel;
        final List<Component> refreshTargets;

        public AjaxChangePanelLink(String id, IModel<Panel> currentPanelModel, Panel replacePanel, Component... refreshTargets) {
            super(id);
            this.currentPanelModel = currentPanelModel;
            this.replacePanel = replacePanel;
            this.refreshTargets = refreshTargets != null ? Stream.of(refreshTargets).collect(Collectors.toList()) : new ArrayList<>();
            add(new ClassAttributeModifier() {
                @Override
                protected Set<String> update(Set<String> oldClasses) {
                    if (currentPanelModel.getObject().equals(replacePanel)) {
                        oldClasses.add("active");
                    }
                    else {
                        oldClasses.remove("active");
                    }
                    return oldClasses;
                }
            });

        }

        @Override
        public void onClick(AjaxRequestTarget target) {
            currentPanelModel.getObject().replaceWith(replacePanel);
            currentPanelModel.setObject(replacePanel);
            target.add(currentPanelModel.getObject());
            target.add(refreshTargets.toArray(new Component[0]));
        }

        public void addRefreshTargets(Component... components) {
            final List<Component> targets = refreshTargets != null ? Stream.of(components).collect(Collectors.toList()) : new ArrayList<>();
            this.refreshTargets.addAll(targets);
        }
    }
