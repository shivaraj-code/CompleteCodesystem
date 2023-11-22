package com.io.codesystem.utils;

import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UtilsController {

	@Autowired
	UtilsService utilsService;

	@GetMapping("/resetcodefilesystem")
	public ResponseEntity<CustomResponse> resetCodeFileSystem(@RequestParam int fileId, @RequestParam int userId) {

		CustomResponse customResponse = utilsService.resetCodeFileSystem(fileId, userId);
		return new ResponseEntity<>(customResponse, new HttpHeaders(), HttpStatus.OK);
	}

	@GetMapping("/checkinginprocessfiles")
	public ResponseEntity<CustomResponse> checkingInprocessFiles(@RequestParam String verificationType,
			@RequestParam String codeStandard, @RequestParam Date releaseDate) {

		CustomResponse customResponse = utilsService.checkingInprocessFiles(verificationType, codeStandard,
				releaseDate);
		return new ResponseEntity<>(customResponse, new HttpHeaders(), customResponse.getStatusCode());
	}
}
