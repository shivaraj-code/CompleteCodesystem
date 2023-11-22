package com.io.codesystem.allergies;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.io.codesystem.utils.TableRecordCounts;

public interface AllergiesTableRecordCountsRepository extends JpaRepository<TableRecordCounts, Integer> {

	@Query(value = "select 1 as id, 'Existing Table' as table_name,count(*) as records_count from allergies_new where status='Y' and version_state='Validated'"
			+ "union "
			+ "select 2 as id, 'Dump Table' as table_name,count(*) as records_count from allergies_standard_versions", nativeQuery = true)
	public List<TableRecordCounts> getTableRecordCounts();
}
