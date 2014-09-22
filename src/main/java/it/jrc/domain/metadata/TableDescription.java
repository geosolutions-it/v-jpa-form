package it.jrc.domain.metadata;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(schema = "metadata", name = "table_description")
public class TableDescription {

    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "gen")
    @SequenceGenerator(allocationSize = 1, name = "gen", sequenceName = "metadata.table_description_id_seq")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private String name;

    @Column
    @NotNull
    @Size(max=100)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String schema;

    @Column
    @NotNull
    @Size(max=100)
    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    private String description;

    @Column
    @NotNull
    @Size(max=1000)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    private Set<ColumnDescription> columnDescriptions;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "tableDescription")
    @OrderBy("id")
    public Set<ColumnDescription> getColumnDescriptions() {
        return columnDescriptions;
    }

    public void setColumnDescriptions(Set<ColumnDescription> columnDescriptions) {
        for (ColumnDescription columnDescription : columnDescriptions) {
            if(columnDescription.getTableDescription() == null) {
                columnDescription.setTableDescription(this);
            }
        }
        this.columnDescriptions = columnDescriptions;
    }
    
    @Override
    public String toString() {
        return name;
    }

}
