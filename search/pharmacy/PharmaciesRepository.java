package com.io.codesystem.search.pharmacy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PharmaciesRepository extends JpaRepository<Pharmacies, Integer>{

	
	
}
