package com.io.codesystem.cpt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CptCodeStandardRepository extends JpaRepository<CptCodeStandardModel, Integer> {

	@Query(value = "CALL PrepareCptDataForVerification(:file_id,:file_name,:user_id)", nativeQuery = true)
	public void prepareCptDataForVerification(Integer file_id, String file_name, Integer user_id);

}