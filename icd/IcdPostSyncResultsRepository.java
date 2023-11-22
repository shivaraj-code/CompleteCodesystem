package com.io.codesystem.icd;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IcdPostSyncResultsRepository extends JpaRepository<IcdPostSyncResultsModel, Integer> {

	@Query(value = "CALL GetIcdPostSyncData(:file_id,:status)", nativeQuery = true)
	public List<IcdPostSyncResultsModel> icdPostSyncDataResults(Integer file_id, String status);

	@Query(value = "CALL GetIcdCodeVerificationDetailsAfterSync(:fileid,:searchterm,:status)", nativeQuery = true)
	public List<IcdPostSyncResultsModel> getIcdSearchByAfterSync(Integer fileid, String searchterm, String status);

	//@Query("select m from IcdPostSyncResultsModel m where (m.icd10id = :searchTerm) and m.versionState = 'Validated'")
	@Query("SELECT m FROM IcdPostSyncResultsModel m WHERE (CAST(m.icd10id AS string) = :searchTerm or m.icd10code=:searchTerm) AND m.versionState = 'Validated'")
	public Page<IcdPostSyncResultsModel> getIcdVerificationSearch(@Param("searchTerm") String searchTerm, Pageable pageable);

	@Query("SELECT i FROM IcdPostSyncResultsModel i WHERE i.versionState = 'Validated' and i.status='Y' and (i.type='V' or i.type='H') ")
	public Page<IcdPostSyncResultsModel> findValidatedRecords(Pageable pageable);

}
