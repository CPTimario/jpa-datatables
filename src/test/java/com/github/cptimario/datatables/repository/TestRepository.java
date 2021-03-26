package com.github.cptimario.datatables.repository;

import com.github.cptimario.datatables.entity.ParentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRepository extends JpaRepository<ParentEntity, Integer> {
}
