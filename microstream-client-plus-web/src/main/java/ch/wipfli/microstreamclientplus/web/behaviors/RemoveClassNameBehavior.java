package ch.wipfli.microstreamclientplus.web.behaviors;

import java.util.Set;

import org.apache.wicket.ClassAttributeModifier;

public class RemoveClassNameBehavior extends ClassAttributeModifier {

    private final String className;

    public RemoveClassNameBehavior(String className) {
        this.className = className;
    }

    @Override
    protected Set<String> update(Set<String> oldClasses) {
        oldClasses.remove(className);
        return oldClasses;
    }
}
