package com.techpool.muncipality.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techpool.muncipality.entity.Door;

public interface DoorRepository extends JpaRepository<Door, Long> {
}
