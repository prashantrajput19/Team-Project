package com.examly.springapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.examly.springapp.model.Architect;

@Repository
public interface ArchitectRepo extends JpaRepository<Architect, Long> {
    List<Architect> findByDeletedFalse();

    @Query(value = "SELECT * FROM architect "
            + "WHERE deleted = false AND status = 'Active' "
            + "ORDER BY rating DESC "
            + "LIMIT 3", nativeQuery = true)
    List<Architect> findTopRated();

    @Query("SELECT DISTINCT a.location FROM Architect a WHERE a.deleted = false ORDER BY a.location")
    List<String> findAllLocations();
}
