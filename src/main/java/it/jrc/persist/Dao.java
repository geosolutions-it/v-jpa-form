package it.jrc.persist;

import it.jrc.auth.RoleManager;
import it.jrc.domain.metadata.ColumnDescription;
import it.jrc.domain.metadata.TableDescription;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.vaadin.addon.jpacontainer.EntityManagerProvider;

/**
 * 
 * Provides generic persistence functions.
 * 
 * 1. Acts as an {@link EntityManager} provider
 * 2. Gets table metadata
 * 3. 
 * 
 * @author Will Temperley
 *
 */
public class Dao implements EntityManagerProvider {

    private Map<Class<?>, String> classUrlMapping;
    private RoleManager roleManager;
    private Provider<EntityManager> entityManagerProvider;
    private PersistenceUnitUtil puu;

    Logger logger = LoggerFactory.getLogger(Dao.class);

    @Inject
    public Dao(Provider<EntityManager> entityManagerProvider,
            EntityManagerFactory emf, Map<Class<?>, String> classUrlMapping,
            @Nullable RoleManager roleManager) {
        // this.em = entityManagerFactory.createEntityManager();
        this.puu = emf.getPersistenceUnitUtil();
        this.classUrlMapping = classUrlMapping;
        this.roleManager = roleManager;
        this.entityManagerProvider = entityManagerProvider;
    }

    public EntityManager getEntityManager() {
        return entityManagerProvider.get();
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public Object persist(Object obj) {

        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            em.persist(obj);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException(e.getMessage());
        }

        return obj;
    }

    public Object merge(Object obj) {

        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            obj = em.merge(obj);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException(e.getMessage());
        }

