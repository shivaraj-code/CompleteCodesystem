package com.io.codesystem.search.allergies;

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
public class AllergiesController {

	@Autowired
	Allergieservice allergiesService;

	@GetMapping("/allergies/search")
	public ResponseEntity<Page<Allergies>> searchAllergiesCode(@RequestParam String searchTerm, Pageable pageable) {
		Page<Allergies> searchResults = null;
		
		try {
			//allergiesService.createAllergiesIndex();
			searchResults = allergiesService.searchAllergiesCode(searchTerm, pageable);

		} catch (Exception ex) {

			ex.printStackTrace();
		}

		return new ResponseEntity<>(searchResults, new HttpHeaders(), HttpStatus.OK);

	}

	@PostMapping("/allergies/index")
	public ResponseEntity<String> createAllergiesIndex() {

		String response = "Allergies Index created successfully";

		try {
			allergiesService.createAllergiesIndex();
		} catch (Exception e) {

			response = "Failed to create Allergies Index";

			e.printStackTrace();
			return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

}
