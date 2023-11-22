package com.io.codesystem.pharmacy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import com.io.codesystem.search.pharmacy.PharmacySearchService;
import com.io.codesystem.utils.CodeVerification;
import com.io.codesystem.utils.CustomResponse;
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
public class PharmacyCodeMaintenanceService extends CodeMaintenanceService {

	@Autowired
	S3Service s3Service;

	@Autowired
	UtilsService utilsService;

	@Autowired
	CodeMaintenanceFileService codeMaintenanceFileService;

	@Autowired
	CodeMaintenanceLoggerService codeMaintenanceLoggerService;

	@Autowired
	PharmacyCodeStandardRepository pharmacyCodeStandardRepository;

	@Autowired
	PharmacyDataVerificationRepository pharmacyDataVerificationRepository;

	@Autowired
	PharmacySyncResultsRepository pharmacySyncResultsRepository;

	@Autowired
	PharmacyPostSyncResultsRepository pharmacyPostSyncResultsRepository;

	@Autowired
	CodeVerificationLogRepository codeVerificationLogRepository;

	@Autowired
	AsyncTasksStatusService asyncTasksStatusService;

	@Autowired
	PharmacyTableRecordCountsRepository pharmacyTableRecordCountsRepository;

	@Autowired
	PharmacySearchService pharmacySearchService;

	@Autowired
	PharmacyVersionSummaryRepository pharmacyVersionSummaryRepository;

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

