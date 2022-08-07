package ch.wipfli.microstreamclientplus.web.behaviors;

import java.util.Set;

import org.apache.wicket.ClassAttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;

public class SelectedLinkClassModifier extends ClassAttributeModifier {

    private Class<? extends Page> page;
    private Class<? extends Page> currentPage;

    public SelectedLinkClassModifier(Class<? extends Page> page) {
        this.page = page;
    }

    @Override
    public void onConfigure(Component component) {
        super.onConfigure(component);
        currentPage = component.getPage().getClass();
    }

    @Override
    protected Set<String> update(Set<String> oldClasses) {
        if (this.page.equals(currentPage)) {
            oldClasses.add("active");
        }
        else {
            oldClasses.remove("active");
        }
        return oldClasses;
    }
}
