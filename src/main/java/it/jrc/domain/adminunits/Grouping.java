package it.jrc.domain.adminunits;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(schema = "administrative_units", name = "grouping")
public class Grouping {

    private String id;

    @Id
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    private GroupingType groupingType;

    @ManyToOne
    @JoinColumn(name="grouping_type_id")
    public GroupingType getGroupingType() {
        return groupingType;
    }

    public void setGroupingType(GroupingType groupingType) {
        this.groupingType = groupingType;
    }

    private String description;

    @NotNull
    @Column
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private Set<Country> countrys;

    @ManyToMany
    @JoinTable(name = "administrative_units.country_grouping", joinColumns = @JoinColumn(name = "grouping_id"), inverseJoinColumns = @JoinColumn(name = "country_id"))
    public Set<Country> getCountrys() {
        return countrys;
    }

    public void setCountrys(Set<Country> countrys) {
        this.countrys = countrys;
    }
    
    @Override
    public String toString() {
        return id;
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if (obj instanceof Grouping) {
            Grouping comparee = (Grouping) obj;
            if (comparee.getId().equals(getId())) {
                return true;
            }
        }
        return super.equals(obj);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
