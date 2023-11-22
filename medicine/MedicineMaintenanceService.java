package com.io.codesystem.medicine;

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
import com.io.codesystem.codechanges.CodeChangeCountsRepository;
import com.io.codesystem.codemaintenancefile.CodeMaintenanceFile;
import com.io.codesystem.codemaintenancefile.CodeMaintenanceFileService;
import com.io.codesystem.codemaintenancelog.CodeMaintenanceLoggerService;
import com.io.codesystem.search.medicine.MedicineService;
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
public class MedicineMaintenanceService extends CodeMaintenanceService {

	@Autowired
	S3Service s3Service;

	@Autowired
	UtilsService utilsService;

	@Autowired
	CodeMaintenanceFileService codeMaintenanceFileService;

	@Autowired
	CodeMaintenanceLoggerService codeMaintenanceLoggerService;

	@Autowired
	MedicineStandardRepository medicineStandardRepository;

	@Autowired
	MedicineSyncResultsRepository medicineSyncResultsRepository;

	@Autowired
	MedicineDataVerificationRepository medicineDataVerificationRepository;

	@Autowired
	MedicinePostSyncResultsRepository medicinePostSyncResultsRepository;

	@Autowired
	MedicineTableRecordCountsRepository medicineTableRecordCountsRepository;

	@Autowired
	CodeVerificationLogRepository codeVerificationLogRepository;

	@Autowired
	AsyncTasksStatusService asyncTasksStatusService;

	@Autowired
	VersionSummaryRepository versionSummaryRepository;

	@Autowired
	MedicineVersionSummaryRepository medicineVersionSummaryRepository;

	@Autowired
	CodeChangeCountsRepository codeChangeCountsRepository;

	@Autowired
	MedicineService medicineService;

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
				ValidationCheck validationCheck = utilsService.getInprocessFileId("medicine",
						java.sql.Date.valueOf(LocalDate.now()));
				if (validationCheck.getId() != 0) {
					utilsService.resetCodeFileSystem(validationCheck.getId(), userId);
				}
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileProcess", "File Process Started",
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

