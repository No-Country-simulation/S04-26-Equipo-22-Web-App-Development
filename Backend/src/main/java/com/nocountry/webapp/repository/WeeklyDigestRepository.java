package com.nocountry.webapp.repository;

import com.nocountry.webapp.entity.WeeklyDigest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeeklyDigestRepository extends JpaRepository<WeeklyDigest, Long> {
    
}