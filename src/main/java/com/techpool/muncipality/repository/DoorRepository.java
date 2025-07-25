package com.techpool.muncipality.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techpool.muncipality.entity.BuildingMaster;

public interface DoorRepository extends JpaRepository<BuildingMaster, Long> {
}
