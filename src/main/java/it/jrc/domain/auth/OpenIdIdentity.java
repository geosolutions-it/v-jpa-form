package it.jrc.domain.auth;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
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
@Table(schema = "auth", name = "openid_identity")
@Cacheable(true)
public class OpenIdIdentity {

    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq")
    @SequenceGenerator(allocationSize = 1, name = "seq", sequenceName = "auth.openid_identity_id_seq")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private Role role;

    @ManyToOne
    @JoinColumn(name = "role_id")
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    private String identity;

    @Column
    @NotNull
    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    private String realm;

    @Column
    @NotNull
    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }
    
    @Override
    public String toString() {
        return identity;
    }

}
