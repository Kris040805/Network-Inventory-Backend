package com.example.inventory.repository;

import com.example.inventory.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SlotRepository extends JpaRepository<Slot, Long> {
    void deleteByShelfId(Long shelfId);

    List<Slot> findByShelfId(Long shelfId);
}
