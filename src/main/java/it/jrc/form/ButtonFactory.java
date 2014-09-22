package it.jrc.form;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

/**
 * Builds buttons of predefined types and serves to keep button icon information
 * in one place.
 * 
 * FIXME: Use CSS instead.
 * 
 * @author tempewi
 */
public class ButtonFactory {

    public static final String SAVE_ICON = "../biopama/img/action_save.gif";

    public static final String CANCEL_ICON = "../runo/icons/16/cancel.png";

    public static final String DELETE_ICON = "../runo/icons/16/trash.png";

    public static final String HELP_ICON = "../runo/icons/16/help.png";

    public static final String SAVE_BUTTON_CAPTION = "Save";

    public static final String EDIT_BUTTON_CAPTION = "Edit";

    public static final String DELETE_BUTTON_CAPTION = "Delete";

    public static final String CREATE_BUTTON_CAPTION = "New";

    /**
     * Builds a button with an icon on the left and a caption on the right.
     * 
     * @param caption
     *            the button text
     * @param iconUrl
     *            the icon url, usually obtained from the enclosing class
     * @return
     */
    public static Button getButton(String caption, String iconUrl) {
        Button button = new Button(caption);
        button.setIcon(new ThemeResource(iconUrl));
        return button;

    }

    /**
     * Builds a button with an image that looks like a link but behaves as a
     * button.
     * 
     * @param iconUrl
     * @return
     */
    public static Button getLinkButton(String iconUrl) {
        Button button = new Button();
        button.setStyleName(BaseTheme.BUTTON_LINK);
        button.setIcon(new ThemeResource(iconUrl));
        return button;
    }
}
