package com.io.codesystem.search.pharmacy;

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
public class PharmaciesController {

	@Autowired
	PharmacySearchService pharmImplem;

	@PostMapping("/pharmacies/index")
	public ResponseEntity<String> createPharmacyIndex() {

		String response = "Pharmacies Index created successfully";

		try {
			pharmImplem.createPharmacyIndex();
		} catch (Exception e) {
			response = "Failed to create Pharmacies Index";
			return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}
	
	@GetMapping("/pharmacies/search")
	public ResponseEntity<Page<Pharmacies>> searchPharmacy(@RequestParam String searchTerm,
	        @RequestParam(defaultValue = " ") String filter, Pageable pageable) {
	   
		Page<Pharmacies> searchResults = null;
		try {
	       // pharmImplem.createPharmacyIndex(); // Create the pharmacy index if necessary
	        if (filter.isBlank()) {
	            searchResults = pharmImplem.searchPharmacy(searchTerm, pageable);
	        } else if (filter.equalsIgnoreCase("zip")) {
	            searchResults = pharmImplem.searchPharmacyZip(searchTerm, pageable);
	        } else if (filter.equalsIgnoreCase("name")) {
	            searchResults = pharmImplem.searchPharmacyName(searchTerm, pageable);
	        } else if (filter.equalsIgnoreCase("address")) {
	            searchResults = pharmImplem.searchPharmacyAddress(searchTerm, pageable);
	        } else if (filter.equalsIgnoreCase("ncpdpid")) {
	            searchResults = pharmImplem.searchPharmacyNcpdpid(searchTerm, pageable);
	        }

	    } catch (Exception e) {
	        e.printStackTrace(); // Print the exception stack trace
	        
	    }
	    return new ResponseEntity<>(searchResults, new HttpHeaders(), HttpStatus.OK);
	}



/*	@GetMapping("/pharmacies/search")
	public ResponseEntity<Page<Pharmacies>> searchPharmacy(@RequestParam String searchTerm,
			@RequestParam(defaultValue = " ") String filter, Pageable pageable) {

		Page<Pharmacies> searchResults = null;

		if (filter.isBlank()) {
			searchResults = pharmImplem.searchPharmacy(searchTerm, pageable);
		} else if (filter.equalsIgnoreCase("zip")) {
			searchResults = pharmImplem.searchPharmacyZip(searchTerm, pageable);
		} else if (filter.equalsIgnoreCase("name")) {
			searchResults = pharmImplem.searchPharmacyName(searchTerm, pageable);
		} else if (filter.equalsIgnoreCase("address")) {
			searchResults = pharmImplem.searchPharmacyAddress(searchTerm, pageable);
		} else if (filter.equalsIgnoreCase("ncpdpid")) {
			searchResults = pharmImplem.searchPharmacyNcpdpid(searchTerm, pageable);
		}

		return new ResponseEntity<>(searchResults, new HttpHeaders(), HttpStatus.OK);

	}
*/
	
}
