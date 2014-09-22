package it.jrc.domain.auth;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(PermissionTarget.class)
public class PermissionTarget_ {
  
  public static volatile SingularAttribute<Permission, Long> id;
  
  public static volatile SingularAttribute<Permission, String> name;
}