				String targetCodeStandardFolder = "medicine";
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
			zipFile.setComments("Medicines File Process Failed");
			zipFile.setCurrentStatus("File Process Failed");
			codeMaintenanceFileService.saveCodeMaintenanceFile(zipFile);
		}
		return new CustomResponse("Medicine Maintenance File Processed Successfully", "", HttpStatus.OK);
		// return new CustomResponse("Medicine Maintenance File Process Failed", "",
		// HttpStatus.INTERNAL_SERVER_ERROR);
	}

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
		log.info("====== Verification Message :: " + verificationMessage);
		codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, verificationMessage + " marked as Verified",
				"Code Maintenance Data (" + verifiedType + ") Verified", userId);

		return new CustomResponse(verificationMessage + " marked as Verified", "", HttpStatus.OK);

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
			List<MedicineStandardModel> medicineStandardList = prepareTargetCodeEntityFromInputStream(
					targetDataFileStream);
			saveMedicineStandardList(medicineStandardList, targetCodeDataDetailsMap.get("tempTableName"), fileId,
					userId);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public List<MedicineStandardModel> prepareTargetCodeEntityFromInputStream(InputStream targetDataFileStream)
			throws IOException {
		List<MedicineStandardModel> medicineList = new LinkedList<>();

		if (targetDataFileStream != null) {

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(targetDataFileStream))) {

				CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
				Iterable<CSVRecord> csvRecords = csvParser.getRecords();

				/*
				 * CSVParser csvParser = new CSVParser( reader, CSVFormat.EXCEL .withQuote('"')
				 * .withEscape('\\') .withDelimiter(',') .withFirstRecordAsHeader());
				 * Iterable<CSVRecord> csvRecords = csvParser.getRecords();
				 */
				for (CSVRecord data : csvRecords) {
					MedicineStandardModel entity = new MedicineStandardModel();

					entity.setId(Integer.parseInt(data.get(0)));
					entity.setNdc(data.get(1));
					entity.setName(data.get(2));
					entity.setDea(Integer.parseInt(data.get(3)));
					entity.setObsdtec(data.get(4));
					entity.setRepack(Integer.parseInt(data.get(5)));
					entity.setIsCompounded(data.get(6));

					medicineList.add(entity);
				}
				csvParser.close();
				return medicineList;
			}

		} else {
			throw new IllegalArgumentException("The targetDataFileStream cannot be null.");
		}
	}

	public void saveMedicineStandardList(List<MedicineStandardModel> medicineStandardList, String newTableName,
			int fileId, int userId) {

		try {

			utilsService.truncateTable("medicines_standard_versions");
			CodeMaintenanceFile codeMaintenanceFile = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
			String releaseDate = utilsService.getDateInStringFormat(codeMaintenanceFile.getReleaseDate(), "default");
			newTableName = newTableName + "_" + releaseDate;
			utilsService.dropTable(newTableName);
			medicineStandardRepository.saveAll(medicineStandardList);
			utilsService.createNewTableFromExisting(newTableName, "medicines_standard_versions");
			codeMaintenanceFileService.updateCodeMaintenanceFileStatusById(fileId, "In Process", "Dump Table Created",
					userId);
			codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "Dump Table Created",
					"Dump Table Created Successfully", userId);
//			asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "Dump Table Created",
//					"Dump Table Created Successfully", "In Process", 100, userId);
			medicineStandardRepository.prepareMedicineDataForVerification(fileId, newTableName, userId);
//			asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "Medicine Verification Data Prepared",
//					"Medicine Verification Data Prepared Successfully", "Process Completed", 100, userId);
			codeMaintenanceFileService.updateCodeMaintenanceFileStatusById(fileId, "Pending For Verification",
					"Pending For Verification", userId);
			codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "Medicine Verification Data Prepared",
					"Medicine Verification Data Prepared Successfully", userId);

		} catch (Exception e) {

			codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "Medicine Verification Data Prepared Failed",
					"Medicine Verification Data Preparation Failed", userId);
			System.out.println(e.getMessage());

		}

	}

	@Override
	@Async
	protected void syncVerifiedData(int fileId, int userId) {

		syncMedicineMaintenanceDataWithExistingData(fileId, userId);
	}

	protected CustomResponse syncMedicineMaintenanceDataWithExistingData(int fileId, int userId) {

		CodeMaintenanceFile codeMaintenanceFile = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
		log.info("===== CodeMaintenance File :: " + codeMaintenanceFile);

		// version summary table
		// log.info("Initial call the method.........");
		// getMedicineTotalRecords(fileId);

		asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync", "Code Sync Started", "In Process", 10,
				userId);

		if (codeMaintenanceFile != null) {
			String fileName = codeMaintenanceFile.getFileName();
			log.info("=====fileName:: " + fileName);
			String tempName = fileName.replace(".zip", "");
			log.info("=====tempName:: " + tempName);
			String targetCodeStandardFolder = "medicine";
			String targetFileName = tempName;// + ".csv";
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
				medicineSyncResultsRepository.AddedMedicineSynchProcedure(fileId, targetFileName, userId);
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync", "Added Codes Sync Process Completed",
						"In Process", 50, userId);
//				codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "Medicine Added Records Synching ",
//						"Medicine Added Records Synching Completed Successfully", userId);

				log.info("==================After Adding");
				medicineSyncResultsRepository.UpdatedMedicneSynchProcedure_Batch(fileId, targetFileName, userId);
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync",
						"Updated Codes Sync Process Completed", "In Process", 70, userId);
//				codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "Medicine Updated Records Synching ",
//						"Medicine Updated Records Synching Completed Successfully", userId);

				log.info("===================After Updating");
				medicineSyncResultsRepository.DeletedMedicineSynchProcedure(fileId, targetFileName, userId);
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync",
						"Deleted Codes Sync Process Completed", "In Process", 90, userId);
