package org.guide.gui.controllers.about;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ThirdPartySoftware {

    /**
     * The name of the software.
     */
    private final StringProperty name;

    /**
     * The version of the software.
     */
    private final StringProperty version;

    /**
     * The website of the software.
     */
    private final StringProperty website;

    /**
     * Creates a new third party software instance.
     *
     * @param name    The software name.
     * @param version The software version.
     * @param website The software website.
     */
    public ThirdPartySoftware(String name, String version, String website) {
        this.name = new SimpleStringProperty(name);
        this.version = new SimpleStringProperty(version);
        this.website = new SimpleStringProperty(website);
    }

    /**
     * Gets the name property.
     *
     * @return The name property.
     */
    public StringProperty nameProperty() {
        return name;
    }

    /**
     * Gets the version property.
     *
     * @return The version property.
     */
    public StringProperty versionProperty() {
        return version;
    }

    /**
     * Gets the website property.
     *
     * @return The website property.
     */
    public StringProperty websiteProperty() {
        return website;
    }

    /**
     * Gets the software's name.
     *
     * @return The name.
     */
    public String getName() {
        return name.get();
    }

    /**
     * Gets the software's version.
     *
     * @return The version.
     */
    public String getVersion() {
        return version.get();
    }

    /**
     * Gets the software's website URL.
     *
     * @return The website.
     */
    public String getWebsite() {
        return website.get();
    }

}
