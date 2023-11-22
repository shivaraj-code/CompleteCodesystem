package com.io.codesystem.icd;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IcdCodeStandardRepository extends JpaRepository<IcdCodeStandardModel, Integer> {
	
	@Query(value="CALL PrepareIcdDataForVerification(:file_id,:file_name,:user_id)",nativeQuery=true)
	public void prepareIcdDataForVerification(Integer file_id,String file_name,Integer user_id);

}
