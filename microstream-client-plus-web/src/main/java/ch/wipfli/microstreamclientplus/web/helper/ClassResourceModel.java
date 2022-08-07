package ch.wipfli.microstreamclientplus.web.helper;

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.wicket.Session;
import org.apache.wicket.model.IModel;

public class ClassResourceModel implements IModel<String> {

    /**
     * Factory method for a class resource model.
     *
     * @param type the clazz on which the property is.
     * @param propertyName the name of the property.
     * @return the class recource model.
     */
    public static ClassResourceModel of(Class<?> type, String propertyName) {
        return of(type, propertyName, null);
    }

    /**
     * Factory method for a class resource model.
     *
     * @param type the clazz on which the property is.
     * @param propertyName the name of the property.
     * @param locale (optional) locale to get resource for. If not specified, takes the one from session or default.
     * @return the class recource model.
     */
    public static ClassResourceModel of(Class<?> type, String propertyName, Locale locale) {
        return new ClassResourceModel(type, propertyName, locale);
    }


    private final String className;
    private final String propertyName;
    private final Locale locale;


    /**
     * Constructor.
     *
     * @param type the clazz on which the property is
     * @param propertyName the name of the property
     * @param locale (optional) locale to get resource for. If not specified, takes the one from session or default.
     */
    public ClassResourceModel(Class<?> type, String propertyName, Locale locale) {
        this.className = type.getName();
        this.propertyName = propertyName;
        this.locale = locale;
    }


    @Override
    public String getObject() {
        return getStringResource(className, propertyName, getLocale());
    }

    private Locale getLocale() {
        if (locale != null) {
            return locale;
        }
        else if (Session.exists()) {
            return Session.get().getLocale();
        }
        else {
            return Locale.getDefault();
        }
    }

    /**
     * Returns a string from a resource bundle.
     *
     * @param bundleName the name of the bundle
     * @param key the key to lookup the resource
     * @param locale the locale
     * @return the found resource or throws an exception.
     */
    public static String getStringResource(String bundleName, String key, Locale locale) {
        final ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
        if (bundle.containsKey(key)) {
            return bundle.getString(key);
        }
        throw new RuntimeException("could not find property " + key + " in class " + bundleName);
    }
}
