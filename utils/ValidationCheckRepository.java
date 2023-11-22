package com.io.codesystem.utils;

import java.sql.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ValidationCheckRepository extends JpaRepository<ValidationCheck, Integer> {

	@Query(value = "CALL GetVerificationStatus(:in_verification_type,:in_code_standard,:in_release_date)", nativeQuery = true)
	public ValidationCheck prepareVerificationStatus(String in_verification_type, String in_code_standard,
			Date in_release_date);

}
