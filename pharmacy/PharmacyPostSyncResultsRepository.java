package com.io.codesystem.pharmacy;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
@Repository
public interface PharmacyPostSyncResultsRepository extends JpaRepository<PharmacyPostSyncResultsModel, Integer>{

	@Query(value="CALL GetPharmacyPostSyncData(:file_id,:status)",nativeQuery=true)
	public List<PharmacyPostSyncResultsModel> pharmacyPostSyncDataResults(Integer file_id,String status);

	@Query(value="CALL PharmacyPostSyncSearch(:file_id,:ncpdp_id,:name, :address, :zip, :status)",nativeQuery=true)
	public List<PharmacyPostSyncResultsModel> getPharmacyDataPostSync(Integer file_id, String ncpdp_id, String name,
			String address, String zip, String status);

	@Query(value="CALL PharmacyPostSyncSearchNcpdpid(:file_id,:ncpdp_id, :status)",nativeQuery=true)
	public List<PharmacyPostSyncResultsModel> getPharmacyDataPostSyncNcpdpId(Integer file_id, String ncpdp_id, String status);

	@Query(value="CALL PharmacyPostSyncSearchName(:file_id,:name, :status)",nativeQuery=true)
	public List<PharmacyPostSyncResultsModel> getPharmacyDataPostSyncName(Integer file_id, String name, String status);

	@Query(value="CALL PharmacyPostSyncSearchAddress(:file_id,:address, :status)",nativeQuery=true)
	public List<PharmacyPostSyncResultsModel> getPharmacyDataPostSyncAddress(Integer file_id, String address, String status);
	
	@Query(value="CALL PharmacyPostSyncSearchZip(:file_id,:zip, :status)",nativeQuery=true)
	public List<PharmacyPostSyncResultsModel> getPharmacyDataPostSyncZip(Integer file_id, String zip, String status);

	@Query("SELECT p FROM PharmacyPostSyncResultsModel p WHERE p.versionState = 'Validated' and p.status='Y'")
	public Page<PharmacyPostSyncResultsModel> findValidatedRecords(Pageable pageable);


	
}
