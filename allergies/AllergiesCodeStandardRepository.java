package com.io.codesystem.allergies;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AllergiesCodeStandardRepository extends JpaRepository<AllergiesCodeStandardModel, Integer> {

	@Query(value = "CALL PrepareAllergiesDataForVerification(:file_id,:file_name,:user_id)", nativeQuery = true)
	public void prepareAllergiesDataForVerification(Integer file_id, String file_name, Integer user_id);

}
