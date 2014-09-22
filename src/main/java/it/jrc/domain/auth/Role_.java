package it.jrc.domain.auth;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Role.class)
public class Role_ {

  public static volatile SingularAttribute<Role, Long> id;
  
  public static volatile SingularAttribute<Role, String> firstName;
  
  public static volatile SingularAttribute<Role, String> lastName;
  
//  public static volatile SingularAttribute<Role, String> identity;
  
  public static volatile SingularAttribute<Role, Boolean> canLogin;
  
  public static volatile SingularAttribute<Role, String> email;
  
//  public static volatile SingularAttribute<Role, String> realm;
  
  public static volatile SingularAttribute<Role, String> password;
  
  public static volatile SingularAttribute<Role, Boolean> isSuperUser;
  
  public static volatile SetAttribute<Role, Group> groups;

  public static volatile SetAttribute<Role, Permission> permissions;

  public static volatile SetAttribute<Role, OpenIdIdentity> openIdIdentities;
}
