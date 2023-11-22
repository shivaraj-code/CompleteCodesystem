package com.io.codesystem.pharmacy;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.io.codesystem.allergies.PostSyncAllergiesResultsModel;
import com.io.codesystem.medicine.MedicinePostSyncResultsModel;
import com.io.codesystem.utils.TableRecordCounts;
import com.io.codesystem.versionsummary.VersionSummary;

@RestController
public class PharmacyCodeMaintenanceController {

	@Autowired
	PharmacyCodeMaintenanceService pharmacyCodeMaintenanceService;

	@GetMapping("/verification/pharmacy")
	public ResponseEntity<Page<PharmacyDataVerificationModel>> searchPharmacyFromVerification(
			@RequestParam Integer fileId, @RequestParam String searchTerm, @RequestParam String filter,
			@RequestParam String status, @RequestParam(defaultValue = "10") int pageSize,
			@RequestParam(defaultValue = "0") int pageNo) {
		HttpHeaders headers = new HttpHeaders();
		Page<PharmacyDataVerificationModel> response = pharmacyCodeMaintenanceService
				.searchPharmacyFromVerification(fileId, searchTerm, filter, status, pageSize, pageNo);
		return new ResponseEntity<>(response, headers, HttpStatus.OK);
	}

	@GetMapping("/postsyncresults/pharmacy")
	public ResponseEntity<Page<PharmacyPostSyncResultsModel>> getPharmacyPostSyncresults(@RequestParam int fileId,
			@RequestParam String status, @RequestParam int pageSize, @RequestParam int pageNumber) {
		Page<PharmacyPostSyncResultsModel> response = pharmacyCodeMaintenanceService.getPharmacyPostSyncresults(fileId,
				status, pageSize, pageNumber);
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}

	@GetMapping("/tableRecordCounts/pharmacy")
	public ResponseEntity<List<TableRecordCounts>> getTableRecordCounts() {
		List<TableRecordCounts> tableRecordCounts = pharmacyCodeMaintenanceService.getTableRecordCounts();
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(tableRecordCounts, headers, HttpStatus.OK);

	}

	@GetMapping("/aftersync/search/pharmacy")
	public ResponseEntity<Page<PharmacyPostSyncResultsModel>> searchPharmacyAfterSync(@RequestParam Integer fileId,
			@RequestParam String searchTerm, @RequestParam String filter, @RequestParam String syncStatus,
			@RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "0") int pageNo) {
		HttpHeaders headers = new HttpHeaders();
		Page<PharmacyPostSyncResultsModel> response = pharmacyCodeMaintenanceService.searchPharmacyPostSync(fileId,
				searchTerm, filter, syncStatus, pageSize, pageNo);
		return new ResponseEntity<>(response, headers, HttpStatus.OK);
	}

	@GetMapping("/verfication/pharmaciesSearch")
	public ResponseEntity<Page<PharmacyPostSyncResultsModel>> getPharmaciesVerificationSearch(
			@RequestParam String searchTerm, @RequestParam int pageSize, @RequestParam int pageNumber) {
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		HttpHeaders headers = new HttpHeaders();
		Page<PharmacyPostSyncResultsModel> medResponse = pharmacyCodeMaintenanceService
				.getPharmaciesVerificationSearch(searchTerm, pageable);
		return new ResponseEntity<>(medResponse, headers, HttpStatus.OK);

	}

	@GetMapping("/totalrecords/pharmacies")
	public ResponseEntity<Page<PharmacyPostSyncResultsModel>> getTotalCount(@RequestParam int pageSize,
			@RequestParam int pageNumber) {
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Page<PharmacyPostSyncResultsModel> totalCounts = pharmacyCodeMaintenanceService.getAllCount(pageable);
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(totalCounts, headers, HttpStatus.OK);
	}

	@GetMapping("/version/summary/pharmacy")
	public ResponseEntity<List<VersionSummary>> getPharmacyVersionSummaryData(@RequestParam int fileId,
			@RequestParam int userId) {

		HttpHeaders headers = new HttpHeaders();
		List<VersionSummary> response = pharmacyCodeMaintenanceService.getPharmacyVersionSummaryData(fileId, userId);
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}
}
