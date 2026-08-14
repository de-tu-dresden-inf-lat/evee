package de.tu_dresden.inf.lat.evee.concreteDomains.data.names;
/**
 * @author Christian Alrabbaa
 *
 */
public enum PropertyName {
    enableZ3Checks("concrete-domain-reasoner.enableZ3Checks");

    private final String propertyStr;

    PropertyName(String value) {
        propertyStr = value;
    }

    public String getProperty() {
        return this.propertyStr;
    }
}
