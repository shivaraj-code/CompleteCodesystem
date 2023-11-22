package com.io.codesystem.versionsummary;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.io.codesystem.codechanges.CodeChangeCounts;

@Repository
public interface VersionSummaryRepository extends JpaRepository<VersionSummary, Integer> {

	@Query(value = "SELECT * FROM version_summary as vs WHERE vs.code_standard = :codeStandard ORDER BY release_date DESC LIMIT 1", nativeQuery = true)
	VersionSummary findOrderByCodeStandard(@Param("codeStandard") String codeStandard);

	@Query(value = "SELECT cc FROM CodeChangeCounts cc WHERE cc.fileId=:fileId AND cc.status=:status")
	CodeChangeCounts findByfileIdAndStatus(@Param("fileId") int fileId, @Param("status") String status);

	
	Page<VersionSummary> findOrderByCodeStandard(String codeStandard, Pageable pageable);

	
//	@Query(value = "SELECT added_records,updated_records,deleted_records  FROM code_change_counts as cc WHERE cc.file_id=:fileId AND cc.status=:status", nativeQuery = true)
//	CodeChangeCounts findByfileIdAndStatus(int fileId, String status);
}