		processAndPrepareVerificationData(fileId, userId);
	}

	protected CustomResponse processAndPrepareVerificationData(int fileId, int userId) {
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
				ValidationCheck validationCheck = utilsService.getInprocessFileId("pharmacy",
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

				/*
				 * try { InputStream zipInputStream =
				 * s3Service.getS3FileStream(zipFile.getFilePath()); Map<String, String>
				 * targetCodeDataDetailsMap = getTargetCodeDataFilePath(zipFile.getFileName());
				 * asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "File Processed",
				 * "Zip File Extracted And Read CSV File", "In Process", 40, userId);
				 * 
				 * parseTargetCodeDataFromFile(zipInputStream, targetCodeDataDetailsMap, fileId,
				 * userId); } catch (Exception e) { // Create a CustomResponse with a custom
				 * message and the exception's message
				 * System.out.println("==entering the catch"); CustomResponse customResponse =
				 * new CustomResponse("Zip file can't read, please check it once",
				 * e.getMessage(), HttpStatus.OK); return customResponse; }
				 */
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileProcess",
						"Dump Table Created And Verification Data Prepared ", "In Process", 60, userId);

				String targetCodeStandardFolder = "pharmacy";
				String uploadFilePath = zipFile.getFilePath();
				log.info("====== Upload FilePath :: " + uploadFilePath);

				String dateFormatPath = utilsService.getDateInStringFormat(zipFile.getReleaseDate(), "default");
				String inProcessFilePath = rootFolderName + "/" + inprocessFolderName + "/" + targetCodeStandardFolder
						+ "/" + dateFormatPath + "/" + zipFile.getFileName();
				log.info("=========== inProcessFilePath ::" + inProcessFilePath);

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
			zipFile.setComments("Pharmacies File Process Failed");
			zipFile.setCurrentStatus("File Process Failed");
			codeMaintenanceFileService.saveCodeMaintenanceFile(zipFile);
		}
		return new CustomResponse("Pharmacy Code Maintenance File Processed Successfully", "", HttpStatus.OK);
	}

	// return new CustomResponse("Pharmacy Code Maintenance File Process Failed",
	// "", HttpStatus.INTERNAL_SERVER_ERROR);

	@Override
	protected CustomResponse markAsVerified(int fileId, String verifiedType, int userId) {
		if (verifiedType.equalsIgnoreCase("All")) {
			CodeMaintenanceFile codeMaintenanceFile = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
			codeMaintenanceFile.setProcessedState("Verified");
			codeMaintenanceFile.setCurrentStatus("Verified");
			codeMaintenanceFile.setModifiedUserId(userId);
			Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());
			codeMaintenanceFile.setModifiedDate(newModifiedDate);
			codeMaintenanceFileService.saveCodeMaintenanceFile(codeMaintenanceFile);
		}

		String verificationMessage = verifiedType + " Dataset";
		codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, verificationMessage + " marked as Verified",
				"Code Maintenance Data (" + verifiedType + ") Verified", userId);

		return new CustomResponse(verificationMessage + " marked as Verified", "", HttpStatus.OK);
	}

	@Override
	@Async
	protected void syncVerifiedData(int fileId, int userId) {
		synchPharmacyCodeMaintenanceDataWithExistingData(fileId, userId);
	}

	public Map<String, String> getTargetCodeDataFilePath(String zipFileName) {

		Map<String, String> targetCodeDataFileDetailsMap = new HashMap<>();

		String tempName = zipFileName.replace(".zip", "");
		log.info("==== Temp Name:: " + tempName);
		String targetCodeDataFilePath = tempName + ".csv";
		log.info("===== TargetCodeData FilePath:: " + targetCodeDataFilePath);
		targetCodeDataFileDetailsMap.put("targetCodeDataFilePath", targetCodeDataFilePath);
		targetCodeDataFileDetailsMap.put("tempTableName", tempName);

		return targetCodeDataFileDetailsMap;
	}

	public void parseTargetCodeDataFromFile(InputStream zipInputStream, Map<String, String> targetCodeDataDetailsMap,
			int fileId, int userId) {

		InputStream targetDataFileStream = null;
		try {
			targetDataFileStream = utilsService.getTargetFileStreamFromZipFile(zipInputStream,
					targetCodeDataDetailsMap.get("targetCodeDataFilePath"));
			log.info("====== TargetDataFileStream :: " + targetDataFileStream);
			List<PharmacyCodeStandardModel> pharmacyCodeStandardList = prepareTargetCodeEntityFromInputStream(
					targetDataFileStream);
			savePharmacyCodeStandardList(pharmacyCodeStandardList, targetCodeDataDetailsMap.get("tempTableName"),
					fileId, userId);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private List<PharmacyCodeStandardModel> prepareTargetCodeEntityFromInputStream(InputStream targetDataFileStream)
			throws IOException {
		List<PharmacyCodeStandardModel> pharmacyList = new LinkedList<>();

		if (targetDataFileStream != null) {

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(targetDataFileStream))) {

				CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

				Iterable<CSVRecord> csvRecords = csvParser.getRecords();
				for (CSVRecord data : csvRecords) {
					PharmacyCodeStandardModel entity = new PharmacyCodeStandardModel();

					entity.setId(Integer.parseInt(data.get(0)));
					entity.setNcpdpid(data.get(1));
					entity.setStoreNumber((data.get(2)));
					entity.setReferenceNumberAlt1(data.get(3));
					entity.setReferenceNumberAlt1Qualifier(data.get(4));
					entity.setStoreName(data.get(5));
					entity.setAddressLine1(data.get(6));
					entity.setAddressLine2(data.get(7));
					entity.setCity(data.get(8));
					entity.setState(data.get(9));
					entity.setZip(data.get(10));
					entity.setPhonePrimary(data.get(11));
					entity.setFax(data.get(12));
					entity.setEmail(data.get(13));
					entity.setPhoneAlt1(data.get(14));
					entity.setPhoneAlt1Qualifier(data.get(15));
					entity.setPhoneAlt2(data.get(16));
					entity.setPhoneAlt2Qualifier(data.get(17));
					entity.setPhoneAlt3(data.get(18));
					entity.setPhoneAlt3Qualifier(data.get(19));
					entity.setPhoneAlt4(data.get(20));
					entity.setPhoneAlt4Qualifier(data.get(21));
					entity.setPhoneAlt5(data.get(22));
					entity.setPhoneAlt5Qualifier(data.get(23));
					entity.setActiveStartTime(data.get(24));
					entity.setActiveEndTime(data.get(25));
					entity.setServiceLevel(data.get(26));
					entity.setPartnerAccount(data.get(27));
					entity.setLastModifiedDate(data.get(28));
					entity.setTwentyFourHourFlag(data.get(29));
					entity.setCrossStreet(data.get(30));
					entity.setRecordChange(data.get(31));
					entity.setOldServiceLevel(data.get(32));
					entity.setTextServiceLevel(data.get(33));
					entity.setTextServiceLevelChange(data.get(34));
					entity.setVersion(data.get(35));
					entity.setNpi(data.get(36));
					entity.setIsDeleted(data.get(37));
					entity.setSpecialtyType1(data.get(38));
					entity.setSpecialtyType2(data.get(39));
					entity.setSpecialtyType3(data.get(40));
					entity.setSpecialtyType4(data.get(41));
					entity.setType(data.get(42));
					entity.setLongitude(data.get(43));
					entity.setLatitude(data.get(44));
					entity.setLocation(data.get(45));

					pharmacyList.add(entity);
				}
				csvParser.close();
				return pharmacyList;
			}
		} else {
			throw new IllegalArgumentException("The targetDataFileStream cannot be null.");
		}
	}

	public void savePharmacyCodeStandardList(List<PharmacyCodeStandardModel> pharmacyCodeStandardList,
			String newTableName, int fileId, int userId) {

		try {

			utilsService.truncateTable("pharmacy_standard_versions");

			CodeMaintenanceFile codeMaintenanceFile = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
			String releaseDate = utilsService.getDateInStringFormat(codeMaintenanceFile.getReleaseDate(), "default");
			newTableName = newTableName + "_" + releaseDate;
			utilsService.dropTable(newTableName);

			pharmacyCodeStandardRepository.saveAll(pharmacyCodeStandardList);

			utilsService.createNewTableFromExisting(newTableName, "pharmacy_standard_versions");
			codeMaintenanceFileService.updateCodeMaintenanceFileStatusById(fileId, "In Process", "Dump Table Created",
					userId);
			codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "Dump Table Created",
					"Dump Table Created Successfully", userId);
			pharmacyCodeStandardRepository.preparePharmacyDataForVerification(fileId, newTableName, userId);
			codeMaintenanceFileService.updateCodeMaintenanceFileStatusById(fileId, "Pending For Verification",
					"Pending For Verification", userId);
			codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "Pharmacy Verification Data Prepared",
					"Pharmacy Verification Data Prepared Successfully", userId);

		} catch (Exception e) {

			codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "Pharmacy Verification Data Prepared Failed",
					"Pharmacy Verification Data Preparation Failed", userId);
			System.out.println(e.getMessage());

		}

	}

	public CustomResponse synchPharmacyCodeMaintenanceDataWithExistingData(int fileId, int userId) {

		CodeMaintenanceFile codeMaintenanceFile = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
		log.info("===== CodeMaintenance File :: " + codeMaintenanceFile);
		asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync", "Code Sync Started", "In Process", 10,
				userId);

		if (codeMaintenanceFile != null) {
			String fileName = codeMaintenanceFile.getFileName();
			log.info("=====fileName:: " + fileName);
			String tempName = fileName.replace(".zip", "");
			log.info("=====tempName:: " + tempName);
			String targetCodeStandardFolder = "pharmacy";
			String targetFileName = tempName;
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
				pharmacySyncResultsRepository.pharmacyCompareAndSyncTablesForAdded(fileId, targetFileName, userId);
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync", "Added Codes Sync Process Completed",
						"In Process", 50, userId);
