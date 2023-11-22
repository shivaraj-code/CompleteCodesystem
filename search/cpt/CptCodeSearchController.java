package com.io.codesystem.search.cpt;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CptCodeSearchController {

	@Autowired
	CptCodeSearchService cptCodeSearchService;

	@GetMapping("/cpt/search")
	public ResponseEntity<Page<CptCode>> searchCptCode(@RequestParam String searchTerm, Pageable pageable) {

		Page<CptCode> searchResults = null;

		try {
			//cptCodeSearchService.createCptIndex();
			searchResults = cptCodeSearchService.searchCptCode(searchTerm, pageable);

		} catch (Exception ex) {

			ex.printStackTrace();
		}

		return new ResponseEntity<>(searchResults, new HttpHeaders(), HttpStatus.OK);

	}

	@PostMapping("/cpt/index")
	public ResponseEntity<String> createCptIndex() {

		String response = "Cpt Index created successfully";

		try {
			cptCodeSearchService.createCptIndex();
		} catch (Exception e) {
			response = "Failed to create CPT Index";
			e.printStackTrace();
			return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	/*
	 * @GetMapping("/cptcode/searchwithctg") public
	 * ResponseEntity<List<CptCodeCategory>> getCptCodeByCodeOrShortWithCtg(
	 * 
	 * @RequestParam(value = "searchstring", required = false) String codeorshort) {
	 * 
	 * HttpHeaders headers = new HttpHeaders(); List<CptCodeCategory>
	 * cptCodeResponse =
	 * cptCodeSearchService.getCptCodeByCodeOrShortWithCtg(codeorshort); return new
	 * ResponseEntity<>(cptCodeResponse, headers, HttpStatus.OK); }
	 */

}
