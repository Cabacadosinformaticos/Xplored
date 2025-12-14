package com.Xplored.Xplored.Model.Pedipaper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedipaperRepository extends JpaRepository<Pedipaper, Long> {
    // Basic CRUD is enough
}