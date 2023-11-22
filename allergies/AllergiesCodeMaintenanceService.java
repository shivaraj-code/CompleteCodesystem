package com.io.codesystem.allergies;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.io.codesystem.CodeMaintenanceService;
import com.io.codesystem.asynctasks.AsyncTasksStatusService;
import com.io.codesystem.codechanges.CodeChangeCounts;
import com.io.codesystem.codemaintenancefile.CodeMaintenanceFile;
import com.io.codesystem.codemaintenancefile.CodeMaintenanceFileService;
import com.io.codesystem.codemaintenancelog.CodeMaintenanceLoggerService;
import com.io.codesystem.search.allergies.Allergieservice;
import com.io.codesystem.utils.AcceptedCodesdto;
import com.io.codesystem.utils.CodeVerification;
import com.io.codesystem.utils.CustomResponse;
import com.io.codesystem.utils.RejectedCodesdto;
import com.io.codesystem.utils.S3Service;
import com.io.codesystem.utils.TableRecordCounts;
import com.io.codesystem.utils.UtilsService;
import com.io.codesystem.utils.ValidationCheck;
import com.io.codesystem.verificationlog.CodeVerificationLogModel;
import com.io.codesystem.verificationlog.CodeVerificationLogRepository;
import com.io.codesystem.versionsummary.VersionSummary;
import com.io.codesystem.versionsummary.VersionSummaryRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AllergiesCodeMaintenanceService extends CodeMaintenanceService {

	@Autowired
	S3Service s3Service;

	@Autowired
	UtilsService utilsService;

	@Autowired
	CodeMaintenanceFileService codeMaintenanceFileService;

	@Autowired
	CodeMaintenanceLoggerService codeMaintenanceLoggerService;

	@Autowired
	AllergiesCodeStandardRepository allergiesCodeStandardRepository;

	@Autowired
	AllergiesSyncResultsRepo allergiesSyncResultsRepo;

	@Autowired
	AllergiesDataVerificationRepository allergiesDataVerificationRepository;

	@Autowired
	AllergiesPostSyncResultsRepo allergiesPostSyncResultsRepo;

	@Autowired
	AllergiesTableRecordCountsRepository allergiesTableRecordCountsRepository;

	@Autowired
	CodeVerificationLogRepository codeVerificationLogRepository;

	@Autowired
	AsyncTasksStatusService asyncTasksStatusService;

	@Autowired
	Allergieservice allergiesservice;

	@Autowired
	AllergiesVersionSummaryRepository allergiesVersionSummaryRepository;

	@Autowired
	VersionSummaryRepository versionSummaryRepository;

	@Value("${aws.s3.root-folder}")
	private String rootFolderName;

	@Value("${aws.s3.upload-folder}")
	private String uploadFolderName;

	@Value("${aws.s3.inprocess-folder}")
	private String inprocessFolderName;

	@Value("${aws.s3.processed-folder}")
	private String processedFolderName;

	@Value("${aws.s3.rejected-folder}")
	private String rejectedFolderName;

	@Override
	protected CustomResponse uploadFileToS3(String codeType, String releaseVersion, Date releaseDate,
			MultipartFile releaseFile, int userId, String effectiveFrom, String effectiveTo) {

		String zipFileName = releaseFile.getOriginalFilename();
		String targetCodeTypeFolderName = utilsService.getTargetCodeTypeFolderName(codeType);

		/*
		 * if (utilsService.prepareVerificationStatus("version-validation", codeType,
		 * releaseDate))
		 * 
		 * 
		 * return new CustomResponse("Zip File Uploading Failed",
		 * "Error:Current Uploading File Version is olderthan already existing version",
		 * HttpStatus.INTERNAL_SERVER_ERROR); String savedFilePath = "";
		 */

		if (utilsService.prepareVerificationStatus("version-validation", codeType, releaseDate))
			return new CustomResponse("Zip File Uploading Failed",
					"Error:Current Uploading File Version is olderthan already existing version",
					HttpStatus.INTERNAL_SERVER_ERROR);

		String savedFilePath = "";
		try {
			// Saving Zip File to Target Folder
			savedFilePath = s3Service.saveCodeZipFile(zipFileName, targetCodeTypeFolderName,
					releaseFile.getInputStream());
			log.info("=====SavedFilePath :: " + savedFilePath);
			log.info("==== Release Date :: " + releaseDate);
			CodeMaintenanceFile codeMaintenanceFile = utilsService.prepareCodeMaintenaceFile(codeType, zipFileName,
					savedFilePath, releaseVersion, releaseDate, userId, effectiveFrom, effectiveTo);
			codeMaintenanceFile = codeMaintenanceFileService.saveCodeMaintenanceFile(codeMaintenanceFile);
			codeMaintenanceLoggerService.saveCodeMaintenanceLog(codeMaintenanceFile.getId(), "File Uploading",
					"File Uploaded Successfully", userId);

			// ============VersionSummary
			/*
			 * CodeMaintenanceFile file =
			 * codeMaintenanceFileService.getCodeMaintenanceFileById(fileId); int
			 * fileId=codeMaintenanceFile.getId(); String
			 * name=codeMaintenanceFile.getCodeStandard(); int userId1
			 * =codeMaintenanceFile.getUserId();
			 * 
			 * VersionSummary versionSummary = new VersionSummary();
			 * 
			 * String version = versionSummary.getCurrentVersion();
			 * versionSummary.setPreviousVersion(version); versionSummary.setFileId(fileId);
			 * versionSummary.setUserId(userId1); versionSummary.setCodeStandard(name);
			 * versionSummaryRepository.save(versionSummary);
			 */

		} catch (IOException e) {
			e.printStackTrace();
			log.error("Error while Saving Zip File");
			log.error(e.getLocalizedMessage());
			return new CustomResponse("Zip File Uploading Failed", "Error:" + e.getLocalizedMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}

		return new CustomResponse("Zip File Uploaded Successfully", "", HttpStatus.OK);
	}

	@Override
	@Async
	protected void processData(int fileId, int userId) {

		processAndVerificationData(fileId, userId);
	}

	protected CustomResponse processAndVerificationData(int fileId, int userId) {
		// TODO Auto-generated method stub
		try {
			CodeMaintenanceFile zipFile = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
			log.info("===== ZipFile :: " + zipFile);
			if (zipFile != null & zipFile.getProcessedState().equalsIgnoreCase("Uploaded")) {

				/*
				 * if (utilsService.prepareVerificationStatus("version-validation",
				 * zipFile.getCodeStandard(), zipFile.getReleaseDate())) { return new
				 * CustomResponse("Zip File Processing Failed",
				 * "Error:Current Processing File Version is olderthan already existing version"
				 * , HttpStatus.INTERNAL_SERVER_ERROR); }
				 */
				ValidationCheck validationCheck = utilsService.getInprocessFileId("allergies",
						java.sql.Date.valueOf(LocalDate.now()));
				if (validationCheck.getId() != 0) {
					utilsService.resetCodeFileSystem(validationCheck.getId(), userId);
				}

				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileProcess", "File Processed Started",
						"In Process", 10, userId);

				zipFile.setProcessedState("InProcess");
				zipFile.setNextState("Verification");
				codeMaintenanceFileService.saveCodeMaintenanceFile(zipFile);
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileProcess", "Zip File Extracted",
						"In Process", 25, userId);

				InputStream zipInputStream = s3Service.getS3FileStream(zipFile.getFilePath());
				Map<String, String> targetCodeDataDetailsMap = getTargetCodeDataFilePath(zipFile.getFileName());
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileProcess",
						"Zip File Extracted And Read CSV File", "In Process", 40, userId);

				parseTargetCodeDataFromFile(zipInputStream, targetCodeDataDetailsMap, fileId, userId);
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileProcess",
						"Dump Table Created And Verification Data Prepared ", "In Process", 60, userId);

				String targetCodeStandardFolder = "allergies";
				String uploadFilePath = zipFile.getFilePath();
				log.info("===== Upload FilePath :: " + uploadFilePath);

				String dateFormatPath = utilsService.getDateInStringFormat(zipFile.getReleaseDate(), "default");
				String inProcessFilePath = rootFolderName + "/" + inprocessFolderName + "/" + targetCodeStandardFolder
						+ "/" + dateFormatPath + "/" + zipFile.getFileName();
				log.info("===== InProcess FilePath :: " + inProcessFilePath);

				s3Service.moveFile(uploadFilePath, inProcessFilePath, false);
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileProcess", "File Moved to Inprocess Folder ",
						"In Process", 80, userId);

				codeMaintenanceFileService.updateCodeMaintenanceFilePathById(fileId, inProcessFilePath);
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileProcess", "File Processed Completed",
						"Completed", 100, userId);

			}
		} catch (Exception e) {
			String errorMessage = "";
			if (e != null) {
				String localizedMessage = e.getLocalizedMessage();
				if (localizedMessage != null) {
					errorMessage = localizedMessage.substring(0, Math.min(200, localizedMessage.length()));
				}
			}

			asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileProcess", errorMessage, "Failed", 0, userId);
			CodeMaintenanceFile zipFile = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
			zipFile.setStatus("Failed");
			zipFile.setNextState("");
			zipFile.setProcessedState("");
			zipFile.setComments("Allergies File Process Failed");
			zipFile.setCurrentStatus("File Process Failed");
			codeMaintenanceFileService.saveCodeMaintenanceFile(zipFile);
		}
		return new CustomResponse("Allergies Code Maintenance File Processed Successfully", "", HttpStatus.OK);

		// return new CustomResponse("Allergies Code Maintenance File Process Failed",
		// "", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	protected CustomResponse markAsVerified(int fileId, String verifiedType, int userId) {

		CodeMaintenanceFile codeMaintenanceFile = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);

		codeMaintenanceFile.setProcessedState("Verified");
		codeMaintenanceFile.setCurrentStatus("Verified");
		codeMaintenanceFile.setModifiedUserId(userId);
		Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());
		codeMaintenanceFile.setModifiedDate(newModifiedDate);

		codeMaintenanceFileService.saveCodeMaintenanceFile(codeMaintenanceFile);
		codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "Mark as Verified",
				"Code Maintenance Data Verified", userId);

		return new CustomResponse("Code Maintenance File Verified Successfully", "", HttpStatus.OK);
	}

	@Override
	@Async
	protected void syncVerifiedData(int fileId, int userId) {

		synchAllergiesCodeMaintenanceDataWithExistingData(fileId, userId);
	}

	public Map<String, String> getTargetCodeDataFilePath(String zipFileName) {

		Map<String, String> targetCodeDataFileDetailsMap = new HashMap<>();

		String tempName = zipFileName.replace(".zip", "");
		System.out.println("tempName" + tempName);
		String targetCodeDataFilePath = tempName + ".csv";
		System.out.println("targetCodeDataFilePath" + targetCodeDataFilePath);
		targetCodeDataFileDetailsMap.put("targetCodeDataFilePath", targetCodeDataFilePath);
		targetCodeDataFileDetailsMap.put("tempTableName", tempName);

		/*
		 * String tempName = zipFileName.replace("icd10", "icd10cm_").replace(".zip",
		 * ""); // icd10cm_2022 String targetCodeDataFilePath =
		 * zipFileName.replace(".zip", "") + "/" + "dot/" + tempName + "/" + tempName +
		 * "_tab.txt";
		 * 
		 * targetCodeDataFileDetailsMap.put("targetCodeDataFilePath",
		 * targetCodeDataFilePath); targetCodeDataFileDetailsMap.put("tempTableName",
		 * tempName + "_tab");
		 */

		return targetCodeDataFileDetailsMap;
	}

	public void parseTargetCodeDataFromFile(InputStream zipInputStream, Map<String, String> targetCodeDataDetailsMap,
			int fileId, int userId) {

		InputStream targetDataFileStream = null;
		try {
			targetDataFileStream = utilsService.getTargetFileStreamFromZipFile(zipInputStream,
					targetCodeDataDetailsMap.get("targetCodeDataFilePath"));
			List<AllergiesCodeStandardModel> allegiesCodeStandardList = prepareTargetCodeEntityFromInputStream(
					targetDataFileStream);
			saveAllergiesCodeStandardList(allegiesCodeStandardList, targetCodeDataDetailsMap.get("tempTableName"),
					fileId, userId);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public List<AllergiesCodeStandardModel> prepareTargetCodeEntityFromInputStream(InputStream targetDataFileStream)
			throws IOException {

		if (targetDataFileStream != null) {

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(targetDataFileStream))) {
				CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
				List<AllergiesCodeStandardModel> allergiesCodeStandardList = new LinkedList<>();
				Iterable<CSVRecord> csvRecords = csvParser.getRecords();
				for (CSVRecord data : csvRecords) {

					AllergiesCodeStandardModel entity = new AllergiesCodeStandardModel();

					entity.setId(Integer.parseInt(data.get(0)));
					entity.setDamConceptId(data.get(1));
					entity.setDamConceptIdDesc(data.get(2));
					entity.setDamConceptIdType(Integer.parseInt(data.get(3)));
					entity.setDamAlrgnGrpDesc(data.get(4));
					entity.setSnomedCode(data.get(5));
					entity.setSnomedConcept(data.get(6));

					allergiesCodeStandardList.add(entity);
				}
				csvParser.close();
				return allergiesCodeStandardList;
			}
		} else {
			throw new IllegalArgumentException("The targetDataFileStream cannot be null.");
		}
	}

	public void saveAllergiesCodeStandardList(List<AllergiesCodeStandardModel> allergiesCodeStandardList,
			String newTableName, int fileId, int userId) {

		try {

			utilsService.truncateTable("allergies_standard_versions");
			utilsService.dropTable(newTableName);

			allergiesCodeStandardRepository.saveAll(allergiesCodeStandardList);

			utilsService.createNewTableFromExisting(newTableName, "allergies_standard_versions");
			codeMaintenanceFileService.updateCodeMaintenanceFileStatusById(fileId, "In Process", "Dump Table Created",
					userId);

			codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "Dump Table Created",
					"Dump Table Created Successfully", userId);

			allergiesCodeStandardRepository.prepareAllergiesDataForVerification(fileId, newTableName, userId);

			codeMaintenanceFileService.updateCodeMaintenanceFileStatusById(fileId, "Pending For Verification",
					"Pending For Verification", userId);
			codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "Allergies Verification Data Prepared",
					"Allergies Verification Data Prepared Successfully", userId);

		} catch (Exception e) {

			codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "Allergies Verification Data Prepared Failed",
					"Allergies Verification Data Preparation Failed", userId);
			System.out.println(e.getMessage());

		}

	}

	public CustomResponse synchAllergiesCodeMaintenanceDataWithExistingData(int fileId, int userId) {

		CodeMaintenanceFile codeMaintenanceFile = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
		log.info("===== CodeMaintenance File :: " + codeMaintenanceFile);
		asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync", "Code Sync Started", "In Process", 10,
				userId);
