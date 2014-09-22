package it.jrc.ui;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

public class HtmlLabel extends Label {
    
    public HtmlLabel(String content) {
        super();
        setValue(content);
        setContentMode(ContentMode.HTML);
        addStyleName("simple-html-label");
    }
    
    public HtmlLabel() {
        setContentMode(ContentMode.HTML);
        addStyleName("simple-html-label");
    }

//    @Override
//    public void setWidth(float width, Unit unit) {
//        // NO-OP
//    }
}