//				codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "Medicine Deleted Records Synching ",
//						"Medicine Deleted Records Synching Completed Successfully", userId);

				log.info("====================After Deleting");
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync", "File Sync Process Completed",
						"In Process", 95, userId);
				codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "Medicine File Synching Completed",
						"Medicine File Synching Completed Successfully", userId);

			} catch (Exception e) {

				System.out.println(e.getMessage());
			}

			utilsService.truncateTable("medicines_standard_versions");
			codeMaintenanceFileService.updateCodeMaintenanceFileStatusById(fileId, "Synced", "Syncing completed",
					userId);
			codeMaintenanceFile = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
			codeMaintenanceFile.setComments("Medicine File Synced Successfully");
			Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());

			codeMaintenanceFile.setNextState("");
			codeMaintenanceFile.setModifiedDate(newModifiedDate);
			codeMaintenanceFileService.saveCodeMaintenanceFile(codeMaintenanceFile);
			asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync", "Code Sync Process Completed",
					"Completed", 100, userId);

			// version summary table
			log.info("Call the Version Summary method After Sync...........");
			getMedicineVersionSummaryData(fileId, userId);

			String processedFilePath = rootFolderName + "/" + processedFolderName + "/" + targetCodeStandardFolder + "/"
					+ dateFormatPath + "/" + codeMaintenanceFile.getFileName();
			log.info("======= Processed FilePath :: " + processedFilePath);
			log.info("Source Key:" + inProcessFilePath + "......... Destination Key:" + processedFilePath);
			s3Service.moveFile(inProcessFilePath, processedFilePath, false);
			codeMaintenanceFileService.updateCodeMaintenanceFilePathById(fileId, processedFilePath);

			try {
				log.info("Before index====");
				medicineService.createMedicineIndex();

			} catch (Exception e) {
				e.printStackTrace();
			}
			return new CustomResponse("Medicine File Synced Successfully", "", HttpStatus.OK);
		}

		else {
			// Handle case where codeStandardFile is not found
			asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "File Sync", "File Sync process Failed", "Failed", 0,
					userId);
			return new CustomResponse("Medicine File Sync Failed", "", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 * private void createMedcineIndex() { // TODO Auto-generated method stub
	 * SearchSession searchSession = Search.session(entityManager); MassIndexer
	 * massIndexer = searchSession.massIndexer(Medicine.class);
	 * massIndexer.type(Medicine.class);
	 * massIndexer.idFetchSize(250).batchSizeToLoadObjects(200).threadsToLoadObjects
	 * (4).startAndWait(); }
	 */
	public Page<MedicineDataVerificationModel> getNDCorName(Integer fileId, String searchTerm, String status,
			int pageSize, int pageNumber) {
		Pageable paging = PageRequest.of(pageNumber, pageSize);
		List<MedicineDataVerificationModel> medicinePostSyncList = medicineDataVerificationRepository
				.getMedicineVerificationDetails(fileId, searchTerm, status);
		Page<MedicineDataVerificationModel> pagedResult = new PageImpl<>(
				medicinePostSyncList.subList(Math.min(pageNumber * pageSize, medicinePostSyncList.size()),
						Math.min((pageNumber + 1) * pageSize, medicinePostSyncList.size())),
				paging, medicinePostSyncList.size());

		return pagedResult;
	}

	public Page<MedicinePostSyncResultsModel> getMedicinePostSyncresults(int fileId, String status, int pageSize,
			int pageNumber) {
		Pageable paging = PageRequest.of(pageNumber, pageSize);
		List<MedicinePostSyncResultsModel> medicinePostSyncList = medicinePostSyncResultsRepository
				.medicinePostSyncDataResults(fileId, status);
		Page<MedicinePostSyncResultsModel> pagedResult = new PageImpl<>(
				medicinePostSyncList.subList(Math.min(pageNumber * pageSize, medicinePostSyncList.size()),
						Math.min((pageNumber + 1) * pageSize, medicinePostSyncList.size())),
				paging, medicinePostSyncList.size());
		return pagedResult;

	}

	public List<TableRecordCounts> getTableRecordCounts() {
		return medicineTableRecordCountsRepository.getTableRecordCounts();
	}

	// Define custom repository methods for finding by ID and NDC
	@Override
	protected CustomResponse saveCodeVerificationLogDetails(CodeVerification codes, String codeset, int fileId,
			int userId, String notes) {
		CodeMaintenanceFile zipFile = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
		log.info("zipFile Data:" + zipFile.toString());
		System.out.println("codeset :: " + codeset);
		System.out.println("fileId :: " + fileId);

		List<MedicineDataVerificationModel> acceptedList = new LinkedList<>();
		log.info(">>>>>acceptedList Data :: " + acceptedList.toString());
		List<MedicineDataVerificationModel> rejectedList = new LinkedList<>();
		log.info(">>>>>rejectedList Data :: " + rejectedList.toString());

		List<String> accepted = codes.getAcceptedCodes();
		log.info("===== accepted Data :: " + accepted.toString());
		List<String> rejected = codes.getRejectedCodes();
		log.info("===== rejected Data :: " + rejected.toString());

		List<MedicineDataVerificationModel> verifiedAcceptedList = acceptedVerification(accepted, acceptedList);
		log.info("===== verifiedAcceptedList Data :: " + verifiedAcceptedList.toString());
		List<MedicineDataVerificationModel> verifiedRejectedList = rejectedVerification(rejected, rejectedList);
		log.info("===== verifiedRejectedListt Data :: " + verifiedRejectedList.toString());
		// System.out.println("AFTER>>>>"+verifiedList);
		try {
			System.out.println("inside the try...........");
			System.out.println("codeset :: " + codeset);
			System.out.println("fileId :: " + fileId);
			System.out.println("verifiedAcceptedList  :" + verifiedAcceptedList);

			saveAccepted(verifiedAcceptedList, codeset, fileId, userId, notes, zipFile);
			saveRejected(verifiedRejectedList, codeset, fileId, userId, notes, zipFile);

		} catch (Exception e) {
			return new CustomResponse("Medicine Verification Log Saving Failed", e.getLocalizedMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (rejected.size() > 0) {
			zipFile.setProcessedState("Verification Rejected");
			zipFile.setStatus("Failed");
			zipFile.setNextState("");
			zipFile.setCurrentStatus("Verification Rejected");
			zipFile.setActive(0);
			String targetCodeStandardFolder = "medicine";
			String dateFormatPath = utilsService.getDateInStringFormat(zipFile.getReleaseDate(), "default");
			String rejectedFilePath = rootFolderName + "/" + rejectedFolderName + "/" + targetCodeStandardFolder + "/"
					+ dateFormatPath + "/" + zipFile.getFileName();
			log.info("==== Rejected FilePath :: " + rejectedFilePath);
			String targetFolderPath = rootFolderName + "/" + uploadFolderName + "/" + targetCodeStandardFolder + "/"
					+ zipFile.getFileName();
			log.info("==== Target FolderPath :: " + targetFolderPath);
			s3Service.moveFile(targetFolderPath, rejectedFilePath, true);
			zipFile.setFilePath(rejectedFilePath);
			codeMaintenanceFileService.saveCodeMaintenanceFile(zipFile);

			return new CustomResponse("Medicine Verification Rejected",
					"Due to some codes are marked as rejected while verification ", HttpStatus.INTERNAL_SERVER_ERROR);

		} else if (accepted.size() > 0) {
			zipFile.setProcessedState("Verified");
			zipFile.setCurrentStatus("Verification Complete");
			zipFile.setNextState("Sync");
			codeMaintenanceFileService.saveCodeMaintenanceFile(zipFile);
		}
		// icdVerificationLogRepository.saveAll(icdDataVerificationModel);
		return new CustomResponse("Medicine Verification Success", "", HttpStatus.OK);
	}

	public List<MedicineDataVerificationModel> acceptedVerification(List<String> accepted,
			List<MedicineDataVerificationModel> acceptedList) {

		for (String ndc : accepted) {

			MedicineDataVerificationModel model = medicineDataVerificationRepository.findByNdc(ndc);

			if (model != null) {

				model.setVerificationState("Accepted");
				acceptedList.add(model);
			}

		}
		return acceptedList;
	}

	public List<MedicineDataVerificationModel> rejectedVerification(List<String> rejected,
			List<MedicineDataVerificationModel> rejectedList) {

		for (String ndc : rejected) {

			MedicineDataVerificationModel model = medicineDataVerificationRepository.findByNdc(ndc);

			if (model != null) {

				model.setVerificationState("Rejected");
				rejectedList.add(model);
			}

		}
		return rejectedList;
	}

	public void saveAccepted(List<MedicineDataVerificationModel> verifiedAcceptedList, String codeset, int fileId,
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
			for (MedicineDataVerificationModel verifiedModel : verifiedAcceptedList) {

				medicineDataVerificationRepository.save(verifiedModel);
				// CodeVerificationLogModel logModel = new CodeVerificationLogModel();

				logModel.setFileId(fileId);
				logModel.setCodeset(codeset);
				logModel.setCode(verifiedModel.getNdc());
				logModel.setUserId(userId);
				Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());
				logModel.setInsertedDate(newModifiedDate);
				logModel.setNotes(notes);
				logModel.setVerifiedState("Accepted");
				codeVerificationLogRepository.save(logModel);

				// zipFile.setProcessedState("Verified");
				// zipFile.setCurrentStatus("Verification Complete");
				// zipFile.setNextState("Sync");
				// codeMaintenanceFileService.saveCodeMaintenanceFile(zipFile);
				codeMaintenanceLoggerService.saveCodeMaintenanceLog(verifiedModel.getFileId(), "Code Verification",
						"Following Medicine NDC Verified : " + verifiedModel.getNdc(), userId);
			}
		}
	}

	public void saveRejected(List<MedicineDataVerificationModel> verifiedRejectedList, String codeset, int fileId,
			int userId, String notes, CodeMaintenanceFile zipFile) {
		for (MedicineDataVerificationModel verifiedModel : verifiedRejectedList) {

			medicineDataVerificationRepository.save(verifiedModel);
			CodeVerificationLogModel logModel = new CodeVerificationLogModel();

			if (verifiedModel == null) {
				logModel.setFileId(fileId);
				logModel.setCodeset(codeset);
				logModel.setCode("");
				logModel.setUserId(userId);
				Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());
				logModel.setInsertedDate(newModifiedDate);
				logModel.setNotes(notes);
				logModel.setVerifiedState("Rejected");
				codeVerificationLogRepository.save(logModel);
			}
			logModel.setFileId(fileId);
			logModel.setCodeset(codeset);
			logModel.setCode(verifiedModel.getNdc());
			logModel.setUserId(userId);
			Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());
			logModel.setInsertedDate(newModifiedDate);
			logModel.setNotes(notes);
			logModel.setVerifiedState("Rejected");
			codeVerificationLogRepository.save(logModel);

			// zipFile.setProcessedState("Rejected");
			// zipFile.setNextState("");
			// zipFile.setCurrentStatus("Verification Rejected");
			codeMaintenanceLoggerService.saveCodeMaintenanceLog(verifiedModel.getFileId(), "Code Verification",
					"Following Medicine NDC Rejected : " + verifiedModel.getNdc(), userId);
		}
	}

	public Page<MedicinePostSyncResultsModel> getMedicineSearchAfterSync(Integer fileId, String searchTerm,
			String status, int pageSize, int pageNumber) {
		// TODO Auto-generated method stub
		Pageable paging = PageRequest.of(pageNumber, pageSize);
		List<MedicinePostSyncResultsModel> medicineSyncList = medicinePostSyncResultsRepository
				.getMedicineSearchAfterSync(fileId, searchTerm, status);
		Page<MedicinePostSyncResultsModel> pagedResult = new PageImpl<>(
				medicineSyncList.subList(Math.min(pageNumber * pageSize, medicineSyncList.size()),
						Math.min((pageNumber + 1) * pageSize, medicineSyncList.size())),
				paging, medicineSyncList.size());
		return pagedResult;
	}

	public Page<MedicinePostSyncResultsModel> getMedicinesVerificationSearch(String searchTerm, Pageable pageable) {
		// TODO Auto-generated method stub
		return medicinePostSyncResultsRepository.getMedicinesVerificationSearch(searchTerm, pageable);
	}

	public Page<MedicinePostSyncResultsModel> getAllCount(Pageable pageable) {
		// TODO Auto-generated method stub
		return medicinePostSyncResultsRepository.findValidatedRecords(pageable);
	}

	public List<VersionSummary> getMedicineVersionSummaryData(int fileId, int userId) {
		// TODO Auto-generated method stub
		VersionSummary versionSummary = new VersionSummary();

		CodeMaintenanceFile file = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
		int totalValidRecords = getMedicineTotalRecords(fileId);

		VersionSummary codestandard = versionSummaryRepository.findOrderByCodeStandard(file.getCodeStandard());
		System.out.println("outside if Statement............");
		System.out.println("CodeStandard------------" + file.getCodeStandard());
		System.out.println("fileId ------------ " + fileId);
		System.out.println("Codestandard===========" + codestandard);

		// Update previous_version, previous_version_total_records, and
		// previous_version_processed_date
		if (codestandard != null) {
			System.out.println("inside if Statement............");
			versionSummary.setPreviousVersion(codestandard.getCurrentVersion());
			versionSummary.setPreviousVersionTotalRecords(codestandard.getCurrentVersionTotalRecords());
			versionSummary.setPreviousVersionProcessedDate(codestandard.getCurrentVersionProcessedDate());
			System.out.println("After if Statement............");
		}

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
	//	Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now().withNano(0));
		versionSummary.setCurrentVersionProcessedDate(Timestamp.valueOf(LocalDateTime.now()));
		versionSummary.setReleaseDate(file.getReleaseDate());
		versionSummary.setCurrentVersionTotalRecords(totalValidRecords);

		// Save the version summary
		versionSummaryRepository.save(versionSummary);

		return null;
	}

	public int getMedicineTotalRecords(int fileId) {

		return medicineVersionSummaryRepository.currentVersionMedicineTotalRecords(fileId);
	}

}
