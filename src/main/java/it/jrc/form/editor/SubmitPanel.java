package it.jrc.form.editor;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;


public class SubmitPanel extends CssLayout {
    
    private static final String CSS_CLASS_SUBMIT_PANEL = "submit-panel";
    
    public SubmitPanel() {
        addStyleName(CSS_CLASS_SUBMIT_PANEL);
    }
    
    public void addRight(Component c) {
        c.setStyleName("button-right");
        addComponent(c);
    }
    
    public void addLeft(Component c) {
        c.setStyleName("button-left");
        addComponent(c);
    }
    

}