        return obj;
    }

    public <T> T find(Class<T> clazz, Object id) {
        return getEntityManager().find(clazz, id);
    }

    public <T> void remove(Class<T> clazz, Object id) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        em.remove(em.find(clazz, id));
        em.getTransaction().commit();
    }

    /**
     * Return a list of all objects of the class passed in.
     * 
     * @param <T>
     * @param clazz
     *            the class for which to retrieve objects
     * @return a list of objects of the requested class
     */
    public <T> List<T> all(Class<T> clazz) {

        CriteriaBuilder criteriaBuilder = getEntityManager()
                .getCriteriaBuilder();

        CriteriaQuery<T> cq = criteriaBuilder.createQuery(clazz);

        Root<T> root = cq.from(clazz);

        cq.select(root);
        // FIXME all entities have to have an ID!
        cq.orderBy(criteriaBuilder.asc(root.get("id")));

        TypedQuery<T> tq = getEntityManager().createQuery(cq);
        return tq.getResultList();
    }

    public <T> List<T> all(Class<T> clazz, SingularAttribute<?, ?> orderBy) {

        CriteriaBuilder criteriaBuilder = getEntityManager()
                .getCriteriaBuilder();

        CriteriaQuery<T> cq = criteriaBuilder.createQuery(clazz);

        Root<T> root = cq.from(clazz);

        cq.select(root);

        cq.orderBy(criteriaBuilder.asc(root.get(orderBy.getName())));

        TypedQuery<T> tq = getEntityManager().createQuery(cq);
        return tq.getResultList();
    }

    public Object getId(Object obj) {
        return puu.getIdentifier(obj);
    }

    /**
     * Gets the correct URL for an object.
     * 
     * @return the URL
     */
    public String getEditorLink(Object object) {
        Object id = getId(object);
        String editorURL = classUrlMapping.get(object.getClass());
        return String.format("#!%s/%s", editorURL, id);
    }

    public String getEditorAnchor(Object val) {
        return String.format("<a href='%s'>%s</a>", this.getEditorLink(val), val.toString());
    }

    /**
     * For a given property, retrieves the best possible description, based on
     * metadata held in the database where possible.
     * 
     * @param prop
     * @return
     */
    public String getFieldDescription(Attribute<?, ?> prop) {

        StringBuilder stringBuilder = new StringBuilder();
        Class<?> attrClazz = prop.getJavaType();
        Class<?> tableClazz = prop.getDeclaringType().getJavaType();

        TableDescription tableDescr = getTableMetadata(tableClazz);

        if (tableDescr != null) {

            Set<ColumnDescription> cols = tableDescr.getColumnDescriptions();
            for (ColumnDescription columnDescription : cols) {
                if (columnDescription.getName().equals(prop.getName())) {
                    stringBuilder.append("<div class='tooltip-title'>");
                    stringBuilder.append(columnDescription.getDescription());
                    stringBuilder.append("</div>");
                }
            }
        }

        // if (Enumeration.class.isAssignableFrom(attrClazz)) {
        // @SuppressWarnings("unchecked")
        // List<Enumeration> enums = (List<Enumeration>) all(attrClazz);
        // StringBuilder sb = getEnumerationDescription(stringBuilder, enums);
        // return sb.toString();
        // }

        return stringBuilder.toString();
    }

    public String getTableDescription(Class<?> clazz) {
        TableDescription td = getTableMetadata(clazz);
        String desc = null;
        if (td != null) {
            desc = td.getDescription();
        }
        return desc;
    }

    private TableDescription getTableMetadata(Class<?> clazz) {

        Table annotation = clazz.getAnnotation(Table.class);
        String tableName = annotation.name();
        String schema = annotation.schema();

        TypedQuery<TableDescription> q = getEntityManager()
                .createQuery(
                        "from TableDescription where name = :name and schema = :schema",
                        TableDescription.class).setParameter("name", tableName)
                .setParameter("schema", schema);

        List<TableDescription> results = q.getResultList();
        if (results.size() == 1) {
            return results.get(0);
        } else {
            return null;
        }

    }

    /**
     * Not sure what this is for!!
     * 
     * @param attr
     * @return
     */
    public <T> List<String> unique(Attribute<T, String> attr) {

        Class<T> javaType = attr.getDeclaringType().getJavaType();

        CriteriaQuery<String> q = getEntityManager().getCriteriaBuilder()
                .createQuery(String.class);

        Root<T> from = q.from(javaType);
        q.multiselect(from.get(attr.getName()));

        TypedQuery<String> tq = getEntityManager().createQuery(q);

        return tq.getResultList();
    }

    /**
     * Looks up and entity based on a {@link Attribute} and it's value.
     * 
     * @param attr
     * @param attrValue
     * @return
     */
    public <T> T findByProxyId(SingularAttribute<T, String> attr,
            String attrValue) {

        if (attrValue == null || attr == null) {
            return null;
        }

        // upper case, no whitespace
        attrValue = attrValue.trim();
        attrValue = attrValue.toUpperCase();

        CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();

        Class<T> javaType = attr.getDeclaringType().getJavaType();

        CriteriaQuery<T> criteria = builder.createQuery(javaType);
        Root<T> entityRoot = criteria.from(javaType);
        criteria.select(entityRoot);

        Expression<String> trimmedString = builder.trim(builder
                .upper(entityRoot.get(attr)));

        criteria.where(builder.equal(trimmedString, attrValue));

        TypedQuery<T> q = getEntityManager().createQuery(criteria);

        List<T> res = q.getResultList();
        if (res.size() == 0) {
            return null;
        } else if (res.size() == 1) {
            return q.getSingleResult();
        } else {
            for (T t : res) {
                System.out.println("RESULT: " + t);
            }
            logger.error("Multiple objects found for query, using lookup: " + attrValue);
            return null;
        }
    }

    public Integer scalarNativeQuery(String query, Object... params) {
        
        Query q = getEntityManager().createNativeQuery(query);
        
        for (int i = 0; i < params.length; i++) {
            q.setParameter(i+1, params[i]);
        }
        
        try {
            return (Integer) q.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Returns the next value in a named sequence
     * 
     * @param seqName the sequence name (fully qualified)
     * @return
     */
    public Long getNextValueInSequence(String seqName) {

        Query q = getEntityManager().createNativeQuery("select nextval('"+seqName+"');");
        Object res = q.getSingleResult();
        if (res instanceof BigInteger) {
            Long id = ((BigInteger) res).longValue();
            // surface.setId(id);
            return id;
        }
        return null;
    }
        

}
