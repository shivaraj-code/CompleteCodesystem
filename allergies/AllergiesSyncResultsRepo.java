package com.io.codesystem.allergies;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AllergiesSyncResultsRepo extends JpaRepository<AllergiesSyncResults, Integer> {

	@Query(value = "CALL AllergiesCompareAndSyncTablesAdded(:file_id,:file_name,:user_id)", nativeQuery = true)
	public AllergiesSyncResults allergiesCompareAndSyncTablesForAdded(Integer file_id, String file_name,
			Integer user_id);

	@Query(value = "CALL AllergiesCompareAndSyncTablesBatchUpdatedWithConcat(:IN_file_id,:file_name,:user_id)", nativeQuery = true)
	public AllergiesSyncResults allergiesCompareAndSyncTablesForUpdated(Integer IN_file_id, String file_name,
			Integer user_id);

	@Query(value = "CALL AllergiesCompareAndSyncTablesDeleted(:IN_file_id,:file_name,:user_id)", nativeQuery = true)
	public AllergiesSyncResults allergiesCompareAndSyncTablesForDeleted(Integer IN_file_id, String file_name,
			Integer user_id);

}
