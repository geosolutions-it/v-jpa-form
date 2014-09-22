package it.jrc.ui;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

public class HtmlHeader extends Label {
    
    private static final String DISPLAY_PANEL_HEADER = "display-panel-header";

    public HtmlHeader(String content) {
        super();
        setValue(content);
        
        //Why just calling super() doesn't work I don't know
        addStyleName(DISPLAY_PANEL_HEADER);
    }
    
    public HtmlHeader() {
        setContentMode(ContentMode.HTML);
        addStyleName(DISPLAY_PANEL_HEADER);
    }

}
