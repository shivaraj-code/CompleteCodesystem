package com.io.codesystem.medicine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.io.codesystem.icd.IcdSyncResultsModel;

@Repository
public interface MedicineSyncResultsRepository extends JpaRepository<MedicineSyncResultsModel, Integer> {

	@Query(value = "CALL MedicineAddedSyncProcedure(:file_id,:file_name,:user_id)", nativeQuery = true)
	public MedicineSyncResultsModel AddedMedicineSynchProcedure(Integer file_id, String file_name, Integer user_id);

	@Query(value = "CALL MedicneUpdatedSyncProcedure(:IN_file_id,:file_name,:user_id)", nativeQuery = true)
	public MedicineSyncResultsModel UpdatedMedicneSynchProcedure_Batch(Integer IN_file_id, String file_name, Integer user_id);

	@Query(value = "CALL MedicineDeletedSyncProcedure(:IN_file_id,:file_name,:user_id)", nativeQuery = true)
	public MedicineSyncResultsModel DeletedMedicineSynchProcedure(Integer IN_file_id, String file_name, Integer user_id);
}
