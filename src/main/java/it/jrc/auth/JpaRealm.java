package it.jrc.auth;

import it.jrc.domain.auth.Role;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * 
 * @author Will Temperley
 * 
 *         Essentially a DAO that supplies login and credential information to
 *         Shiro.
 * 
 */
@Singleton
public class JpaRealm extends AuthorizingRealm {


    Logger logger = LoggerFactory.getLogger(JpaRealm.class);
    private EntityManager entityManager;

    @Inject
    public JpaRealm(EntityManagerFactory emf) {
        setName("JpaRealm");
        this.entityManager = emf.createEntityManager();
    }

    /**
     * Gets a role by its identity. If this fails, the email address is tried,
     * as this is used to retrieve Google openIds.
     */
    protected AuthenticationInfo doGetAuthenticationInfo(
            AuthenticationToken authcToken) throws AuthenticationException {

        UsernamePasswordToken token = (UsernamePasswordToken) authcToken;
        
        String userName = token.getUsername();
        if (userName != null && userName.equals(Anonymous.USERNAME)) {
            return new SimpleAuthenticationInfo(Anonymous.id, "", getName());
        }
        /*
         * Obtain the account details
         */
        TypedQuery<Role> q = entityManager.createNamedQuery(
                "Role.findRoleByIdentity", Role.class);
        q.setParameter("identity", token.getUsername());

        List<Role> roles = q.getResultList();
        if (roles.size() == 0) {
            q = entityManager.createNamedQuery("Role.findRoleByEmail",
                    Role.class);
            q.setParameter("email", token.getUsername());
            roles = q.getResultList();
        }
        if (roles.size() < 1) {
            throw new UnknownAccountException();
        }

        /*
         * Get the first role in the list. There must be at least one role here.
         * Throw exception if account is locked.
         */
        Role role = roles.get(0);

        /*
         * Ensure the role is completely reloaded from the database, to ensure
         * any changes made externally, e.g. permission changes have been
         * reloaded properly.
         */
        entityManager.refresh(role);
        if (role.getCanLogin()) {
            return new SimpleAuthenticationInfo(role.getId(), "", getName());
        } else {
            throw new LockedAccountException();
        }

    }

    protected AuthorizationInfo doGetAuthorizationInfo(
            PrincipalCollection principals) {

        Long userId = (Long) principals.fromRealm(getName()).iterator().next();

        Role role = entityManager.find(Role.class, userId);
        if (role != null) {
            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

            info.addStringPermissions(role.getStringPermissions());

            return info;
        }
        return null;
    }

}