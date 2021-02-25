import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DataTablesTest {
    private DataTables<ValidEntity> dataTables;
    private DataTablesParameter dataTablesParameter;
    private Column id;
    private Column data;
    private Column firstSubEntity;
    private Column secondSubEntity;

    @BeforeEach
    public void setup() {
        dataTablesParameter = new DataTablesParameter();
        dataTables = new DataTables<>(ValidEntity.class, dataTablesParameter);

        dataTablesParameter.setColumnList(getColumnList());
    }

    private List<Column> getColumnList() {
        id = new Column();
        data = new Column();
        firstSubEntity = new Column();
        secondSubEntity = new Column();

        id.setData("id");

        data.setData("data");

        firstSubEntity.setData("firstSubEntity.firstData");

        secondSubEntity.setData("secondSubEntity?.firstData + secondSubEntity?.secondData");

        return List.of(id, data, firstSubEntity, secondSubEntity);
    }

    @Test
    public void isEntityTest() {
        assertTrue(DataTables.isEntity(ValidEntity.class));
        assertFalse(DataTables.isEntity(InvalidEntity.class));
    }

    @Test
    public void datatablesConstructionInvalidEntity() {
        assertThrows(IllegalArgumentException.class, () -> new DataTables<>(InvalidEntity.class, new DataTablesParameter()));
    }

    @Test
    public void datatablesConstructionInvalidDataTablesParameters() {
        assertThrows(NullPointerException.class, () -> new DataTables<>(ValidEntity.class, null));
    }

    @Test
    public void getLeftJoinClauseTest() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" Left Join ");
        stringBuilder.append("valid_0.");
        stringBuilder.append(firstSubEntity.getRelationshipFieldName());
        stringBuilder.append(" first_1");
        assertEquals(stringBuilder.toString(), dataTables.getLeftJoinClause(firstSubEntity));
        assertEquals("", dataTables.getLeftJoinClause(secondSubEntity));
    }
}
