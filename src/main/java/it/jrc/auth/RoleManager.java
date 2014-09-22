package it.jrc.auth;

import it.jrc.domain.auth.HasRole;
import it.jrc.domain.auth.Role;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.servlet.SessionScoped;

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
        
        Object id = SecurityUtils.getSubject().getPrincipal();
        if (id.equals(0l)) {
            role = anonymousRole;
        } else {
            role = getEm().find(Role.class, id);
        }

        this.classUrlMapping = classUrlMapping;
        loadPermissions();
    }

    public boolean checkPermission(Action action, String target) {
        
        System.out.println(target);
        
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
