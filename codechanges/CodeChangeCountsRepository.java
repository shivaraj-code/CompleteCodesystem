package com.io.codesystem.codechanges;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CodeChangeCountsRepository extends JpaRepository<CodeChangeCounts, Integer> {

	@Query("select m from CodeChangeCounts m where(m.fileId =:fileId and m.status=:status)")
	public CodeChangeCounts findByStatus(Integer fileId, String status);

	@Modifying
	@Transactional
	@Query("DELETE FROM CodeChangeCounts c WHERE c.fileId = :fileId")
	void deleteByFileId(@Param("fileId") Integer fileId);

}
