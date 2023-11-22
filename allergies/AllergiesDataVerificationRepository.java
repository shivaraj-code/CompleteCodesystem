package com.io.codesystem.allergies;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AllergiesDataVerificationRepository extends JpaRepository<AllergiesDataVerificationModel, Integer> {

	@Query(value = "CALL GetAllergiesCodeVerificationDetails(:fileid,:searchterm,:status)", nativeQuery = true)
	List<AllergiesDataVerificationModel> getAllergiesCodeVerificationDetails(Integer fileid, String searchterm,
			String status);

	@Query("select a from AllergiesDataVerificationModel a where a.damConceptIdDesc=:damConceptIdDesc and a.snomedCode=:snomedCode ")
	public AllergiesDataVerificationModel findByDamConceptIdDesc(String damConceptIdDesc, String snomedCode);

}
