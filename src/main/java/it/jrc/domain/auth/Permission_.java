package it.jrc.domain.auth;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Permission.class)
public class Permission_ {
  
  public static volatile SingularAttribute<Permission, Long> id;
  
  public static volatile SingularAttribute<Permission, String> action;
  
  public static volatile SingularAttribute<Permission, PermissionTarget> target;
}
