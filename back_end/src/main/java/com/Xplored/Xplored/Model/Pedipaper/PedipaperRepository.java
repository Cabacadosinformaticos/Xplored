package com.Xplored.Xplored.Model.Pedipaper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedipaperRepository extends JpaRepository<Pedipaper, Long> {
    // Only the routes that are active show up in the app.
    List<Pedipaper> findByActiveTrue();
}