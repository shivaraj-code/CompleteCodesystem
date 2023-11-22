package com.io.codesystem.search.cpt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CptCodeCategorySearchRepository extends JpaRepository<CptCodeCategory, Long> {

	// @Query(value = "CALL getCptCodeCategorySearch(:codeorshort)", nativeQuery =
	// true)
	// public List<CptCodeCategory> getCptCodeByCodeOrShortWithCtg(String
	// codeorshort);

}
