package com.io.codesystem.icd;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.io.codesystem.versionsummary.VersionSummary;

@Repository
public interface IcdVersionSummaryRepository extends JpaRepository<VersionSummary, Integer> {

	@Query("SELECT COUNT(*) FROM IcdPostSyncResultsModel i WHERE i.versionState = 'Validated'")
	public int currentVersionIcdTotalRecords(@Param("fileId") int fileId);

}
