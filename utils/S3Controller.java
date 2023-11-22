package com.io.codesystem.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class S3Controller {

	@Autowired
	private S3Service s3Service;

	@GetMapping("/s3bucket/files")
	public ResponseEntity<List<String>> getFileNamesFromS3Bucket(
			@RequestParam("rootFolderName") String rootFolderName) {

		List<String> filesList = s3Service.showFilesInS3BucketFolder(rootFolderName);
		HttpHeaders headers = new HttpHeaders();
		return new ResponseEntity<>(filesList, headers, HttpStatus.OK);

	}

	@DeleteMapping("/s3bucket/files/delete")
	public ResponseEntity<CustomResponse> deleteFileInS3Bucket(@RequestParam("filePath") String filePath) {

		CustomResponse customResponse = s3Service.deleteFileInS3Bucket(filePath);
		return new ResponseEntity<>(customResponse, new HttpHeaders(), customResponse.getStatusCode());

	}

	@GetMapping("/s3filedownload")
	public ResponseEntity<byte[]> downloadFile(@RequestParam String fileKey) {
		try {
			InputStream fileStream = s3Service.downloadFile(fileKey);

			byte[] fileBytes = fileStream.readAllBytes();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachment", fileKey);

			return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}