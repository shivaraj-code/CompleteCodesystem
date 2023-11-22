package com.io.codesystem.pharmacy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PharmacySyncResultsRepository extends JpaRepository<PharmacySyncResultsModel, Integer> {

	@Query(value = "CALL PharmacyCompareAndSyncTablesAdded(:file_id,:file_name,:user_id)", nativeQuery = true)
	public PharmacySyncResultsModel pharmacyCompareAndSyncTablesForAdded(Integer file_id, String file_name,
			Integer user_id);

	@Query(value = "CALL PharmacyCompareAndSyncTablesUpdated(:IN_file_id,:file_name,:user_id)", nativeQuery = true)
	public PharmacySyncResultsModel pharmacyCompareAndSyncTablesForUpdated(Integer IN_file_id, String file_name,
			Integer user_id);

	@Query(value = "CALL PharmacyCompareAndSyncTablesDeleted(:IN_file_id,:file_name,:user_id)", nativeQuery = true)
	public PharmacySyncResultsModel pharmacyCompareAndSyncTablesForDeleted(Integer IN_file_id, String file_name,
			Integer user_id);

	@Query("select p from PharmacyPostSyncResultsModel p where(p.ncpdpid =:searchTerm)and versionState='Validated'")
	public Page<PharmacyPostSyncResultsModel> getPharmaciesVerificationSearch(String searchTerm, Pageable pageable);
}