import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Column {
    private String data;
    private String name;
    private boolean searchable;
    private Search search;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public Search getSearch() {
        return search;
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Column)) return false;
        Column column = (Column) o;
        return searchable == column.searchable && Objects.equals(data, column.data) && Objects.equals(name, column.name) && Objects.equals(search, column.search);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, name, searchable, search);
    }

    boolean isMultiField() {
        return data.contains("+");
    }

    boolean hasRelationship() {
        return data.contains(".");
    }

    boolean isNullable() {
        return data.contains("?");
    }

    String getFullFieldName() {
        return String.join(".", getFieldNames());
    }

    String getRelationshipFieldName() {
        if (hasRelationship() && !isMultiField())
            return getFieldNames().get(0);
        return "";
    }

    List<Column> getSubColumnList() {
        List<Column> subColumnList = new ArrayList<>();
        if (isMultiField()) {
            for (String fieldData : data.split("\\+")) {
                Column subColumn = new Column();
                subColumn.setData(fieldData.trim());
                subColumn.setSearchable(searchable);
                subColumn.setSearch(search);
                subColumnList.add(subColumn);
            }
        }
        return subColumnList;
    }

    String getLeftJoinClause(String leftTableAlias, String rightTableAlias) {
        StringBuilder stringBuilder = new StringBuilder();
        if (hasRelationship() && !isMultiField()) {
            String fieldName = getRelationshipFieldName();
            stringBuilder.append(" Left Join ");
            stringBuilder.append(leftTableAlias);
            stringBuilder.append(".");
            stringBuilder.append(fieldName);
            stringBuilder.append(" ");
            stringBuilder.append(rightTableAlias);
        }
        return stringBuilder.toString();
    }

    private List<String> getFieldNames() {
        List<String> fieldNameList = new ArrayList<>();
        Pattern fieldPattern = Pattern.compile("\\w+");
        Matcher matcher = fieldPattern.matcher(data);
        while (matcher.find() && !isMultiField()) {
            fieldNameList.add(matcher.group());
        }
        return fieldNameList;
    }
}
