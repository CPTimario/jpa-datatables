import javax.persistence.*;
import java.util.List;

@Entity
public class ValidEntity {
    @Id
    Integer id;

    String data;

    @ManyToOne
    SubEntity firstSubEntity;

    @ManyToOne
    SubEntity secondSubEntity;
}
