package com.io.codesystem.cpt;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.io.codesystem.medicine.MedicinePostSyncResultsModel;
import com.io.codesystem.utils.TableRecordCounts;
import com.io.codesystem.versionsummary.VersionSummary;

@RestController
public class CptCodeMaintenanceController {

	@Autowired
	private CptCodeMaintenanceService cptCodeMaintenanceService;

	@GetMapping(path = "/verification/cpt")
	public ResponseEntity<Page<CptDataVerificationModel>> getCptCodeOrDescOrstatus(@RequestParam Integer fileId,
			@RequestParam String searchTerm, @RequestParam String status, @RequestParam int pageSize,
			@RequestParam int pageNumber)

	{

		HttpHeaders headers = new HttpHeaders();
		Page<CptDataVerificationModel> response = cptCodeMaintenanceService.getCptCodeOrDescOrstatus(fileId, searchTerm,
				status, pageSize, pageNumber);
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}

	@GetMapping("/aftersync/search/cpt")
	public ResponseEntity<Page<CptPostSyncResultsModel>> getCptSearchByAfterSync(@RequestParam Integer fileId,
			@RequestParam String searchTerm, @RequestParam String status, @RequestParam int pageSize,
			@RequestParam int pageNumber) {

		HttpHeaders headers = new HttpHeaders();
		Page<CptPostSyncResultsModel> Response = cptCodeMaintenanceService.getCptSearchByAfterSync(fileId, searchTerm,
				status, pageSize, pageNumber);
		return new ResponseEntity<>(Response, headers, HttpStatus.OK);
	}

	@GetMapping("/postsyncresults/cpt")
	public ResponseEntity<Page<CptPostSyncResultsModel>> getCptPostSyncresults(@RequestParam int fileId,
			@RequestParam String status, @RequestParam int pageSize, @RequestParam int pageNumber) {
		Page<CptPostSyncResultsModel> response = cptCodeMaintenanceService.getCptPostSyncresults(fileId, status,
				pageSize, pageNumber);
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}

	@GetMapping("/verfication/cptSearch")
	public ResponseEntity<Page<CptPostSyncResultsModel>> getCptVerificationSearch(
			@RequestParam String searchTerm,
			@RequestParam int pageSize, 
			@RequestParam int pageNumber) {
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		HttpHeaders headers = new HttpHeaders();
		Page<CptPostSyncResultsModel> medResponse = cptCodeMaintenanceService
				.getCptVerificationSearch(searchTerm, pageable);
		return new ResponseEntity<>(medResponse, headers, HttpStatus.OK);

	}

	@GetMapping("/tableRecordCounts/cpt")
	public ResponseEntity<List<TableRecordCounts>> getTableRecordCounts() {
		List<TableRecordCounts> tableRecordCounts = cptCodeMaintenanceService.getTableRecordCounts();
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(tableRecordCounts, headers, HttpStatus.OK);

	}

	@GetMapping("/totalrecords/cpt")
	public ResponseEntity<Page<CptPostSyncResultsModel>> getTotalCount(
	        @RequestParam int pageSize,
	        @RequestParam int pageNumber) {
	    Pageable pageable = PageRequest.of(pageNumber, pageSize);
	    Page<CptPostSyncResultsModel> totalCounts = cptCodeMaintenanceService.getAllCount(pageable);
	    HttpHeaders headers = new HttpHeaders();
	    return new ResponseEntity<>(totalCounts, headers, HttpStatus.OK);
	}
	
	@GetMapping("/version/summary/cpt")
	public ResponseEntity<List<VersionSummary>> getCptVersionSummaryData(@RequestParam int fileId,
			@RequestParam int userId) {

		HttpHeaders headers = new HttpHeaders();
		List<VersionSummary> response = cptCodeMaintenanceService.getCptVersionSummaryData(fileId, userId);
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}
}