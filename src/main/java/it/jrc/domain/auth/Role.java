package it.jrc.domain.auth;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

/**
 * Simple class that represents any User domain entity in any application.
 * 
 * <p>
 * Because this class performs its own Realm and Permission checks, and these
 * can happen frequently enough in a production application, it is highly
 * recommended that the internal User {@link #getRoles} collection be cached in
 * a 2nd-level cache when using JPA and/or Hibernate.
 * </p>
 */
@Entity
@Table(schema = "auth", name = "role")
@Cacheable(true)
@NamedQueries({
        @NamedQuery(name = "Role.findRoleByEmail", query = "FROM Role WHERE email = :email"),
        @NamedQuery(name = "Role.findRoleByIdentity", query = "select o.role FROM OpenIdIdentity o where o.identity = :identity")
        })
public class Role {

    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq")
    @SequenceGenerator(allocationSize = 1, name = "seq", sequenceName = "auth.role_id_seq")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private String firstName;

    @Column
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    private String lastName;

    @Column
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    private Boolean canLogin = false;

    @Column
    @NotNull
    public Boolean getCanLogin() {
        return canLogin;
    }

    public void setCanLogin(Boolean canLogin) {
        this.canLogin = canLogin;
    }

    private String email;

    @Column
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private Set<OpenIdIdentity> openIdIdentities;

    @OneToMany(mappedBy = "role", cascade=CascadeType.ALL)
    public Set<OpenIdIdentity> getOpenIdIdentities() {
        return openIdIdentities;
    }

    public void setOpenIdIdentities(Set<OpenIdIdentity> openIdIdentities) {
        this.openIdIdentities = openIdIdentities;
    }

    private Boolean isSuperUser = false;

    @Column(name = "is_superuser")
    @NotNull
    public Boolean getIsSuperUser() {
        return isSuperUser;
    }

    public void setIsSuperUser(Boolean isSuperUser) {
        this.isSuperUser = isSuperUser;
    }

    private Set<Group> groups = new HashSet<Group>();

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "auth.role_group", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "group_id"))
    public Set<Group> getGroups() {
        return groups;
    }

    public void setGroups(Set<Group> roles) {
        this.groups = roles;
    }

    private Set<Permission> permissions;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "auth.role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }

    private Set<String> stringPermissions = new HashSet<String>();

    /**
     * Get all permissions associated with this role
     * 
     * @return {@link Set<String>} the set of string permissions that is the
     *         union of the role and group permissions
     */
    @Transient
    @Deprecated
    public Set<String> getStringPermissions() {
        
        if (isAnonymous()) {
            return stringPermissions;
        }

        Set<Permission> perms = getPermissions();

        Set<Group> groups = getGroups();
        for (Group group : groups) {
            perms.addAll(group.getPermissions());
        }

        for (Permission permission : permissions) {
            stringPermissions.add(permission.toString());
        }
        return stringPermissions;
    }

    @Override
    public boolean equals(Object obj) {
        
        if (!(obj instanceof Role)) {
            return false;
        }
        Role otherRole = (Role) obj;
        if (otherRole.getId().equals(this.getId())) {
            return true;
        }
        return false;
    }
    
    /**
     * Is the user anonymous
     * 
     * @return true if yes
     */
    @Transient
    public boolean isAnonymous() {
        if (id != null && id != 0) {
            return false;
        }
        return true;
    }
}
