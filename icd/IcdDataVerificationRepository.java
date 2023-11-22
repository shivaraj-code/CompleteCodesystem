package com.io.codesystem.icd;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IcdDataVerificationRepository extends JpaRepository<IcdDataVerificationModel, Integer> {

	@Query(value = "CALL GetIcdCodeVerificationDetails(:fileid,:searchterm,:status)", nativeQuery = true)
	public List<IcdDataVerificationModel> getIcdCodeVerificationDetails(Integer fileid, String searchterm,
			String status);

	//public IcdDataVerificationModel findByIcd10Code(String icd10code);

	public IcdDataVerificationModel findByIcd10id(Integer icd10id);

}
