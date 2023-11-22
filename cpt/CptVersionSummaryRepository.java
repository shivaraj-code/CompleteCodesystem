package com.io.codesystem.cpt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.io.codesystem.versionsummary.VersionSummary;

@Repository
public interface CptVersionSummaryRepository extends JpaRepository<VersionSummary, Integer> {

	@Query("SELECT COUNT(*) FROM CptPostSyncResultsModel c WHERE c.versionState = 'Validated'")
	public int currentVersionCptTotalRecords(@Param("fileId") int fileId);

}
