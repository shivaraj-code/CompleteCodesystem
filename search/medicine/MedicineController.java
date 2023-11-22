package com.io.codesystem.search.medicine;

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
public class MedicineController {

	@Autowired
	private MedicineService medicineservice;

	@PostMapping("/medicine/index")
	public ResponseEntity<String> createMedicineIndex() {

		String response = "Medicine Index created successfully";
		try {
			medicineservice.createMedicineIndex();
		} catch (Exception e) {
			response = "Failed to create Medicine Index";
			e.printStackTrace();
			return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping("/medicine/search")
	public ResponseEntity<Page<Medicine>> searchMedicine(@RequestParam String searchTerm, Pageable pageable) {

		Page<Medicine> searchResults = null;

		try {
			//medicineservice.createMedicineIndex();
			searchResults = medicineservice.searchMedicine(searchTerm, pageable);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return new ResponseEntity<>(searchResults, new HttpHeaders(), HttpStatus.OK);
	}

}
