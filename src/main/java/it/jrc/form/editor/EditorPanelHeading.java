package it.jrc.form.editor;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

public class EditorPanelHeading extends CssLayout {

    public class SimpleLabel extends Label {

        public SimpleLabel(String content) {
            setValue(content);
        }

        @Override
        public void setWidth(float width, Unit unit) {
            // NO-OP
        }
    }

    private SimpleLabel heading;

    public EditorPanelHeading(String title) {
        heading = new SimpleLabel(title);
        addStyleName("panel-header");
        addComponent(heading);

    }

    public void addActionButton(Component button) {
        addComponent(button);
    }
    
    public void setTitle(String title) {
        heading.setValue(title);
    }
}
