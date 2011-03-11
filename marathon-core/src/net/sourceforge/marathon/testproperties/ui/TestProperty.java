package net.sourceforge.marathon.testproperties.ui;

public class TestProperty {

    public static enum PropertyType {
        INTEGER, STRING
    }

    public static enum DisplayType {
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

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((display == null) ? 0 : display.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TestProperty other = (TestProperty) obj;
        if (display != other.display)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type != other.type)
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
