package it.jrc.auth;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.vaadin.server.Page;

/**
 * One place for generic account actions
 * 
 * @author Will Temperley
 * 
 */
public class AccountActions {

    private String logoutPath;

    @Inject
    public AccountActions(@Named("logout_path") String logoutPath) {
        this.logoutPath = logoutPath;
    }
    
    public void logout() {
        Page.getCurrent().setLocation(logoutPath);
    }

}
