package com.io.codesystem.medicine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.io.codesystem.versionsummary.VersionSummary;

@Repository
public interface MedicineVersionSummaryRepository extends JpaRepository<VersionSummary, Integer> {

	@Query("SELECT COUNT(*) FROM MedicinePostSyncResultsModel m WHERE m.versionState = 'Validated'")
	public int currentVersionMedicineTotalRecords(@Param("fileId") Integer fileId);
}
