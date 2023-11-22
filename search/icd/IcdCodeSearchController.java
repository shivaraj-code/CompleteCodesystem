package com.io.codesystem.search.icd;

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
import com.io.codesystem.search.icd.IcdCode;
import com.io.codesystem.search.icd.IcdCodeTree;
import com.io.codesystem.search.icd.IcdCodeSearchService;

@RestController
public class IcdCodeSearchController {

	@Autowired
	IcdCodeSearchService icdCodeSearchService;

	@GetMapping("/icd/search")
	public ResponseEntity<Page<IcdCode>> searchIcdCode(@RequestParam String searchTerm, Pageable pageable) {

		Page<IcdCode> searchResults = null;

		try {
			//icdCodeSearchService.createIcdIndex();
			searchResults = icdCodeSearchService.searchIcdCode(searchTerm, pageable);

		} catch (Exception ex) {

			ex.printStackTrace();
		}

		return new ResponseEntity<>(searchResults, new HttpHeaders(), HttpStatus.OK);

	}

	@PostMapping("/icd/index")
	public ResponseEntity<String> createIcdIndex() {

		String response = "ICD Index created successfully";

		try {
			icdCodeSearchService.createIcdIndex();
			
		} catch (Exception e) {
			response = "Failed to create ICD Index";

			return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.OK);
	}

//	@GetMapping(path="/icd/tree")
//    public ResponseEntity<List<IcdCodeTree>> getIcdCodeTree(@RequestParam Integer icd10id){
//		
//        List<IcdCodeTree> icdCodeTreeResponse=icdCodeSearchService.get_Icd_tree(icd10id);
//		HttpHeaders headers = new HttpHeaders();
//		return new ResponseEntity<>(icdCodeTreeResponse, headers, HttpStatus.OK);
//	}
	@GetMapping(path = "/icd/tree")
	public ResponseEntity<Page<IcdCodeTree>> getIcdCodeTree(@RequestParam Integer icd10id, Pageable pageable) {

		Page<IcdCodeTree> icdCodeTreeResponse = icdCodeSearchService.get_Icd_tree(icd10id, pageable);
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(icdCodeTreeResponse, headers, HttpStatus.OK);
	}
}
