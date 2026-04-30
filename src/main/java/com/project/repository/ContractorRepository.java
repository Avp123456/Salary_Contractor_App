package com.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.entity.Contractor;

public interface ContractorRepository extends JpaRepository<Contractor, Long> {
	    Contractor findByEmailAndPassword(String email, String password);
	    Contractor findByEmail(String email);
	}


