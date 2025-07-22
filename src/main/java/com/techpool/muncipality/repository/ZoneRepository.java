package com.techpool.muncipality.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techpool.muncipality.entity.Zone;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
}
