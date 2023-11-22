package com.io.codesystem.codemaintenancefile;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.io.codesystem.codemaintenancelog.CodeMaintenanceLoggerService;
import com.io.codesystem.medicine.MedicinePostSyncResultsModel;
import com.io.codesystem.utils.CustomResponse;
import com.io.codesystem.utils.S3Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CodeMaintenanceFileService {

	@Autowired
	CodeMaintenanceFileRepository codeMaintenanceFileRepository;

	@Autowired
	CodeMaintenanceLoggerService codeMaintenanceLoggerService;

	@Autowired
	S3Service s3Service;

	public CodeMaintenanceFile saveCodeMaintenanceFile(CodeMaintenanceFile codeMaintenanceFile) {

		return codeMaintenanceFileRepository.save(codeMaintenanceFile);
	}

	public CodeMaintenanceFile getCodeMaintenanceFileById(int fileId) {

		Optional<CodeMaintenanceFile> codeMaintenanceFile = codeMaintenanceFileRepository.findById(fileId);

		if (codeMaintenanceFile.isPresent()) {

			return codeMaintenanceFile.get();
		}

		else
			return null;
	}

	public CustomResponse deleteCodeMaintenanceFile(int fileId, int userId) {

		try {

			Optional<CodeMaintenanceFile> codeMaintenanceFile = codeMaintenanceFileRepository.findById(fileId);
			String filePath = codeMaintenanceFile.get().getFilePath();
			if (codeMaintenanceFile.isPresent()) {
				// codeMaintenanceFileRepository.deleteById(fileId);
				System.out.println("----Original File Path ::" + filePath);

				codeMaintenanceFile.get().setStatus("Deleted");
				codeMaintenanceFile.get().setNextState("");
				codeMaintenanceFile.get().setProcessedState("");
				codeMaintenanceFile.get().setComments("File Deleted Successfully");
				codeMaintenanceFile.get().setActive(0);
				codeMaintenanceFile.get().setCurrentStatus("File Deleted");

				codeMaintenanceFileRepository.save(codeMaintenanceFile.get());
				if (filePath.contains("/inprocess/")) {
					String updatedFilePath = filePath.replace("/inprocess/", "/upload/");
					String updatedFilePath1 = removeDateFromFilePath(updatedFilePath);

					System.out.println("Updated File Path: " + updatedFilePath1); // Add this line for debugging
					s3Service.deleteFileInS3Bucket(updatedFilePath1);
				}
				s3Service.deleteFileInS3Bucket(filePath);
				codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "File Deletion",
						"File Deleted Successfully", userId);
				return new CustomResponse("FIle Deleted Successfully", "", HttpStatus.OK);
			}
		} catch (Exception e) {
			codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "File Deletion", "File Deletion Failed",
					userId);
			return new CustomResponse("File Deletion Failed", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return null;
	}

	private String removeDateFromFilePath(String filePath) {
		// This method removes the date part (e.g., "20230310") from the filePath
		String regex = "/\\d{8}/"; // Matches 8 digits between slashes
		return filePath.replaceFirst(regex, "/");
	}

	public void updateCodeMaintenanceFileStatusById(int fileId, String processedState, String currentStatus,
			int userId) {

		Optional<CodeMaintenanceFile> codeMaintenanceFile = codeMaintenanceFileRepository.findById(fileId);
		log.info("======CurrentStatus:: " + currentStatus);
		log.info("======codeMaintenanceFile:: " + codeMaintenanceFile);
		if (codeMaintenanceFile.isPresent()) {
			CodeMaintenanceFile file = codeMaintenanceFile.get();
			file.setProcessedState(processedState);
			file.setCurrentStatus(currentStatus);
			Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());
			// System.out.println(newModifiedDate.toString());
			file.setModifiedDate(newModifiedDate);
			file.setModifiedUserId(userId);
			codeMaintenanceFileRepository.save(file);
		}
	}

	public void updateCodeMaintenanceFilePathById(int fileId, String filePath) {
		Optional<CodeMaintenanceFile> codeMaintenanceFile = codeMaintenanceFileRepository.findById(fileId);
		log.info("======codeMaintenanceFile Path:: " + filePath);
		if (codeMaintenanceFile.isPresent()) {
			CodeMaintenanceFile file = codeMaintenanceFile.get();
			file.setFilePath(filePath);
			Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());
			// System.out.println(newModifiedDate.toString());
			file.setModifiedDate(newModifiedDate);

			codeMaintenanceFileRepository.save(file);
		}
	}

	public Page<CodeMaintenanceFile> getCodeStandardFileList(String processedState, String codeStandard, int pageSize,
			int pageNumber, String sortBy, String sortOrder) {

		Pageable paging = PageRequest.of(pageNumber, pageSize);
		// TODO Auto-generated method stub

		 if(sortOrder.equalsIgnoreCase("DESC")) {
             
	            paging = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "id"));
	        } else {
	            paging = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "id"));
	        }
		List<CodeMaintenanceFile> fileList = codeMaintenanceFileRepository
				.findByCodeStandardAndProcessedState(processedState, codeStandard);
		
		Page<CodeMaintenanceFile> pagedResult = new PageImpl<>(
				fileList.subList(Math.min(pageNumber * pageSize, fileList.size()),
						Math.min((pageNumber + 1) * pageSize, fileList.size())),
				paging, fileList.size());

		return pagedResult;

	}

}
/*
 * public CodeMaintenanceFile getByProcessedState(int fileId) { // TODO
 * Auto-generated method stub return
 * codeMaintenanceFileRepository.findByProcessedState(fileId); }
 */

