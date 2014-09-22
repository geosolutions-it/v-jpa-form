package it.jrc.domain.adminunits;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Grouping.class)
public abstract class Grouping_ {

    public static volatile SingularAttribute<Grouping,GroupingType> groupingType;

    public static volatile SingularAttribute<Grouping,String> description;

    public static volatile SingularAttribute<Grouping,String> id;

    public static volatile SetAttribute<Grouping,Country> countrys;

}
