package com.example.inventory.repository;

import com.example.inventory.entity.NetworkSite;
import com.example.inventory.entity.Router;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NetworkSiteRepository extends JpaRepository<NetworkSite, Long> {
    boolean existsBySiteCode(String siteCode);

    boolean existsBySiteCodeAndIdNot(String siteCode, Long id);

    boolean existsByIdAndRoutersIsNotEmpty(Long id);

    @Query("""
        SELECT s FROM NetworkSite s
        WHERE (:status IS NULL OR s.status = :status)
        AND (:city IS NULL OR s.city = :city)
    """)
    Page<NetworkSite> findAllByFilters(
            @Param("status") String status,
            @Param("city") String city,
            Pageable pageable);
}
