package com.io.codesystem.pharmacy;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
@Repository
public interface PharmacyDataVerificationRepository extends JpaRepository<PharmacyDataVerificationModel, Integer>{

	@Query(value="CALL GetPharmacyVerificationDetails(:file_id,:status)",nativeQuery=true)
	public List<PharmacyDataVerificationModel> getPharmacyVerificationDetails(Integer file_id,  String status);
	
	@Query(value="CALL GetPharmacyVerificationDetailsNcpdpId(:file_id,:ncpdp_id,:status)",nativeQuery=true)
	public List<PharmacyDataVerificationModel> getPharmacyVerificationDetailsNcpdpId(Integer file_id, String ncpdp_id, String status);
	
	@Query(value="CALL GetPharmacyVerificationDetailsName(:file_id,:name,:status)",nativeQuery=true)
	public List<PharmacyDataVerificationModel> getPharmacyVerificationDetailsName(Integer file_id, String name, String status);
	
	@Query(value="CALL GetPharmacyVerificationDetailsAddress(:file_id,:address,:status)",nativeQuery=true)
	public List<PharmacyDataVerificationModel> getPharmacyVerificationDetailsAddress(Integer file_id, String address, String status);

	@Query(value="CALL GetPharmacyVerificationDetailsZip(:file_id,:zip,:status)",nativeQuery=true)
	public List<PharmacyDataVerificationModel> getPharmacyVerificationDetailsZip(Integer file_id, String zip, String status);


	public PharmacyDataVerificationModel findByNcpdpid(String ncpdpCode);

	public List<PharmacyDataVerificationModel> findByZip(String searchTerm);

    public List<PharmacyDataVerificationModel> findByName(String searchTerm);

	public List<PharmacyDataVerificationModel> findByAddress1(String searchTerm);

	
}