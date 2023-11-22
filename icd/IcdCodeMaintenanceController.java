package com.io.codesystem.icd;

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

import com.io.codesystem.cpt.CptPostSyncResultsModel;
import com.io.codesystem.medicine.MedicinePostSyncResultsModel;
import com.io.codesystem.utils.TableRecordCounts;
import com.io.codesystem.versionsummary.VersionSummary;

@RestController
public class IcdCodeMaintenanceController {

	@Autowired
	private IcdCodeMaintenanceService icdCodeMaintenanceService;

	@GetMapping(path = "/verification/icd")
	public ResponseEntity<Page<IcdDataVerificationModel>> getIcdCodeOrDescOrstatus(@RequestParam Integer fileId,
			@RequestParam String searchTerm, 
			@RequestParam String status,
			@RequestParam int pageSize,
			@RequestParam int pageNumber) {
		HttpHeaders headers = new HttpHeaders();
		Page<IcdDataVerificationModel> response = icdCodeMaintenanceService.getIcdCodeOrDescOrstatus(fileId, searchTerm,
				status, pageSize, pageNumber);
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}

	@GetMapping("/postsyncresults/icd")
	public ResponseEntity<Page<IcdPostSyncResultsModel>> getIcdPostSyncresults(@RequestParam int fileId,

			@RequestParam String status, @RequestParam int pageSize, @RequestParam int pageNumber) {
		Page<IcdPostSyncResultsModel> response = icdCodeMaintenanceService.getIcdPostSyncresults(fileId, status,
				pageSize, pageNumber);
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}

	@GetMapping("/aftersync/search/icd")
	public ResponseEntity<Page<IcdPostSyncResultsModel>> getIcdSearchByAfterSync(@RequestParam Integer fileId,
			@RequestParam String searchTerm, @RequestParam String status, @RequestParam int pageSize,
			@RequestParam int pageNumber) {

		HttpHeaders headers = new HttpHeaders();
		Page<IcdPostSyncResultsModel> Response = icdCodeMaintenanceService.getIcdSearchByAfterSync(fileId, searchTerm,
				status, pageSize, pageNumber);
		return new ResponseEntity<>(Response, headers, HttpStatus.OK);
	}
	
/*	@GetMapping("/verfication/icdSearch")
	public ResponseEntity<Page<IcdPostSyncResultsModel>> getIcdVerificationSearch(
			@RequestParam String searchTerm, @RequestParam int pageSize, @RequestParam int pageNumber) {
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		HttpHeaders headers = new HttpHeaders();
		Page<IcdPostSyncResultsModel> medResponse = icdCodeMaintenanceService
				.getIcdVerificationSearch(searchTerm, pageable);
		return new ResponseEntity<>(medResponse, headers, HttpStatus.OK);

	}
*/
	@GetMapping("/verfication/icdSearch")
	public ResponseEntity<Page<IcdPostSyncResultsModel>> getIcdVerificationSearch(
	        @RequestParam String searchTerm,
	        @RequestParam int pageSize,
	        @RequestParam int pageNumber) {
	    Pageable pageable = PageRequest.of(pageNumber, pageSize);
	    HttpHeaders headers = new HttpHeaders();
	    Page<IcdPostSyncResultsModel> medResponse = icdCodeMaintenanceService
	            .getIcdVerificationSearch(searchTerm, pageable);
	    return new ResponseEntity<>(medResponse, headers, HttpStatus.OK);
	}

	@GetMapping("/tableRecordCounts/icd")
	public ResponseEntity<List<TableRecordCounts>> getTableRecordCounts() {
		List<TableRecordCounts> tableRecordCounts = icdCodeMaintenanceService.getTableRecordCounts();
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(tableRecordCounts, headers, HttpStatus.OK);

	}
	
	@GetMapping("/totalrecords/icd")
	public ResponseEntity<Page<IcdPostSyncResultsModel>> getTotalCount(
	        @RequestParam int pageSize,
	        @RequestParam int pageNumber) {
	    Pageable pageable = PageRequest.of(pageNumber, pageSize);
	    Page<IcdPostSyncResultsModel> totalCounts = icdCodeMaintenanceService.getAllCount(pageable);
	    HttpHeaders headers = new HttpHeaders();
	    return new ResponseEntity<>(totalCounts, headers, HttpStatus.OK);
	}
	@GetMapping("/version/summary/icd")
	public ResponseEntity<List<VersionSummary>> getIcdVersionSummaryData(@RequestParam int fileId,
			@RequestParam int userId) {

		HttpHeaders headers = new HttpHeaders();
		List<VersionSummary> response = icdCodeMaintenanceService.getIcdVersionSummaryData(fileId, userId);
		return new ResponseEntity<>(response, headers, HttpStatus.OK);

	}
}
