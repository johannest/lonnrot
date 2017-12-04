package org.vaadin.lonnrot;

import com.vaadin.ui.Component;
import com.vaadin.ui.Tag;
import com.vaadin.ui.common.HasStyle;
import com.vaadin.ui.common.HtmlImport;

@Tag("iron-autogrow-textarea")
@HtmlImport("frontend://bower_components/iron-autogrow-textarea/iron-autogrow-textarea.html")
public class TextArea extends Component implements HasStyle {

    /**
     * Sets the value
     * @param value the value
     */
    public void setValue(String value) {
        getElement().setProperty("value", value);
    }

    /**
     * Gets the value
     * @return the value
     */
    public String getValue() {
        return getElement().getProperty("value", "");
    }


    public int getRows() {
        return getElement().getProperty("rows", 5);
    }

    public void setRows(int rows) {
        getElement().setProperty("rows", rows);
    }

    public int getMaxRows() {
        return getElement().getProperty("maxRows", 100);
    }

    public void setMaxRows(int maxRows) {
        getElement().setProperty("maxRows", maxRows);
    }

    public int getMinlength() {
        return getElement().getProperty("minlength", 0);
    }

    public void setMinlength(int minlength) {
        getElement().setProperty("minlength", minlength);
    }

    public int getMaxlength() {
        return getElement().getProperty("maxlength", 1000);
    }

    public void setMaxlength(int maxlength) {
        getElement().setProperty("maxlength", maxlength);
    }

    public String getPlaceholder() {
        return getElement().getProperty("placeholder", "");
    }

    public void setPlaceholder(String placeholder) {
        getElement().setProperty("placeholder", placeholder);
    }

    public boolean getReadonly() {
        return getElement().getProperty("readonly", false);
    }

    public void setReadonly(boolean readonly) {
        getElement().setProperty("readonly", readonly);
    }

    public boolean getRequired() {
        return getElement().getProperty("required", false);
    }

    public void setRequired(boolean required) {
        getElement().setProperty("required", required);
    }
}
