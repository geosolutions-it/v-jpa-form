package it.jrc.domain.metadata;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ColumnDescription.class)
public abstract class ColumnDescription_ {

	public static volatile SingularAttribute<ColumnDescription, Long> id;
	
	public static volatile SingularAttribute<ColumnDescription, String> name;
	
	public static volatile SingularAttribute<ColumnDescription, String> description;
	
	public static volatile SingularAttribute<ColumnDescription, TableDescription> tableDescription;

}