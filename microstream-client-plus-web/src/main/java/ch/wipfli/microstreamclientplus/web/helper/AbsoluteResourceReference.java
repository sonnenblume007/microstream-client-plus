package ch.wipfli.microstreamclientplus.web.helper;

import org.apache.wicket.request.resource.PackageResourceReference;

import ch.wipfli.microstreamclientplus.web.WicketApplication;

public class AbsoluteResourceReference extends PackageResourceReference {
    public AbsoluteResourceReference(String name) {
        super(WicketApplication.class, name);
    }
}
