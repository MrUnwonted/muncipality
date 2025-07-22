package com.techpool.muncipality.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techpool.muncipality.entity.Ward;

public interface WardRepository extends JpaRepository<Ward, Long> {}