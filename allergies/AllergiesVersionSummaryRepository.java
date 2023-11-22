package com.io.codesystem.allergies;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.io.codesystem.versionsummary.VersionSummary;

@Repository
public interface AllergiesVersionSummaryRepository extends JpaRepository<VersionSummary, Integer>{

	@Query("SELECT COUNT(*) FROM PostSyncAllergiesResultsModel a WHERE a.versionState = 'Validated'")
	public int currentVersionAllergiesTotalRecords(@Param("fileId") int fileId);

}
