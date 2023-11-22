package com.io.codesystem.pharmacy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.io.codesystem.versionsummary.VersionSummary;

@Repository
public interface PharmacyVersionSummaryRepository extends JpaRepository<VersionSummary, Integer> {

	@Query("SELECT COUNT(*) FROM PharmacyPostSyncResultsModel p WHERE p.versionState = 'Validated'")
	public int currentVersionPharmacyTotalRecords(@Param("fileId") int fileId);

}
