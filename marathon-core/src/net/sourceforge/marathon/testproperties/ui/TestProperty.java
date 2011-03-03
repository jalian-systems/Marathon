package net.sourceforge.marathon.testproperties.ui;

public class TestProperty {

    static enum PropertyType {
        INTEGER, STRING
    }

    static enum DisplayType {
        TEXTBOX, TEXTFIELD
    }

    String name;
    PropertyType type;
    DisplayType display;
    private String value;

    public TestProperty() {
    }

    public TestProperty(String name, PropertyType type, DisplayType display, String value) {
        this.name = name;
        this.type = type;
        this.display = display;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public PropertyType getType() {
        return type;
    }

    public DisplayType getDisplay() {
        return display;
    }

    public String getValue() {
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(PropertyType type) {
        this.type = type;
    }

    public void setDisplay(DisplayType display) {
        this.display = display;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
