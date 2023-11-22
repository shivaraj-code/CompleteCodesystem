
package com.io.codesystem.versionsummary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.io.codesystem.codemaintenancefile.CodeMaintenanceFile;

@Service
public class VersionSummaryService {

	@Autowired
	VersionSummaryRepository versionSummaryRepository;
	

	public Page<VersionSummary> getVersionSummaryList(int pageSize, int pageNumber, String sortBy, String sortOrder) {
		// TODO Auto-generated method stub
		 Pageable pageable = PageRequest.of(pageNumber, pageSize);
		 if(sortOrder.equalsIgnoreCase("ASC")) {
		       
				//if (sortBy.equals("Oldest First")) {
				// System.out.println(sortBy + "SortBy");
				pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "id"));
			} else {
				pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "id"));
			}
		 return versionSummaryRepository.findAll(pageable);
	}

	public Page<VersionSummary> getVersionSummaryListByCodeType(String codeStandard, int pageSize, int pageNumber,
			String sortBy, String sortOrder) {
		// TODO Auto-generated method stub
		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		 if(sortOrder.equalsIgnoreCase("ASC")) {
		       
				//if (sortBy.equals("Oldest First")) {
				// System.out.println(sortBy + "SortBy");
				pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "id"));
			} else {
				pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "id"));
			}
		return versionSummaryRepository.findOrderByCodeStandard(codeStandard,pageable);
	}
}

/*	public Page<VersionSummary> getVersionSummaryList(int pageSize, int pageNumber) {
	// TODO Auto-generated method stub
	 Pageable pageable = PageRequest.of(pageNumber, pageSize);
		return versionSummaryRepository.findAll(pageable);
}

public Page<VersionSummary> getVersionSummaryListByCodeType(String codeStandard, int pageSize, int pageNumber) {
	// TODO Auto-generated method stub
	Pageable pageable = PageRequest.of(pageNumber, pageSize);
	return versionSummaryRepository.findOrderByCodeStandard(codeStandard,pageable);
}	
}
*/
