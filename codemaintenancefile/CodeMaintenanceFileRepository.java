package com.io.codesystem.codemaintenancefile;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeMaintenanceFileRepository extends JpaRepository<CodeMaintenanceFile, Integer> {

	@Query(value = "CALL GetCodeMaintenanceList (:processed_state, :code_standard)", nativeQuery = true)
	List<CodeMaintenanceFile> findByCodeStandardAndProcessedState(String processed_state, String code_standard);
}
