package com.example.inventory.repository;

import com.example.inventory.entity.Router;
import com.example.inventory.entity.Shelf;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RouterRepository extends JpaRepository<Router, Long> {
    void deleteBySiteId(Long siteId);

    List<Router> findBySiteId(Long siteId);

    boolean existsByHostnameOrSerialNumber(String hostname, String serialNumber);

    @Query("""
        SELECT r FROM Router r
        WHERE (:status IS NULL OR r.status = :status)
    """)
    Page<Router> findAllByFilters(
            @Param("status") String status,
            Pageable pageable
    );

    boolean existsByHostnameAndIdNot(String hostname, Long id);

    boolean existsBySerialNumberAndIdNot(String serialNumber, Long id);

    boolean existsByIdAndShelvesIsNotEmpty(Long id);
}
