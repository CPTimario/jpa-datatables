# jpa-datatables
Server side JPA support for datatables. Generates the datatables response based on the received datatables request from the client side.
## Maven Dependency
```xml
<dependency>
  <groupId>io.github.cptimario</groupId>
  <artifactId>jpa-datatables</artifactId>
  <version>1.0.0</version>
</dependency>
```
## Usage
### Basic
```java
@PersistenceContext
EntityManager entityManager;
...
DataTablesParameter dataTablesParameter = ...; // Received from the client side.
DataTables<SomeEntity> dataTables = DataTable.of(SomeEntity.class,dataTablesParameter);
DataTablesResponse<SomeEntity> dataTablesResponse = dataTables.getDataTablesResponse(entityManager);
```
### Using [QueryParameter](src/main/java/io/github/cptimario/datatables/QueryParameter.java) class
#### Including additional WHERE condition
```java
@PersistenceContext
EntityManager entityManager;
...
QueryParameter queryParameter = new QueryParameter();
// Use the camel case of the entity class as alias and use named parameters
queryParameter.addWhereCondition("someEntity.property = :namedParameter");
queryParameter.put("namedParameter", value);
DataTablesResponse<SomeEntity> dataTablesResponse = dataTables.getDataTablesResponse(entityManager, queryParameter);
```
#### Including additional ORDER condition
```java
@PersistenceContext
EntityManager entityManager;
...
QueryParameter queryParameter = new QueryParameter();
// Use the camel case of the entity class as alias
queryParameter.addOrderCondition("someEntity.property asc");
DataTablesResponse<SomeEntity> dataTablesResponse = dataTables.getDataTablesResponse(entityManager, queryParameter);
```
#### Overriding SELECT clause
```java
@PersistenceContext
EntityManager entityManager;
...
QueryParameter queryParameter = new QueryParameter();
// Use the camel case of the entity class as alias
queryParameter.setSelectClause("Select someEntity.firstProperty, someEntity.secondProperty, someEntity.thirdProperty");
DataTablesResponse<?> dataTablesResponse = dataTables.getDataTablesResponse(entityManager, queryParameter);
```
**NOTE:** Using this functionally will make the return type of the result list as `List<Object[]>`, instead of `List<SomeEntity>`, where `Object[]` is an array which represent the columns specified in the **SELECT** clause.
#### Adding GROUP BY and HAVING conditions
```java
@PersistenceContext
EntityManager entityManager;
...
QueryParameter queryParameter = new QueryParameter();
// Use the camel case of the entity class as alias
queryParameter.setSelectClause("Select someEntity.firstProperty, Count(someEntity.firstProperty)");
queryParameter.addGroupByField("someEntity.firstProperty");
queryParameter.addHavingCondition("Count(someEntity.firstProperty) > 0");
DataTablesResponse<?> dataTablesResponse = dataTables.getDataTablesResponse(entityManager, queryParameter);
```
**NOTE:** Use this functionally together with [Overriding SELECT clause](#overriding-select-clause).