//				codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "Pharmacies Added Records Syncing Completed",
//						"Pharmacies Added Records Syncing Completed Successfully", userId);

				log.info("==================After Adding");
				pharmacySyncResultsRepository.pharmacyCompareAndSyncTablesForUpdated(fileId, targetFileName, userId);
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync",
						"Updated Codes Sync Process Completed", "In Process", 70, userId);
//				codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId,"Pharmacies Updated Records Syncing Completed",
//						"Pharmacies Updated Records Syncing Completed Successfully", userId);

				log.info("===================After Updating");
				pharmacySyncResultsRepository.pharmacyCompareAndSyncTablesForDeleted(fileId, targetFileName, userId);
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync",
						"Deleted Codes Sync Process Completed", "In Process", 90, userId);
//				codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId,	"Pharmacies Deleted Records Syncing Completed",
//						"Pharmacies Deleted Records Synching Completed Successfully", userId);

				log.info("====================After Deleting");
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync", "File Sync Process Completed",
						"In Process", 95, userId);
				codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "Pharmacy Data Syncing Completed",
						"Pharmacy Data Syncing Completed Successfully", userId);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

			utilsService.truncateTable("pharmacy_standard_versions");
			codeMaintenanceFileService.updateCodeMaintenanceFileStatusById(fileId, "Synced", "Syncing Completed",
					userId);
			codeMaintenanceFile = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
			codeMaintenanceFile.setComments("Pharmacy File Proceessed Successfully");
			Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());

			codeMaintenanceFile.setNextState("");
			codeMaintenanceFile.setModifiedDate(newModifiedDate);
			codeMaintenanceFileService.saveCodeMaintenanceFile(codeMaintenanceFile);
			asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync", "Code Sync Process Completed",
					"Completed", 100, userId);

			// ================version summary table
			log.info("Call the Version Summary method After Sync...........");
			getPharmacyVersionSummaryData(fileId, userId);

			String processedFilePath = rootFolderName + "/" + processedFolderName + "/" + targetCodeStandardFolder + "/"
					+ dateFormatPath + "/" + codeMaintenanceFile.getFileName();
			log.info("======= Processed FilePath :: " + processedFilePath);
			log.info("Source Key:" + inProcessFilePath + "......... Destination Key:" + processedFilePath);
			s3Service.moveFile(inProcessFilePath, processedFilePath, true);
			codeMaintenanceFileService.updateCodeMaintenanceFilePathById(fileId, processedFilePath);

			try {
				System.out.println("Before index====");
				pharmacySearchService.createPharmacyIndex();

			} catch (Exception e) {
				e.printStackTrace();
			}
			return new CustomResponse("Pharmacy Code File Synched Successfully", "", HttpStatus.OK);
		} else {
			// Handle case where codeStandardFile is not found
			asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "File Sync", "Code Sync process Failed", "Failed", 0,
					userId);
			return new CustomResponse("Pharmacy Code File Synch Failed", "", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public Page<PharmacyDataVerificationModel> searchPharmacyFromVerification(Integer fileId, String searchTerm,
			String filter, String status, int pageSize, int pageNo) {
		Pageable paging = PageRequest.of(pageNo, pageSize);

		if (status.equalsIgnoreCase("All")) {
			status = "";
		}
		List<PharmacyDataVerificationModel> pharmacyVerificationList = new LinkedList<>();

		if (searchTerm.isBlank()) {
			pharmacyVerificationList = pharmacyDataVerificationRepository.getPharmacyVerificationDetails(fileId,
					status);
		} else if (searchTerm.isBlank() != true) {
			if (filter.equalsIgnoreCase("ncpdpid")) {
				pharmacyVerificationList = pharmacyDataVerificationRepository
						.getPharmacyVerificationDetailsNcpdpId(fileId, searchTerm, status);
			} else if (filter.equalsIgnoreCase("name")) {
				pharmacyVerificationList = pharmacyDataVerificationRepository.getPharmacyVerificationDetailsName(fileId,
						searchTerm, status);
			} else if (filter.equalsIgnoreCase("zip")) {
				pharmacyVerificationList = pharmacyDataVerificationRepository.getPharmacyVerificationDetailsZip(fileId,
						searchTerm, status);
			} else if (filter.equalsIgnoreCase("address")) {
				pharmacyVerificationList = pharmacyDataVerificationRepository
						.getPharmacyVerificationDetailsAddress(fileId, searchTerm, status);
			}
		}

		long totalHitCount = pharmacyVerificationList.size();
		int fromIndex = pageNo * pageSize;
		int toIndex = Math.min(fromIndex + pageSize, pharmacyVerificationList.size());
		List<PharmacyDataVerificationModel> hits = pharmacyVerificationList.subList(fromIndex, toIndex);
		return new PageImpl<>(hits, paging, totalHitCount);
	}

	public Page<PharmacyPostSyncResultsModel> searchPharmacyPostSync(Integer fileId, String searchTerm, String filter,
			String status, int pageSize, int pageNo) {
		Pageable paging = PageRequest.of(pageNo, pageSize);
		if (status.equalsIgnoreCase("All")) {
			status = "";
		}

		List<PharmacyPostSyncResultsModel> pharmacyVerificationList = new LinkedList<>();

		if (searchTerm.isBlank()) {
			pharmacyVerificationList = pharmacyPostSyncResultsRepository.pharmacyPostSyncDataResults(fileId, status);
		} else if (searchTerm.isBlank() != true) {
			if (filter.equalsIgnoreCase("ncpdpid")) {
				pharmacyVerificationList = pharmacyPostSyncResultsRepository.getPharmacyDataPostSyncNcpdpId(fileId,
						searchTerm, status);
			} else if (filter.equalsIgnoreCase("name")) {
				pharmacyVerificationList = pharmacyPostSyncResultsRepository.getPharmacyDataPostSyncName(fileId,
						searchTerm, status);
			} else if (filter.equalsIgnoreCase("zip")) {
				pharmacyVerificationList = pharmacyPostSyncResultsRepository.getPharmacyDataPostSyncZip(fileId,
						searchTerm, status);
			} else if (filter.equalsIgnoreCase("address")) {
				pharmacyVerificationList = pharmacyPostSyncResultsRepository.getPharmacyDataPostSyncAddress(fileId,
						searchTerm, status);
			}
		}

		long totalHitCount = pharmacyVerificationList.size();
		int fromIndex = pageNo * pageSize;
		int toIndex = Math.min(fromIndex + pageSize, pharmacyVerificationList.size());
		List<PharmacyPostSyncResultsModel> hits = pharmacyVerificationList.subList(fromIndex, toIndex);
		return new PageImpl<>(hits, paging, totalHitCount);
	}

	public Page<PharmacyPostSyncResultsModel> getPharmacyPostSyncresults(int fileId, String status, int pageSize,
			int pageNumber) {
		Pageable paging = PageRequest.of(pageNumber, pageSize);
		List<PharmacyPostSyncResultsModel> pharmacyPostSyncList = pharmacyPostSyncResultsRepository
				.pharmacyPostSyncDataResults(fileId, status);
		Page<PharmacyPostSyncResultsModel> pagedResult = new PageImpl<>(
				pharmacyPostSyncList.subList(Math.min(pageNumber * pageSize, pharmacyPostSyncList.size()),
						Math.min((pageNumber + 1) * pageSize, pharmacyPostSyncList.size())),
				paging, pharmacyPostSyncList.size());
		return pagedResult;
	}

//public List<TableRecordCounts> getTableRecordCounts()
//{
//	return icdTableRecordCountsRepository.getTableRecordCounts();
//}

	@Override
	protected CustomResponse saveCodeVerificationLogDetails(CodeVerification codes, String codeset, int fileId,
			int userId, String notes) {
		CodeMaintenanceFile zipFile = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
		int id = 0;
		List<PharmacyDataVerificationModel> acceptedList = new LinkedList<>();
		List<PharmacyDataVerificationModel> rejectedList = new LinkedList<>();

		List<String> accepted = codes.getAcceptedCodes();
		List<String> rejected = codes.getRejectedCodes();

		List<PharmacyDataVerificationModel> verifiedAcceptedList = acceptedVerification(accepted, acceptedList);
		List<PharmacyDataVerificationModel> verifiedRejectedList = rejectedVerification(rejected, rejectedList);

		// System.out.println("AFTER>>>>"+verifiedList);
		try {
			System.out.println("inside the try...........");
			System.out.println("codeset :: " + codeset);
			System.out.println("fileId :: " + fileId);
			System.out.println("verifiedAcceptedList  :" + verifiedAcceptedList);
			
			saveAccepted(verifiedAcceptedList, codeset, fileId, userId, notes, zipFile);
			saveRejected(verifiedRejectedList, codeset, fileId, userId, notes, zipFile);

		} catch (Exception e) {
			return new CustomResponse("Pharmacy Verification Log Saving Failed", e.getLocalizedMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (rejected.size() > 0) {
			zipFile.setProcessedState("Verification Rejected");
			zipFile.setStatus("Failed");
			zipFile.setNextState("");
			zipFile.setCurrentStatus("Verification Rejected");
			zipFile.setActive(0);
			String targetCodeStandardFolder = "pharmacy";
			String dateFormatPath = utilsService.getDateInStringFormat(zipFile.getReleaseDate(), "default");
			String rejectedFilePath = rootFolderName + "/" + rejectedFolderName + "/" + targetCodeStandardFolder + "/"
					+ dateFormatPath + "/" + zipFile.getFileName();
			System.out.println(">>>>>>>>>>>>>>>>>>>>" + rejectedFilePath);
			String targetFolderPath = rootFolderName + "/" + uploadFolderName + "/" + targetCodeStandardFolder + "/"
					+ zipFile.getFileName();
			System.out.println("+++++++++++" + targetFolderPath);
			s3Service.moveFile(targetFolderPath, rejectedFilePath, true);

			zipFile.setFilePath(rejectedFilePath);
			codeMaintenanceFileService.saveCodeMaintenanceFile(zipFile);
			return new CustomResponse("Pharmacy Verification Rejected",
					"Due to some codes are marked as rejected while verification ", HttpStatus.INTERNAL_SERVER_ERROR);
		} else if (accepted.size() > 0) {
			zipFile.setProcessedState("Verified");
			zipFile.setCurrentStatus("Verification Completed");
			zipFile.setNextState("Sync");
			codeMaintenanceFileService.saveCodeMaintenanceFile(zipFile);
		}

		// icdVerificationLogRepository.saveAll(icdDataVerificationModel);
		return new CustomResponse("Pharmacy Verification Success", "", HttpStatus.OK);
	}

	public List<TableRecordCounts> getTableRecordCounts() {
		return pharmacyTableRecordCountsRepository.getTableRecordCounts();
	}

	public List<PharmacyDataVerificationModel> acceptedVerification(List<String> accepted,
			List<PharmacyDataVerificationModel> acceptedList) {

		for (String ncpdpCode : accepted) {

			PharmacyDataVerificationModel model = pharmacyDataVerificationRepository.findByNcpdpid(ncpdpCode);

			if (model != null) {

				model.setVerificationState("Accepted");

				acceptedList.add(model);
			}

		}
		return acceptedList;
	}

	public List<PharmacyDataVerificationModel> rejectedVerification(List<String> rejected,
			List<PharmacyDataVerificationModel> rejectedList) {

		for (String ncpdpCode : rejected) {

			PharmacyDataVerificationModel model = pharmacyDataVerificationRepository.findByNcpdpid(ncpdpCode);

			if (model != null) {

				model.setVerificationState("Rejected");
				rejectedList.add(model);
			}

		}
		return rejectedList;
	}

	public void saveAccepted(List<PharmacyDataVerificationModel> verifiedAcceptedList, String codeset, int fileId,
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

			for (PharmacyDataVerificationModel verifiedModel : verifiedAcceptedList) {

				pharmacyDataVerificationRepository.save(verifiedModel);

				logModel.setFileId(fileId);
				logModel.setCodeset(codeset);
				logModel.setCode(verifiedModel.getNcpdpid());
				logModel.setUserId(userId);
				Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());
				logModel.setInsertedDate(newModifiedDate);
				logModel.setNotes(notes);
				logModel.setVerifiedState("Accepted");
				codeVerificationLogRepository.save(logModel);
				zipFile.setProcessedState("Verified");
				zipFile.setCurrentStatus("Verification Complete");
				zipFile.setNextState("Sync");
				codeMaintenanceFileService.saveCodeMaintenanceFile(zipFile);
				codeMaintenanceLoggerService.saveCodeMaintenanceLog(verifiedModel.getFileId(), "code verification",
						"Following Pharmacy Ncpdpid verified:" + verifiedModel.getNcpdpid(), userId);
			}
		}
	}

	public void saveRejected(List<PharmacyDataVerificationModel> verifiedRejectedList, String codeset, int fileId,
			int userId, String notes, CodeMaintenanceFile zipFile) {
		for (PharmacyDataVerificationModel verifiedModel : verifiedRejectedList) {

			pharmacyDataVerificationRepository.save(verifiedModel);

			CodeVerificationLogModel logModel = new CodeVerificationLogModel();

			logModel.setFileId(fileId);
			logModel.setCodeset(codeset);
			logModel.setCode(verifiedModel.getNcpdpid());
			logModel.setUserId(userId);
			Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());
			logModel.setInsertedDate(newModifiedDate);
			logModel.setNotes(notes);
			logModel.setVerifiedState("Rejected");
			codeVerificationLogRepository.save(logModel);
			zipFile.setProcessedState("Verified");
			zipFile.setNextState("Sync");
			zipFile.setCurrentStatus("Verification Complete");
			codeMaintenanceLoggerService.saveCodeMaintenanceLog(verifiedModel.getFileId(), "code verification",
					"Following Pharmacy Ncpdpid verified:" + verifiedModel.getNcpdpid(), userId);
		}
	}

	public Page<PharmacyPostSyncResultsModel> getPharmaciesVerificationSearch(String searchTerm, Pageable pageable) {
		// TODO Auto-generated method stub
		return pharmacySyncResultsRepository.getPharmaciesVerificationSearch(searchTerm, pageable);
	}

	public Page<PharmacyPostSyncResultsModel> getAllCount(Pageable pageable) {
		// TODO Auto-generated method stub
		return pharmacyPostSyncResultsRepository.findValidatedRecords(pageable);
	}

	public List<VersionSummary> getPharmacyVersionSummaryData(int fileId, int userId) {
		// TODO Auto-generated method stub
		VersionSummary versionSummary = new VersionSummary();

		CodeMaintenanceFile file = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
		int totalValidRecords = getPharmacyTotalRecords(fileId);

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
		versionSummary.setCurrentVersionProcessedDate(Timestamp.valueOf(LocalDateTime.now()));
		versionSummary.setReleaseDate(file.getReleaseDate());
		versionSummary.setCurrentVersionTotalRecords(totalValidRecords);

		// Save the version summary
		versionSummaryRepository.save(versionSummary);

		return null;
	}

	public int getPharmacyTotalRecords(int fileId) {

		return pharmacyVersionSummaryRepository.currentVersionPharmacyTotalRecords(fileId);
	}

}