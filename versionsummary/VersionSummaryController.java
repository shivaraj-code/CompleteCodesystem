
package com.io.codesystem.versionsummary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.io.codesystem.codemaintenancefile.CodeMaintenanceFile;

@RestController
public class VersionSummaryController {

	@Autowired
	VersionSummaryService versionSummaryService;


	@GetMapping("/versionsummary/list")
	public ResponseEntity<Page<VersionSummary>> getVersionSummaryList(@RequestParam String codeStandard,
			@RequestParam int pageNumber,
			@RequestParam int pageSize,
			@RequestParam(value = "sortBy", required = false, defaultValue = "Newest First") String sortBy,
			@RequestParam(value = "sortOrder", required = false, defaultValue = "DESC") String sortOrder) {

		Page<VersionSummary> summary = null;
		if ("All".equalsIgnoreCase(codeStandard)) {
			summary = versionSummaryService.getVersionSummaryList(pageSize, pageNumber, sortBy, sortOrder);
		} else {
			summary = versionSummaryService.getVersionSummaryListByCodeType(codeStandard, pageSize, pageNumber, sortBy,
					sortOrder);
		}

		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(summary, headers, HttpStatus.OK);

	}
}
/*	@GetMapping("/versionsummary/list")
public ResponseEntity<Page<VersionSummary>> getVersionSummaryList(
        @RequestParam String codeStandard,
        @RequestParam int pageNumber,
        @RequestParam int pageSize) {

    Page<VersionSummary> summary = null;

    if ("All".equalsIgnoreCase(codeStandard)) {
        summary = versionSummaryService.getVersionSummaryList(pageSize, pageNumber);
    } else {
        summary = versionSummaryService.getVersionSummaryListByCodeType(codeStandard, pageSize, pageNumber);
    }

    HttpHeaders headers = new HttpHeaders();
    return new ResponseEntity<>(summary, headers, HttpStatus.OK);
}
}
*/	
