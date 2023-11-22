package com.io.codesystem.medicine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MedicineStandardRepository extends JpaRepository<MedicineStandardModel, Integer> {
	
	@Query(value="CALL PrepareMedicineDataForVerification(:file_id,:file_name,:user_id)",nativeQuery=true)
	public void prepareMedicineDataForVerification(Integer file_id,String file_name,Integer user_id);

}