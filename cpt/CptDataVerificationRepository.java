package com.io.codesystem.cpt;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CptDataVerificationRepository extends JpaRepository<CptDataVerificationModel, Integer> {

	@Query(value = "CALL GetCptCodeVerificationDetails(:fileId,:searchTerm,:status)", nativeQuery = true)
	public List<CptDataVerificationModel> getCptCodeVerificationDetails(Integer fileId, String searchTerm,
			String status);

	public CptDataVerificationModel findByCode(String code);

}