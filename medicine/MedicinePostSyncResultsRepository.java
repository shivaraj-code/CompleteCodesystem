package com.io.codesystem.medicine;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.io.codesystem.allergies.AllergiesDataVerificationModel;

@Repository
public interface MedicinePostSyncResultsRepository extends JpaRepository<MedicinePostSyncResultsModel, Integer> {

	@Query(value = "CALL GetMedicinePostSyncData(:file_id,:status)", nativeQuery = true)
	List<MedicinePostSyncResultsModel> medicinePostSyncDataResults(Integer file_id, String status);

	@Query(value = "CALL GetMedicineVerificationDetailsAfterSync(:fileId,:searchTerm,:status)", nativeQuery = true)
	List<MedicinePostSyncResultsModel> getMedicineSearchAfterSync(Integer fileId, String searchTerm, String status);

	@Query("select m from MedicinePostSyncResultsModel m where(m.ndc =:searchTerm)and versionState='Validated'")
	Page<MedicinePostSyncResultsModel> getMedicinesVerificationSearch(String searchTerm, Pageable pageable);

	@Query("SELECT m FROM MedicinePostSyncResultsModel m WHERE m.versionState = 'Validated' and m.status='Y'")
    Page<MedicinePostSyncResultsModel> findValidatedRecords(Pageable pageable);
	
	}