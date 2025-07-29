package com.techpool.muncipality.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.techpool.muncipality.entity.DemandRegister;

public interface DemandRegisterRepository extends JpaRepository<DemandRegister, Long> {
    @Query("SELECT d FROM DemandRegister d WHERE d.int_year_id = :year")
    List<DemandRegister> findByYear(@Param("year") int year);
}