//		log.info("Before call the method............");
//		getAllergiesVersionSummaryData(fileId, userId);

		if (codeMaintenanceFile != null) {
			String fileName = codeMaintenanceFile.getFileName();
			log.info("=====fileName:: " + fileName);
			String tempName = fileName.replace(".zip", "");
			log.info("=====tempName:: " + tempName);
			String targetCodeStandardFolder = "allergies";
			String targetFileName = tempName;// + "/" + tempName + ".csv";
			log.info("=====targetFileName:: " + targetFileName);

			String dateFormatPath = utilsService.getDateInStringFormat(codeMaintenanceFile.getReleaseDate(), "default");
			String inProcessFilePath = rootFolderName + "/" + inprocessFolderName + "/" + targetCodeStandardFolder + "/"
					+ dateFormatPath + "/" + codeMaintenanceFile.getFileName();
			log.info("===== InProcess FilePath :: " + inProcessFilePath);

			codeMaintenanceFile.setProcessedState("Syncing InProcess");
			// codeMaintenanceFile.setNextState("");
			codeMaintenanceFileService.saveCodeMaintenanceFile(codeMaintenanceFile);
			try {
				log.info("===============Before Adding");
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync", "Code Sync Process started",
						"In Process", 30, userId);
				allergiesSyncResultsRepo.allergiesCompareAndSyncTablesForAdded(fileId, targetFileName, userId);
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync", "Added Codes Sync Process Completed",
						"In Process", 50, userId);

				log.info("==================After Adding");
				allergiesSyncResultsRepo.allergiesCompareAndSyncTablesForUpdated(fileId, targetFileName, userId);
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync",
						"Updated Codes Sync Process Completed", "In Process", 70, userId);

				log.info("===================After Updating");
				allergiesSyncResultsRepo.allergiesCompareAndSyncTablesForDeleted(fileId, targetFileName, userId);
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync",
						"Deleted Codes Sync Process Completed", "In Process", 90, userId);

				log.info("====================After Deleting");
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync", "File Sync process completed",
						"In Process", 95, userId);
				codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "Allergies Data Syncing Completed",
						" Allergies Data Synching Completed Successfully", userId);

			} catch (Exception e) {

				System.out.println(e.getMessage());
			}

			utilsService.truncateTable("allergies_standard_versions");
			codeMaintenanceFileService.updateCodeMaintenanceFileStatusById(fileId, "Synced", "syncing completed",
					userId);

			codeMaintenanceFile = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
			codeMaintenanceFile.setComments("Allergies File Proceessed Successfully");
			Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());

			codeMaintenanceFile.setNextState("");
			codeMaintenanceFile.setModifiedDate(newModifiedDate);
			codeMaintenanceFileService.saveCodeMaintenanceFile(codeMaintenanceFile);
			asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync", "Code Sync Process Completed",
					"Completed", 100, userId);

			// ================version summary table
			log.info("Call the Version Summary method After Sync...........");
			getAllergiesVersionSummaryData(fileId, userId);

			String processedFilePath = rootFolderName + "/" + processedFolderName + "/" + targetCodeStandardFolder + "/"
					+ dateFormatPath + "/" + codeMaintenanceFile.getFileName();
			log.info("======= Processed FilePath :: " + processedFilePath);
			log.info("Source Key:" + inProcessFilePath + "......... Destination Key:" + processedFilePath);
			s3Service.moveFile(inProcessFilePath, processedFilePath, false);
			codeMaintenanceFileService.updateCodeMaintenanceFilePathById(fileId, processedFilePath);

			try {
				System.out.println("Before index====");
				allergiesservice.createAllergiesIndex();

			} catch (Exception e) {

				e.printStackTrace();
			}

			return new CustomResponse("Allergies Code File Synched Successfully", "", HttpStatus.OK);
		} else {
			// Handle case where codeStandardFile is not found
			asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "File Sync", "Code Sync process Failed", "Failed", 0,
					userId);
			return new CustomResponse("Allergies Code File Synch Failed", "", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public Page<AllergiesDataVerificationModel> getAllergiesdamConceptIdDescOrdamAlrgnGrpDesc(Integer fileId,
			String searchTerm, String status, int pageSize, int pageNumber) {

		Pageable paging = PageRequest.of(pageNumber, pageSize);
		List<AllergiesDataVerificationModel> allergiesVerificationList = allergiesDataVerificationRepository
				.getAllergiesCodeVerificationDetails(fileId, searchTerm, status);
		Page<AllergiesDataVerificationModel> pagedResult = new PageImpl<>(
				allergiesVerificationList.subList(Math.min(pageNumber * pageSize, allergiesVerificationList.size()),
						Math.min((pageNumber + 1) * pageSize, allergiesVerificationList.size())),
				paging, allergiesVerificationList.size());

		return pagedResult;

	}

	public Page<PostSyncAllergiesResultsModel> getAllergiesSearchByAfterSync(Integer fileId, String searchTerm,
			String status, int pageSize, int pageNumber) {
		Pageable paging = PageRequest.of(pageNumber, pageSize);
		List<PostSyncAllergiesResultsModel> allergiesSyncList = allergiesPostSyncResultsRepo
				.getAllergiesSearchByAfterSync(fileId, searchTerm, status);
		Page<PostSyncAllergiesResultsModel> pagedResult = new PageImpl<>(
				allergiesSyncList.subList(Math.min(pageNumber * pageSize, allergiesSyncList.size()),
						Math.min((pageNumber + 1) * pageSize, allergiesSyncList.size())),
				paging, allergiesSyncList.size());
		return pagedResult;

	}

	/*
	 * public List<PostSyncAllergysResultsModel> getAllergysPostSyncResults(int
	 * fileId, String status,int pageSize,int pageNumber) {
	 * 
	 * return allergysPostSyncResultsRepo.allergysPostSyncDataResults(fileId,
	 * status);
	 * 
	 * }
	 */

	public Page<PostSyncAllergiesResultsModel> getAllergiesPostSyncResults(int fileId, String status, int pageSize,
			int pageNumber) {

		Pageable paging = PageRequest.of(pageNumber, pageSize);
		List<PostSyncAllergiesResultsModel> allergiesPostSyncList = allergiesPostSyncResultsRepo
				.allergiesPostSyncDataResults(fileId, status);
		// System.out.print(allergysPostSyncList.toString());
		Page<PostSyncAllergiesResultsModel> pagedResult = new PageImpl<>(
				allergiesPostSyncList.subList(Math.min(pageNumber * pageSize, allergiesPostSyncList.size()),
						Math.min((pageNumber + 1) * pageSize, allergiesPostSyncList.size())),
				paging, allergiesPostSyncList.size());

		// System.out.print(pagedResult.toString());

		return pagedResult;

	}

	public List<TableRecordCounts> getTableRecordCounts() {
		return allergiesTableRecordCountsRepository.getTableRecordCounts();
	}

	@Override
	protected CustomResponse saveCodeVerificationLogDetails(CodeVerification codes, String codeset, int fileId,
			int userId, String notes) {
		CodeMaintenanceFile zipFile = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
		log.info("zipFile Data:" + zipFile.toString());
		System.out.println("codeset :: " + codeset);
		System.out.println("fileId :: " + fileId);

		int id = 0;
		List<AllergiesDataVerificationModel> acceptedList = new LinkedList<>();
		log.info("===== acceptedList Data :: " + acceptedList.toString());
		List<AllergiesDataVerificationModel> rejectedList = new LinkedList<>();
		log.info("rejectedList Data" + rejectedList.toString());

		List<AcceptedCodesdto> accepted = codes.getAcceptedAllergiesCodes();
		log.info("===== accepted Data :: " + accepted.toString());
		List<RejectedCodesdto> rejected = codes.getRejectedAllergiesCodes();
		log.info("===== rejected Data :: " + rejected.toString());

		List<AllergiesDataVerificationModel> verifiedAcceptedList = acceptedVerification(accepted, acceptedList,
				fileId);
		log.info("===== verified AcceptedList Data :: " + verifiedAcceptedList.toString());
		List<AllergiesDataVerificationModel> verifiedRejectedList = rejectedVerification(rejected, rejectedList,
				fileId);
		log.info("===== verified RejectedList Data :: " + verifiedRejectedList.toString());

		// System.out.println("AFTER>>>>"+verifiedList);
		try {
			System.out.println("inside the try...........");
			System.out.println("codeset :: " + codeset);
			System.out.println("fileId :: " + fileId);
			System.out.println("verifiedAcceptedList  :" + verifiedAcceptedList);

			saveAccepted(verifiedAcceptedList, codeset, fileId, userId, notes, zipFile);
			saveRejected(verifiedRejectedList, codeset, fileId, userId, notes, zipFile);
		} catch (Exception e) {
			return new CustomResponse("Allergies Verification Log Saving Failed", e.getLocalizedMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (rejected.size() > 0) {
			zipFile.setProcessedState("Verification Rejected");
			zipFile.setStatus("Failed");
			zipFile.setNextState("");
			zipFile.setCurrentStatus("Verification Rejected");
			zipFile.setActive(0);
			String targetCodeStandardFolder = "allergies";
			String dateFormatPath = utilsService.getDateInStringFormat(zipFile.getReleaseDate(), "default");
			String rejectedFilePath = rootFolderName + "/" + rejectedFolderName + "/" + targetCodeStandardFolder + "/"
					+ dateFormatPath + "/" + zipFile.getFileName();
			log.info("==== rejectedFilePath :: " + rejectedFilePath);
			String targetFolderPath = rootFolderName + "/" + uploadFolderName + "/" + targetCodeStandardFolder + "/"
					+ zipFile.getFileName();
			log.info("===== targetFolderPath :: " + targetFolderPath);
			s3Service.moveFile(targetFolderPath, rejectedFilePath, true);
			codeMaintenanceFileService.saveCodeMaintenanceFile(zipFile);
			return new CustomResponse("Allergies Verification Rejected",
					"Due to some codes are marked as rejected while verification ", HttpStatus.INTERNAL_SERVER_ERROR);
		} else if (accepted.size() > 0) {
			zipFile.setProcessedState("Verified");
			zipFile.setCurrentStatus("Verification Complete");
			zipFile.setNextState("Sync");
			codeMaintenanceFileService.saveCodeMaintenanceFile(zipFile);
		}

		return new CustomResponse("Allergies Verification Success", "", HttpStatus.OK);
	}

	public List<AllergiesDataVerificationModel> acceptedVerification(List<AcceptedCodesdto> accepted,
			List<AllergiesDataVerificationModel> acceptedList, int fileId) {

		for (AcceptedCodesdto acceptedCodeAllergies : accepted) {

			AllergiesDataVerificationModel model = allergiesDataVerificationRepository.findByDamConceptIdDesc(
					acceptedCodeAllergies.getAcceptedCode(), acceptedCodeAllergies.getSnomedCode());

			if (model != null) {

				model.setVerificationState("Accepted");
				acceptedList.add(model);
			}

		}
		return acceptedList;
	}

	public List<AllergiesDataVerificationModel> rejectedVerification(List<RejectedCodesdto> rejected,
			List<AllergiesDataVerificationModel> rejectedList, int fileId) {

		CodeMaintenanceFile zipFile = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);

		for (RejectedCodesdto rejectedCodeAllergies : rejected) {

			AllergiesDataVerificationModel model = allergiesDataVerificationRepository.findByDamConceptIdDesc(
					rejectedCodeAllergies.getRejectedCode(), rejectedCodeAllergies.getSnomedCode());

			if (model != null) {

				zipFile.setProcessedState("Verification Rejected");
				zipFile.setStatus("Failure");
				zipFile.setNextState(" ");

				model.setVerificationState("Rejected");
				rejectedList.add(model);
			}

		}
		return rejectedList;
	}

	public void saveAccepted(List<AllergiesDataVerificationModel> verifiedAcceptedList, String codeset, int fileId,
			int userId, String notes, CodeMaintenanceFile zipFile) {

		CodeVerificationLogModel logModel = new CodeVerificationLogModel();
		if (verifiedAcceptedList.size() == 0 || verifiedAcceptedList == null) {
			System.out.println("Inside if statement=================");
			logModel.setFileId(fileId);
			logModel.setCodeset(codeset);
			// logModel.setCode("");
			logModel.setUserId(userId);
			Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());
			logModel.setInsertedDate(newModifiedDate);
			logModel.setNotes(notes);
			logModel.setVerifiedState("Accepted");
			codeVerificationLogRepository.save(logModel);

			zipFile.setProcessedState("Verified");
			zipFile.setCurrentStatus("Verification Complete");
			zipFile.setNextState("Sync");
			zipFile.setStatus("Success");
			codeMaintenanceFileService.saveCodeMaintenanceFile(zipFile);

			codeMaintenanceLoggerService.saveCodeMaintenanceLog(logModel.getFileId(), "code verification",
					" There were no new Codes to be Verify", userId);

		} else {
			for (AllergiesDataVerificationModel verifiedModel : verifiedAcceptedList) {

				allergiesDataVerificationRepository.save(verifiedModel);

				if (!verifiedModel.getDamConceptIdDesc().equalsIgnoreCase("")) {
					logModel.setCode(verifiedModel.getDamConceptIdDesc());
					log.info("IN IFFFF ACCC>>>>>>>>>>>" + verifiedModel.getDamConceptIdDesc() + ">>>>>>");
					log.info(verifiedModel.toString());
				} else {
					logModel.setCode("id:" + verifiedModel.getDamConceptIdDesc().toString());
				}
				logModel.setFileId(fileId);
				logModel.setCodeset(codeset);
				logModel.setUserId(userId);
				Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());
				logModel.setInsertedDate(newModifiedDate);
				logModel.setNotes(notes);
				logModel.setVerifiedState("Accepted");
				codeVerificationLogRepository.save(logModel);
				codeMaintenanceLoggerService.saveCodeMaintenanceLog(verifiedModel.getFileId(), "code verification",
						"Following Allergies Code verified:" + logModel.getCode(), userId);
			}
		}
	}

	public void saveRejected(List<AllergiesDataVerificationModel> verifiedRejectedList, String codeset, int fileId,
			int userId, String notes, CodeMaintenanceFile zipFile) {
		CodeVerificationLogModel logModel = new CodeVerificationLogModel();

		/*
		 * if (verifiedRejectedList.size()==0 || verifiedRejectedList == null) {
		 * logModel.setFileId(fileId); logModel.setCodeset(codeset);
		 * logModel.setCode(""); logModel.setUserId(userId); Timestamp newModifiedDate =
		 * Timestamp.valueOf(LocalDateTime.now());
		 * logModel.setInsertedDate(newModifiedDate); logModel.setNotes(notes);
		 * logModel.setVerifiedState("Rejected");
		 * codeVerificationLogRepository.save(logModel);
		 * 
		 * } else {
		 */
		for (AllergiesDataVerificationModel verifiedModel : verifiedRejectedList) {

			allergiesDataVerificationRepository.save(verifiedModel);

			if (!verifiedModel.getDamConceptIdDesc().equalsIgnoreCase("")) {

				logModel.setCode(verifiedModel.getDamConceptIdDesc());
				System.out.println("in rejecttt ifff>>>>>>>>>>>" + verifiedModel.getDamConceptIdDesc());

			} else {

				logModel.setCode(verifiedModel.getDamConceptIdDesc().toString());
			}
			logModel.setUserId(userId);
			logModel.setFileId(fileId);
			logModel.setCodeset(codeset);
			Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());
			logModel.setInsertedDate(newModifiedDate);
			logModel.setNotes(notes);
			logModel.setVerifiedState("Rejected");
			codeVerificationLogRepository.save(logModel);
			codeMaintenanceLoggerService.saveCodeMaintenanceLog(verifiedModel.getFileId(), "code verification",
					"Following Allergies Code rejected:" + logModel.getCode(), userId);

		}
	}

	public List<PostSyncAllergiesResultsModel> getAllergiesVerificationSearch(String damConceptId,
			Integer damConceptIdType, String snomedCode, String snomedConcept, String status) {
		return allergiesPostSyncResultsRepo.getAllergiesVerificationSearch(damConceptId, damConceptIdType, snomedCode,
				snomedConcept, status);

	}

	public Page<PostSyncAllergiesResultsModel> getAllCount(Pageable pageable) {
		// TODO Auto-generated method stub
		return allergiesPostSyncResultsRepo.findValidatedRecords(pageable);
	}

	public List<VersionSummary> getAllergiesVersionSummaryData(int fileId, int userId) {

		VersionSummary versionSummary = new VersionSummary();

		CodeMaintenanceFile file = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
		int totalValidRecords = getAllergiesTotalRecords(fileId);

		// VersionSummary lastVersionSummary =
		// versionSummaryRepository.findTopByOrderByCurrentVersionProcessedDateDesc(fileId,file.getCodeStandard());
		VersionSummary codestandard = versionSummaryRepository.findOrderByCodeStandard(file.getCodeStandard());
		System.out.println("CodeStandard=======" + file.getCodeStandard());
		System.out.println("FileId=======" + fileId);
		System.out.println("Reference CodeStandard ----------" + codestandard);

		// Update previous_version, previous_version_total_records, and
		// previous_version_processed_date
		if (codestandard != null) {
			System.out.println("inside if statement............");
			versionSummary.setPreviousVersion(codestandard.getCurrentVersion());
			versionSummary.setPreviousVersionTotalRecords(codestandard.getCurrentVersionTotalRecords());
			versionSummary.setPreviousVersionProcessedDate(codestandard.getCurrentVersionProcessedDate());
			System.out.println("After if statement............");
		}
		// CodeChangeCounts count = new CodeChangeCounts();
		String status = "Post Sync";
		CodeChangeCounts CCcounts = versionSummaryRepository.findByfileIdAndStatus(fileId, status);
		System.out.println("ChangeCounts fileId-------" + fileId);
		System.out.println("Changecounts CCcounts------" + CCcounts);

		versionSummary.setAddedRecords(CCcounts.getAddedRecords());
		versionSummary.setUpdatedRecords(CCcounts.getUpdatedRecords());
		versionSummary.setDeletedRecords(CCcounts.getDeletedRecords());

		// Set current version data
		versionSummary.setCurrentVersion(file.getReleaseVersion());
		versionSummary.setFileId(file.getId());
		versionSummary.setUserId(file.getUserId());
		versionSummary.setCodeStandard(file.getCodeStandard());
		// versionSummary.setCurrentVersionProcessedDate(Timestamp.valueOf(LocalDateTime.now()));

//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
//		//String formattedDateTime = LocalDateTime.now().withNano(0).format(formatter);
//		versionSummary.setCurrentVersionProcessedDate(Timestamp.valueOf(LocalDateTime.now().minusNanos(formatter)));

		versionSummary.setCurrentVersionProcessedDate(Timestamp.valueOf(LocalDateTime.now().minusNanos(0)));
		versionSummary.setReleaseDate(file.getReleaseDate());
		versionSummary.setCurrentVersionTotalRecords(totalValidRecords);

		// Save the version summary
		versionSummaryRepository.save(versionSummary);

		return null;
	}

	public int getAllergiesTotalRecords(int fileId) {

		return allergiesVersionSummaryRepository.currentVersionAllergiesTotalRecords(fileId);
	}

}
