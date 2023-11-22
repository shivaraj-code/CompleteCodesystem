package com.io.codesystem.medicine;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.cognitoidp.model.HttpHeader;
import com.io.codesystem.cpt.CptPostSyncResultsModel;
import com.io.codesystem.utils.TableRecordCounts;
import com.io.codesystem.versionsummary.VersionSummary;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
public class MedicineMaintenanceController {

	@Autowired
	private MedicineMaintenanceService medicineMaintenanceService;

	@GetMapping(path = "/verification/medicine")
	public ResponseEntity<Page<MedicineDataVerificationModel>> getNDCorName(@RequestParam Integer fileId,
			@RequestParam String searchTerm,
			@RequestParam String status, 
			@RequestParam int pageSize,
			@RequestParam int pageNumber) {

		HttpHeaders headers = new HttpHeaders();
		Page<MedicineDataVerificationModel> response = medicineMaintenanceService.getNDCorName(fileId, searchTerm,
				status, pageSize, pageNumber);
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}

	@GetMapping("/postsyncresults/medicine")
	public ResponseEntity<Page<MedicinePostSyncResultsModel>> getMedicinePostSyncresults(@RequestParam int fileId,
			@RequestParam String status,
			@RequestParam int pageSize,
			@RequestParam int pageNumber) {
		HttpHeaders headers = new HttpHeaders();
		Page<MedicinePostSyncResultsModel> response = medicineMaintenanceService.getMedicinePostSyncresults(fileId,
				status, pageSize, pageNumber);
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}

	@GetMapping("/aftersync/search/medicine")
	public ResponseEntity<Page<MedicinePostSyncResultsModel>> getMedicineSearchAfterSync(@RequestParam Integer fileId,
			@RequestParam String searchTerm, 
			@RequestParam String status, 
			@RequestParam int pageSize,
			@RequestParam int pageNumber) {
		HttpHeaders headers = new HttpHeaders();
		Page<MedicinePostSyncResultsModel> Response = medicineMaintenanceService.getMedicineSearchAfterSync(fileId,
				searchTerm, status, pageSize, pageNumber);
		return new ResponseEntity<>(Response, headers, HttpStatus.OK);
	}

	@GetMapping("/verfication/medicinesSearch")
	public ResponseEntity<Page<MedicinePostSyncResultsModel>> getMedicinesVerificationSearch(
			@RequestParam String searchTerm,
			@RequestParam int pageSize,
			@RequestParam int pageNumber) {
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		HttpHeaders headers = new HttpHeaders();
		Page<MedicinePostSyncResultsModel> medResponse = medicineMaintenanceService
				.getMedicinesVerificationSearch(searchTerm, pageable);
		return new ResponseEntity<>(medResponse, headers, HttpStatus.OK);

	}

	@GetMapping("/tableRecordCounts/medicine")
	public ResponseEntity<List<TableRecordCounts>> getTableRecordCounts() {
		List<TableRecordCounts> tableRecordCounts = medicineMaintenanceService.getTableRecordCounts();
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(tableRecordCounts, headers, HttpStatus.OK);

	}
	
	@GetMapping("/totalrecords/medicine")
	public ResponseEntity<Page<MedicinePostSyncResultsModel>> getTotalCount(
	        @RequestParam int pageSize,
	        @RequestParam int pageNumber) {
	    Pageable pageable = PageRequest.of(pageNumber, pageSize);
	    Page<MedicinePostSyncResultsModel> totalCounts = medicineMaintenanceService.getAllCount(pageable);
	    HttpHeaders headers = new HttpHeaders();
	    return new ResponseEntity<>(totalCounts, headers, HttpStatus.OK);
	}

	@GetMapping("/version/summary/medicine")
	public ResponseEntity<List<VersionSummary>> getMedicineVersionSummaryData(@RequestParam int fileId,
			@RequestParam int userId) {

		HttpHeaders headers = new HttpHeaders();
		List<VersionSummary> response = medicineMaintenanceService.getMedicineVersionSummaryData(fileId, userId);
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}
}