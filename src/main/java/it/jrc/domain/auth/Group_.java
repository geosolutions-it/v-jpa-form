package it.jrc.domain.auth;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Group.class)
public class Group_ {

  public static volatile SingularAttribute<Group, Long> id;
  
  public static volatile SingularAttribute<Group, String> name;
  
  public static volatile SetAttribute<Group, Permission> permissions;
  
  public static volatile SetAttribute<Group, Role> roles;
  
}
