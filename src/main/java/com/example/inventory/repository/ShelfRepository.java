package com.example.inventory.repository;

import com.example.inventory.entity.Shelf;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShelfRepository extends JpaRepository<Shelf, Long> {
    void deleteByRouterId(Long routerId);

    List<Shelf> findByRouterId(Long routerId);

    boolean existsByRouterIdAndShelfNumber(Long routerId, Integer shelfNumber);

    boolean existsByRouterIdAndShelfNumberAndIdNot(Long routerId, Integer shelfNumber, Long id);

    @Query("""
        SELECT s FROM Shelf s
        WHERE (:status IS NULL OR s.status = :status)
    """)
    Page<Shelf> findAllByFilters(
            @Param("status") String status,
            Pageable pageable
    );

    boolean existsByIdAndSlotsIsNotEmpty(Long id);
}
