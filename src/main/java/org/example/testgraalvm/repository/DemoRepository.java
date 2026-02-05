package org.example.testgraalvm.repository;

import org.example.testgraalvm.entity.Demo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for Demo entity.
 * Provides standard CRUD operations and custom query methods.
 */
@Repository
public interface DemoRepository extends JpaRepository<Demo, Long> {

    /**
     * Find demos by name containing the given string (case-insensitive).
     */
    List<Demo> findByNameContainingIgnoreCase(String name);

    /**
     * Find demos by exact name.
     */
    List<Demo> findByName(String name);
}
