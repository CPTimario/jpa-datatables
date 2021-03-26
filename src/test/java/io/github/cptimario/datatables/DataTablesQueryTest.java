package io.github.cptimario.datatables;

import io.github.cptimario.datatables.components.Column;
import io.github.cptimario.datatables.components.Order;
import io.github.cptimario.datatables.entity.ChildEntity;
import io.github.cptimario.datatables.entity.OtherEntity;
import io.github.cptimario.datatables.entity.ParentEntity;
import io.github.cptimario.datatables.repository.TestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@EnableAutoConfiguration
@ContextConfiguration(classes = TestRepository.class)
public class DataTablesQueryTest {

    private final TestRepository testRepository;
    private final EntityManager entityManager;

    private List<Column> columnList;
    private List<Order> orderList;
    private List<ParentEntity> parentEntityList;
    private DataTablesParameter dataTablesParameter;
    private DataTables<ParentEntity> dataTables;
    private DataTablesResponse<ParentEntity> dataTablesResponse;

    @Autowired
    public DataTablesQueryTest(TestRepository testRepository, EntityManager entityManager) {
        this.testRepository = testRepository;
        this.entityManager = entityManager;
    }

    @BeforeEach
    void setUp() {
        initializeData();
        columnList = getColumnList();
        orderList = List.of(new Order(0, "asc"));
    }

    private List<Column> getColumnList() {
        Column parentId = new Column("id");
        Column parentData = new Column("data");
        Column parentDate = new Column("date");
        Column childEntity = new Column("childEntity.firstData");
        Column childEntityDate = new Column("childEntity.date");
        Column otherEntityData = new Column("otherEntity.firstData otherEntity.secondData");
        return List.of(parentId, parentData, parentDate, childEntity, childEntityDate, otherEntityData);
    }

    private void initializeData() {
        Random random = new Random();
        parentEntityList = new ArrayList<>();
        List<ChildEntity> childEntityOptions = getChildEntityOptions();
        List<OtherEntity> otherEntityOptions = getOtherEntityOptions();
        for (int i = 0; i < 500; i++) {
            int childEntityIndex = random.nextInt(childEntityOptions.size());
            int otherEntityIndex = random.nextInt(otherEntityOptions.size());
            ParentEntity parentEntity = new ParentEntity();
            parentEntity.setData("parent " + i);
            parentEntity.setDate(LocalDate.of(2020 + i, Month.JANUARY, 1));
            parentEntity.setChildEntity(childEntityOptions.get(childEntityIndex));
            parentEntity.setOtherEntity(otherEntityOptions.get(otherEntityIndex));

            testRepository.save(parentEntity);
            parentEntityList.add(parentEntity);
        }
    }

    private List<ChildEntity> getChildEntityOptions() {
        ChildEntity firstChildEntity = new ChildEntity();
        firstChildEntity.setFirstData("first_child_entity_first_data");
        firstChildEntity.setSecondData("first_child_entity_second_data");
        firstChildEntity.setDate(LocalDate.of(2021, Month.JANUARY, 11));

        ChildEntity secondChildEntity = new ChildEntity();
        secondChildEntity.setFirstData("second child entity - first data");
        secondChildEntity.setSecondData("second child entity - second data");
        secondChildEntity.setDate(LocalDate.of(2021, Month.JANUARY, 25));

        return Arrays.asList(null, firstChildEntity, secondChildEntity);
    }

    private List<OtherEntity> getOtherEntityOptions() {
        OtherEntity firstOtherEntity = new OtherEntity();
        firstOtherEntity.setFirstData("first other entity % first data");
        firstOtherEntity.setSecondData("first other entity % second data");
        firstOtherEntity.setDate(LocalDate.of(2021, Month.FEBRUARY, 11));

        OtherEntity secondOtherEntity = new OtherEntity();
        secondOtherEntity.setFirstData("second other entity - first data");
        secondOtherEntity.setSecondData("second other entity - second data");
        secondOtherEntity.setDate(LocalDate.of(2021, Month.FEBRUARY, 25));

        return Arrays.asList(null, firstOtherEntity, secondOtherEntity);
    }

    private List<ParentEntity> getFilteredListBySearchValue(String searchValue) {
        Predicate<ParentEntity> parentEntityPredicate = new ParentEntityPredicate(searchValue);
        return parentEntityList.stream().filter(parentEntityPredicate).sorted(Comparator.comparing(ParentEntity::getId)).collect(Collectors.toList());
    }

    private DataTablesParameter getDataTablesParameter(int draw, int startIndex, int length, String searchValue) {
        DataTablesParameter dataTablesParameter = new DataTablesParameter();
        dataTablesParameter.setDraw(draw);
        dataTablesParameter.setStart(startIndex);
        dataTablesParameter.setLength(length);
        dataTablesParameter.setSearchValue(searchValue);
        dataTablesParameter.setColumns(columnList);
        dataTablesParameter.setOrder(orderList);
        return dataTablesParameter;
    }