/*
 * public CustomResponse deleteCodeMaintenanceFile(int fileId, int userId) { try
 * { Optional<CodeMaintenanceFile> codeMaintenanceFile =
 * codeMaintenanceFileRepository.findById(fileId);
 * 
 * if (codeMaintenanceFile.isPresent()) { CodeMaintenanceFile file =
 * codeMaintenanceFile.get(); List<String> filePathsToDelete = new
 * ArrayList<>();
 * 
 * if ("uploaded".equals(file.getProcessedState())){ // Add the "uploaded" file
 * path to the list filePathsToDelete.add(file.getFilePath()); } else if
 * ("inprocess".equals(file.getProcessedState())) { // Add both "uploaded" and
 * "inprocess" file paths to the list filePathsToDelete.add(file.getFilePath());
 * filePathsToDelete.add(file.getFilePath().replace("/inprocess/", "/upload/"));
 * }
 * 
 * // Delete the files from the S3 bucket for (String filePath :
 * filePathsToDelete) { s3Service.deleteFileInS3Bucket(filePath); }
 * 
 * // Update file status and next state file.setStatus("Deleted");
 * file.setNextState(""); file.setProcessedState("");
 * 
 * codeMaintenanceFileRepository.save(file); // Update the file in the database
 * 
 * // Log the successful file deletion
 * codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "File Deletion",
 * "File Deleted Successfully", userId);
 * 
 * return new CustomResponse("File(s) Deleted Successfully", "", HttpStatus.OK);
 * } } catch (Exception e) { // Log the failure to delete the file
 * codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "File Deletion",
 * "File Deletion Failed", userId); return new
 * CustomResponse("File Deletion Failed", e.getMessage(),
 * HttpStatus.INTERNAL_SERVER_ERROR); }
 * 
 * // If the code reaches here, it means the file was not found or an exception
 * occurred. return new CustomResponse("File not found", "",
 * HttpStatus.NOT_FOUND); }
 */

/*
 * public Page<CodeMaintenanceFile> getCodeStandardFileDetails(Pageable
 * pageable) {
 * 
 * return codeMaintenanceFileRepository.findAll(pageable); }
 * 
 * public Page<CodeMaintenanceFile> getByProcessedState(String processedState,
 * Pageable pageable) {
 * 
 * return codeMaintenanceFileRepository.findByProcessedState(processedState,
 * pageable); }
 */