package com.example.inventory.repository;

import com.example.inventory.entity.Shelf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShelfRepository extends JpaRepository<Shelf, Long> {
    void deleteByRouterId(Long routerId);

    List<Shelf> findByRouterId(Long routerId);
}
