package com.example.inventory.repository;

import com.example.inventory.entity.Router;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouterRepository extends JpaRepository<Router, Long> {
    void deleteBySiteId(Long siteId);

    List<Router> findBySiteId(Long siteId);
}