    @Test
    void getDataTablesResponseTestDefault() {
        int draw = 1;
        int startIndex = 0;
        int length = 10;
        String searchValue = "first";
        dataTablesParameter = getDataTablesParameter(draw, startIndex, length, searchValue);
        dataTables = DataTables.of(ParentEntity.class, dataTablesParameter);
        dataTablesResponse = dataTables.getDataTablesResponse(entityManager);
        List<ParentEntity> filteredList = getFilteredListBySearchValue(searchValue);
        List<ParentEntity> resultList = IntStream.range(startIndex, startIndex + length).mapToObj(filteredList::get).collect(Collectors.toList());
        DataTablesResponse<ParentEntity> expected = new DataTablesResponse<>();
        expected.setDraw(dataTablesParameter.getDraw());
        expected.setData(resultList);
        expected.setResultList(resultList);
        expected.setRecordsFiltered(filteredList.size());
        expected.setRecordsTotal(parentEntityList.size());
        assertEquals(expected, dataTablesResponse);
    }

    @Test
    void getDataTablesResponseTestLengthIs25() {
        int draw = 1;
        int startIndex = 0;
        int length = 25;
        String searchValue = "first";
        dataTablesParameter = getDataTablesParameter(draw, startIndex, length, searchValue);
        dataTables = DataTables.of(ParentEntity.class, dataTablesParameter);
        dataTablesResponse = dataTables.getDataTablesResponse(entityManager);
        List<ParentEntity> filteredList = getFilteredListBySearchValue(searchValue);
        List<ParentEntity> resultList = IntStream.range(startIndex, startIndex + length).mapToObj(filteredList::get).collect(Collectors.toList());
        DataTablesResponse<ParentEntity> expected = new DataTablesResponse<>();
        expected.setDraw(dataTablesParameter.getDraw());
        expected.setData(resultList);
        expected.setResultList(resultList);
        expected.setRecordsFiltered(filteredList.size());
        expected.setRecordsTotal(parentEntityList.size());
        assertEquals(expected, dataTablesResponse);
    }

    @Test
    void getDataTablesResponseTestPageIs2() {
        int draw = 1;
        int startIndex = 10;
        int length = 10;
        String searchValue = "first";
        dataTablesParameter = getDataTablesParameter(draw, startIndex, length, searchValue);
        dataTables = DataTables.of(ParentEntity.class, dataTablesParameter);
        dataTablesResponse = dataTables.getDataTablesResponse(entityManager);
        List<ParentEntity> filteredList = getFilteredListBySearchValue(searchValue);
        List<ParentEntity> resultList = IntStream.range(startIndex, startIndex + length).mapToObj(filteredList::get).collect(Collectors.toList());
        DataTablesResponse<ParentEntity> expected = new DataTablesResponse<>();
        expected.setDraw(dataTablesParameter.getDraw());
        expected.setData(resultList);
        expected.setResultList(resultList);
        expected.setRecordsFiltered(filteredList.size());
        expected.setRecordsTotal(parentEntityList.size());
        assertEquals(expected, dataTablesResponse);
    }

    @Test
    void getDataTablesResponseTestPercentWildcardSearch() {
        int draw = 1;
        int startIndex = 0;
        int length = 10;
        String searchValue = "%";
        dataTablesParameter = getDataTablesParameter(draw, startIndex, length, searchValue);
        dataTables = DataTables.of(ParentEntity.class, dataTablesParameter);
        dataTablesResponse = dataTables.getDataTablesResponse(entityManager);
        List<ParentEntity> filteredList = getFilteredListBySearchValue(searchValue);
        List<ParentEntity> resultList = IntStream.range(startIndex, startIndex + length).mapToObj(filteredList::get).collect(Collectors.toList());
        DataTablesResponse<ParentEntity> expected = new DataTablesResponse<>();
        expected.setDraw(dataTablesParameter.getDraw());
        expected.setData(resultList);
        expected.setResultList(resultList);
        expected.setRecordsFiltered(filteredList.size());
        expected.setRecordsTotal(parentEntityList.size());
        assertEquals(expected, dataTablesResponse);
    }

    @Test
    void getDataTablesResponseTestUnderscoreWildcardSearch() {
        int draw = 1;
        int startIndex = 0;
        int length = 10;
        String searchValue = "_";
        dataTablesParameter = getDataTablesParameter(draw, startIndex, length, searchValue);
        dataTables = DataTables.of(ParentEntity.class, dataTablesParameter);
        dataTablesResponse = dataTables.getDataTablesResponse(entityManager);
        List<ParentEntity> filteredList = getFilteredListBySearchValue(searchValue);
        List<ParentEntity> resultList = IntStream.range(startIndex, startIndex + length).mapToObj(filteredList::get).collect(Collectors.toList());
        DataTablesResponse<ParentEntity> expected = new DataTablesResponse<>();
        expected.setDraw(dataTablesParameter.getDraw());
        expected.setData(resultList);
        expected.setResultList(resultList);
        expected.setRecordsFiltered(filteredList.size());
        expected.setRecordsTotal(parentEntityList.size());
        assertEquals(expected, dataTablesResponse);
    }
}
