package com.example.inventory.repository;

import com.example.inventory.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CardRepository extends JpaRepository<Card, Long> {
    void deleteBySlotId(Long slotId);

    boolean existsBySlotId(Long slotId);

    boolean existsBySerialNumber(String serialNumber);

    @Query("""
        SELECT c FROM Card c
        WHERE (:status IS NULL OR c.status=:status)
    """)
    Page<Card> findAllByFilter(
            Pageable pageable,
            @Param("status") String status);

    boolean existsBySerialNumberAndIdNot(String serialNumber, Long id);

    boolean existsBySlotIdAndIdNot(Long slotId, Long id);
}
