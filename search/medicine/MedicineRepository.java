package com.io.codesystem.search.medicine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Integer> {

	

	 
//@Query("select m from Medicine m where(m.name like %:NDCorName% or m.ndc like %:NDCorName%)AND m.versionState='Valid'")
// @Query(value = "select * from medicines_new where match(name,ndc) against(:NDCorName) AND version_state='Valid'", nativeQuery = true)
//public Page<Medicine> getNDCorName(String NDCorName, Pageable pageable);

}
