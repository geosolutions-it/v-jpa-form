package it.jrc.domain.metadata;

import java.util.Set;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(TableDescription.class)
public abstract class TableDescription_ {

	public static volatile SingularAttribute<TableDescription, Long> id;
	
	public static volatile SingularAttribute<TableDescription, String> name;
	
	public static volatile SingularAttribute<TableDescription, String> schema;
	
	public static volatile SingularAttribute<TableDescription, String> description;
	
	public static volatile SetAttribute<TableDescription, Set<ColumnDescription>> columnDescriptions;
	

}