package com.io.codesystem.pharmacy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
@Repository
public interface PharmacyCodeStandardRepository extends JpaRepository<PharmacyCodeStandardModel, Integer>{
	
	@Query(value="CALL PreparePharmacyDataForVerification(:file_id,:file_name,:user_id)",nativeQuery=true)
	public void preparePharmacyDataForVerification(Integer file_id,String file_name,Integer user_id);

}