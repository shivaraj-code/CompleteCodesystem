package com.io.codesystem.medicine;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicineDataVerificationRepository extends JpaRepository<MedicineDataVerificationModel, Integer> {

	@Query(value = "CALL GetMedicineVerificationDetails(:fileid,:searchterm,:status)", nativeQuery = true)
	public List<MedicineDataVerificationModel> getMedicineVerificationDetails(Integer fileid, String searchterm,
			String status);

	public MedicineDataVerificationModel findByNdc(String ndc);

}