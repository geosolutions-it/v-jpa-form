package it.jrc.domain.adminunits;

import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import com.vividsolutions.jts.geom.Polygon;

@StaticMetamodel(Country.class)
public abstract class Country_ {

    public static volatile SingularAttribute<Country, String> isoa3Id;

    public static volatile SingularAttribute<Country, String> name;

    public static volatile SingularAttribute<Country, Long> id;

    public static volatile SingularAttribute<Country, String> isoa2Id;

    public static volatile SingularAttribute<Country, Polygon> envelope;

    public static volatile SetAttribute<Country, Grouping> groupings;

}
