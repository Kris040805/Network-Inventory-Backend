package com.example.inventory.repository;

import com.example.inventory.entity.Shelf;
import com.example.inventory.entity.Slot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SlotRepository extends JpaRepository<Slot, Long> {
    void deleteByShelfId(Long shelfId);

    List<Slot> findByShelfId(Long shelfId);

    boolean existsByShelfIdAndSlotNumber(Long shelfId, Integer slotNumber);

    @Query("""
        SELECT s FROM Slot s
        WHERE (:status IS NULL OR s.status=:status)
    """)
    Page<Slot> findAllByFilters(
            @Param("status") String status,
            Pageable pageable
    );

    boolean existsByShelfIdAndSlotNumberAndIdNot(Long shelfId, Integer slotNumber, Long id);
}
