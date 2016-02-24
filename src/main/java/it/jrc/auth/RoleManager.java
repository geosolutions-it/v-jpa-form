package it.jrc.auth;

import it.jrc.domain.auth.HasRole;
import it.jrc.domain.auth.OpenIdIdentity;
import it.jrc.domain.auth.Role;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.shiro.SecurityUtils;
import org.expressme.openid.Authentication;
import org.expressme.openid.OpenIdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.servlet.SessionScoped;
import com.nimbusds.oauth2.sdk.SerializeException;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.UserInfoSuccessResponse;

import eu.cec.digit.ecas.client.jaas.DetailedUser;

@SessionScoped
public class RoleManager {

    public enum Action {
        READ, CREATE, UPDATE, DELETE
    }

    Logger logger = LoggerFactory.getLogger(RoleManager.class);

    private Set<String> stringPermissions = new HashSet<String>();
    
    /*
     * 
     * TODO: get classes from classloader?
     */
    private Map<Class<?>, String> classUrlMapping;

    private Role role;

    private Provider<EntityManager> entityManagerProvider;

    private Role anonymousRole;

    @Inject
    public RoleManager(Provider<EntityManager> em, Map<Class<?>, String> classUrlMapping) {

        this.entityManagerProvider = em;

        this.anonymousRole = new Role();
        DetailedUser ecasPrincipal =(DetailedUser)AuthFilter.ecasPrincipal.get();
        if(ecasPrincipal != null) {
        	role = getUserFromEmail(ecasPrincipal.getEmail());
        	if(role == null) {
        		role = createUser(ecasPrincipal);
        		if(role != null) {
        			getEm().detach(role);
        			role = getUserFromEmail(ecasPrincipal.getEmail());
        		}
        	}
        	
        } else {
        	Object id = SecurityUtils.getSubject().getPrincipal();
            if (id.equals(0l)) {
                role = anonymousRole;
            } else {
                role = getEm().find(Role.class, id);
            }
        }
        

        this.classUrlMapping = classUrlMapping;
        loadPermissions();
    }

	private Role getUserFromEmail(String email) {
		Query query = getEm().createQuery("select f FROM Role f WHERE f.email = :email"); 
		query.setParameter("email", email); 
		List results = query.getResultList();
		if(results.size() == 0) {
			return null;
		} else {
			Role role = (Role)results.get(0);
			return role;
		}
	}

    /**
     * Creates a new role that cannot login. Sends a notification email to the
     * app administrator.
     * 
     * UPDATE: looks for a user with the identity.
     * 
     * @param returnTo
     * 
     */
    private Role createUser(DetailedUser user) {

        Role role = new Role();

        role.setCanLogin(true);
        role.setIsSuperUser(false);

        role.setEmail(user.getEmail());
        role.setFirstName(user.getFirstName());
        role.setLastName(user.getLastName());
        
        getEm().getTransaction().begin();
        getEm().persist(role);
        getEm().getTransaction().commit();
        return role;

    }
    
    public boolean checkPermission(Action action, String target) {
        
        if (role.getIsSuperUser()) {
            return true;
        }

        logger.debug(action + "_" + target);
        boolean hasPermission = stringPermissions.contains(action + "_"
                + target);
        logger.debug("has permission: " + hasPermission);
        return hasPermission;

    }

    public void loadPermissions() {

        stringPermissions = role.getStringPermissions();

    }

    public String getUrlForClass(Class<?> clazz) {
        return classUrlMapping.get(clazz);
    }
    
    public boolean isOwner(HasRole entity) {
    	return entity.getRole().equals(role);
    }

    public boolean canCreate(String target) {
        if (role.getIsSuperUser()) {
            return true;
        }
        return checkPermission(Action.CREATE, target);
    }

    public boolean canUpdate(String target) {
        if (role.getIsSuperUser()) {
            return true;
        }
        return checkPermission(Action.UPDATE, target);
    }

    public boolean canDelete(String target) {
        if (role.getIsSuperUser()) {
            return true;
        }
        return checkPermission(Action.DELETE, target);
    }

    public boolean canView(String target) {
        if (role.getIsSuperUser()) {
            return true;
        }
        return checkPermission(Action.READ, target);
    }

    public Role getRole() {
       return role; 
    }

    private EntityManager getEm() {
        return entityManagerProvider.get();
    }

    public void logout() {
        SecurityUtils.getSubject().logout();
        role = anonymousRole;
    }

}
