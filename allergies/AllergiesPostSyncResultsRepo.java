package com.io.codesystem.allergies;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AllergiesPostSyncResultsRepo extends JpaRepository<PostSyncAllergiesResultsModel, Integer> {

	@Query(value = "CALL GetAllergiesPostSyncData(:file_id,:status)", nativeQuery = true)
	public List<PostSyncAllergiesResultsModel> allergiesPostSyncDataResults(Integer file_id, String status);

	@Query(value = "CALL GetAllergiesAfterSyncDetails(:fileId,:searchTerm,:status)", nativeQuery = true)
	public List<PostSyncAllergiesResultsModel> getAllergiesSearchByAfterSync(Integer fileId, String searchTerm,
			String status);

	@Query(value = "CALL AllergiesCompareAndSearchWithSorceTable(:dam_concept_id,:dam_concept_id_type,:snomed_code,:snomed_concept,:status)", nativeQuery = true)
	public List<PostSyncAllergiesResultsModel> getAllergiesVerificationSearch(
			@Param("dam_concept_id") String damConceptId, @Param("dam_concept_id_type") Integer damConceptIdType,
			@Param("snomed_code") String snomedCode, @Param("snomed_concept") String snomedConcept,
			@Param("status") String status);

	@Query("SELECT a FROM PostSyncAllergiesResultsModel a WHERE a.versionState = 'Validated' and a.status='Y'")
	public Page<PostSyncAllergiesResultsModel> findValidatedRecords(Pageable pageable);

}
//@Query("select a from PostSyncAllergiesResultsModel a where(a.damConceptId
// like %:searchTerm% or a.snomedCode like %:searchTerm% or a.snomedConcept like
// %:searchTerm% ) and a.syncStatus = :status and versionState = 'Validated'")
// @Query("SELECT a FROM PostSyncAllergiesResultsModel a WHERE (a.damConceptId
// LIKE %:searchTerm% AND a.damConceptIdType LIKE %:searchTerm% AND a.snomedCode
// LIKE %:searchTerm% AND a.snomedConcept LIKE %:searchTerm%) AND a.syncStatus =
// :status AND a.versionState = 'Validated'")