import javax.persistence.Entity;
import java.util.*;

public class DataTables<E> {
    public enum JoinType {
        CROSS_JOIN, LEFT_JOIN
    }

    private final Class<E> entity;
    private final String entityName;
    private DataTablesParameter dataTablesParameter;
    private HashMap<String, String> aliasByFieldName;

    public DataTables(Class<E> entity, DataTablesParameter dataTablesParameter) {
        if (!isEntity(entity))
            throw new IllegalArgumentException(entity.getName() + " is not a valid entity.");
        Objects.requireNonNull(dataTablesParameter);
        this.entity = entity;
        this.entityName = entity.getSimpleName();
        this.dataTablesParameter = dataTablesParameter;
        this.aliasByFieldName = new HashMap<>();
        registerAlias(entityName);
    }

    public String getQueryString() {
        return getQueryString(JoinType.CROSS_JOIN);
    }

    public String getQueryString(JoinType joinType) {


        return "";
    }

    String getFromClause(JoinType joinType) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(" From ");
        stringBuilder.append(entityName);
        stringBuilder.append(" ");
        stringBuilder.append(aliasByFieldName.get("main"));

        if (joinType.equals(JoinType.LEFT_JOIN)) {
            // TODO
        }
        return stringBuilder.toString();
    }

    String getLeftJoinClause(List<Column> columnList) {
        Set<String> leftJoinSet = new LinkedHashSet<>();
        StringBuilder stringBuilder = new StringBuilder();
        for (Column column : columnList) {
            if (column.isMultiField()) {
                for (Column subColumn : column.getSubColumnList()) {

                }
            } else {

            }
        }
        return "";
    }

    String getLeftJoinClause(Column column) {
        String leftTableAlias, rightTableAlias;
        String fieldName = column.getRelationshipFieldName();
        registerAlias(fieldName);
        leftTableAlias = aliasByFieldName.get(entityName);
        rightTableAlias = aliasByFieldName.get(fieldName);
        return column.getLeftJoinClause(leftTableAlias, rightTableAlias);
    }

    static <E> boolean isEntity(Class<E> entity) {
        return Objects.nonNull(entity.getAnnotation(Entity.class));
    }

    private void registerAlias(String fieldName) {
        int currentAliasCount = aliasByFieldName.size();
        int aliasSuffix = Objects.isNull(aliasByFieldName.get(fieldName)) ? currentAliasCount : ++currentAliasCount;
        String aliasPrefix = fieldName.length() > 5 ? fieldName.substring(0, 5)  : fieldName;
        String alias = aliasPrefix.toLowerCase() + "_" + aliasSuffix;
        aliasByFieldName.put(fieldName, alias);
    }
}
