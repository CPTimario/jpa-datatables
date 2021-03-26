package io.github.cptimario.datatables;

import io.github.cptimario.datatables.entity.ChildEntity;
import io.github.cptimario.datatables.entity.OtherEntity;
import io.github.cptimario.datatables.entity.ParentEntity;

import java.util.Objects;
import java.util.function.Predicate;

public class ParentEntityPredicate implements Predicate<ParentEntity> {
    private final String searchValue;

    public ParentEntityPredicate(String searchValue) {
        this.searchValue = searchValue;
    }

    @Override
    public boolean test(ParentEntity parentEntity) {
        ChildEntity childEntity = parentEntity.getChildEntity();
        OtherEntity otherEntity = parentEntity.getOtherEntity();
        String id = parentEntity.getId().toString();
        String data = parentEntity.getData();
        String date = parentEntity.getDate().toString();
        String childFirstData = Objects.nonNull(childEntity) ? childEntity.getFirstData() : "";
        String childDate = Objects.nonNull(childEntity) ? childEntity.getDate().toString() : "";
        String otherEntityData = Objects.nonNull(otherEntity) ? otherEntity.getFirstData() + " " + otherEntity.getSecondData() : "";
        return isLikeSearchValue(id, data, date, childFirstData, childDate, otherEntityData);
    }

    private boolean isLikeSearchValue(String... values) {
        for (String text : values) {
            if (text.toUpperCase().contains(searchValue.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
}
