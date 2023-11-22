package com.io.codesystem.cpt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.io.codesystem.search.cpt.CptCodeSearchService;
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
public class CptCodeMaintenanceService extends CodeMaintenanceService {

	@Autowired
	S3Service s3Service;

	@Autowired
	UtilsService utilsService;

	@Autowired
	CodeMaintenanceFileService codeMaintenanceFileService;

	@Autowired
	CodeMaintenanceLoggerService codeMaintenanceLoggerService;

	@Autowired
	CptCodeStandardRepository cptCodeStandardRepository;

	@Autowired
	CptSyncResultsRepository cptSynchResultsRepository;

	@Autowired
	CptDataVerificationRepository cptDataVerificationRepository;

	@Autowired
	CptPostSyncResultsRepository cptPostSyncResultsRepository;

	@Autowired
	CptTableRecordCountsRepository cptTableRecordCountsRepository;

	@Autowired
	CodeVerificationLogRepository codeVerificationLogRepository;

	@Autowired
	AsyncTasksStatusService asyncTasksStatusService;

	@Autowired
	CptCodeSearchService cptCodeSearchService;

	@Autowired
	CptVersionSummaryRepository cptVersionSummaryRepository;

	@Autowired
	VersionSummaryRepository versionSummaryRepository;

	// @Autowired
	// ValidationCheckRepository validationCheckRepository;

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
		log.info("===== ZipFileName :: " + zipFileName);
		String targetCodeTypeFolderName = utilsService.getTargetCodeTypeFolderName(codeType);
		log.info("===== TargetCodeType FolderName :: " + targetCodeTypeFolderName);
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
			// CodeMaintenanceFile codeMaintenanceFile=new CodeMaintenanceFile();
			// codeMaintenanceFile.setStatus("failure");
			return new CustomResponse("Zip File Uploading Failed", "Error:" + e.getLocalizedMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}

		return new CustomResponse("Zip File Uploaded Successfully", "", HttpStatus.OK);
	}

	@Override
	@Async
	protected void processData(int fileId, int userId) {
		// TODO Auto-generated method stub
		processAndPrepareVerificationData(fileId, userId);

	}

	protected CustomResponse processAndPrepareVerificationData(int fileId, int userId) {
		// TODO Auto-generated method stub
		try {
			CodeMaintenanceFile zipFile = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
			log.info("===== ZipFile :: " + zipFile);
			if (zipFile != null & zipFile.getProcessedState().equalsIgnoreCase("Uploaded")) {

				/*
				 * log.info("IN IF>>>>>>>>>>>>>>>>>>");
				 * if(utilsService.prepareVerificationStatus("version-validation",
				 * zipFile.getCodeStandard(), zipFile.getReleaseDate())){ return new
				 * CustomResponse("Zip File Processing Failed"
				 * ,"Error:Current Processing File Version is olderthan already existing version"
				 * , HttpStatus.INTERNAL_SERVER_ERROR); }
				 */
				ValidationCheck validationCheck = utilsService.getInprocessFileId("cpt",
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
						"Zip File Extracted And Read Text File", "In Process", 40, userId);

				parseTargetCodeDataFromFile(zipInputStream, targetCodeDataDetailsMap, fileId, userId);
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileProcess",
						"Dump Table Created And Verification Data Prepared ", "In Process", 60, userId);

				String targetCodeStandardFolder = "cpt";
				String uploadFilePath = zipFile.getFilePath();
				log.info("===== Upload FilePath :: " + uploadFilePath);

				String dateFormatPath = utilsService.getDateInStringFormat(zipFile.getReleaseDate(), "default");
				String inProcessFilePath = rootFolderName + "/" + inprocessFolderName + "/" + targetCodeStandardFolder
						+ "/" + dateFormatPath + "/" + zipFile.getFileName();
				log.info("======InProcess FilePath :: " + inProcessFilePath);

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
			zipFile.setComments("CPT File Process Failed");
			zipFile.setCurrentStatus("File Process Failed");
			codeMaintenanceFileService.saveCodeMaintenanceFile(zipFile);
		}
		return new CustomResponse("CPT Code Maintenance File Processed Successfully", "", HttpStatus.OK);
	}

	// return new CustomResponse("CPT Code Maintenance File Process Failed", "",
	// HttpStatus.INTERNAL_SERVER_ERROR);

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

	@Override
	@Async
	protected void syncVerifiedData(int fileId, int userId) {

		syncCptCodeMaintenanceDataWithExistingData(fileId, userId);
	}

	public Map<String, String> getTargetCodeDataFilePath(String zipFileName) {

		Map<String, String> targetCodeDataFileDetailsMap = new HashMap<>();

		String zipFolderName = zipFileName.replace(".zip", "");
		String shortDescFilePath = zipFolderName.replace("_", " ") + "/SHORTU.txt";
		String mediumDescFilePath = zipFolderName.replace("_", " ") + "/MEDU.txt";
		String longDescFilePath = zipFolderName.replace("_", " ") + "/LONGULT.txt";

		targetCodeDataFileDetailsMap.put("shortDescFilePath", shortDescFilePath);
		targetCodeDataFileDetailsMap.put("mediumDescFilePath", mediumDescFilePath);
		targetCodeDataFileDetailsMap.put("longDescFilePath", longDescFilePath);

		targetCodeDataFileDetailsMap.put("tempTableName", zipFolderName.replaceAll(" ", "_"));

		return targetCodeDataFileDetailsMap;
	}

	public List<CptCodeStandardModel> parseTargetCodeDataFromFile(InputStream zipInputStream,
			Map<String, String> targetCodeDataDetailsMap, int fileId, int userId) {

		List<CptCodeStandardModel> allCptCodeList = null;
		InputStream shortDescTargetDataFileStream, mediumDescTargetDataFileStream, longDescTargetDataFileStream = null;
		try {

			shortDescTargetDataFileStream = utilsService.getTargetFileStreamFromZipFile(zipInputStream,
					targetCodeDataDetailsMap.get("shortDescFilePath"));
			zipInputStream.reset();
			mediumDescTargetDataFileStream = utilsService.getTargetFileStreamFromZipFile(zipInputStream,
					targetCodeDataDetailsMap.get("mediumDescFilePath"));
			zipInputStream.reset();
			longDescTargetDataFileStream = utilsService.getTargetFileStreamFromZipFile(zipInputStream,
					targetCodeDataDetailsMap.get("longDescFilePath"));
			zipInputStream.reset();

			Map<String, String> shortDescMap = prepareTargetCodeEntityFromInputStream(shortDescTargetDataFileStream);

			Map<String, String> mediumDescMap = prepareTargetCodeEntityFromInputStream(mediumDescTargetDataFileStream);

			Map<String, String> longDescMap = prepareTargetCodeEntityFromInputStream(longDescTargetDataFileStream);

			allCptCodeList = mergeCptCodeAllDataFiles(shortDescMap, mediumDescMap, longDescMap);
			saveCptCodeStandardList(allCptCodeList, targetCodeDataDetailsMap.get("tempTableName"), fileId, userId);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return allCptCodeList;
	}

	public Map<String, String> prepareTargetCodeEntityFromInputStream(InputStream targetDataFileStream)
			throws IOException {

		Map<String, String> targetCptDescMap = new HashMap<>();

		if (targetDataFileStream != null) {

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(targetDataFileStream))) {

				// System.out.println(">>>>>>>nul" + targetDataFileStream);
				String line;
				reader.readLine(); // Assuming the first line is a header and should be skipped
				while ((line = reader.readLine()) != null) {
					// System.out.println(line);
					if (line.matches("\\d{4}[\\dA-Za-z]\\s.*")) {

						String[] splitLine = line.trim().split("\\s+", 2);
						// System.out.println(splitLine);
						String code = splitLine[0];
						String codeDesc = splitLine[1];
						targetCptDescMap.put(code, codeDesc);
					}
				}
			}
		}

		return targetCptDescMap;
	}

	private void saveCptCodeStandardList(List<CptCodeStandardModel> allCptCodeList, String newTableName, int fileId,
			int userId) {

		try {

			utilsService.truncateTable("cpt_standard_versions");
			CodeMaintenanceFile codeMaintenanceFile = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
			String releaseDate = utilsService.getDateInStringFormat(codeMaintenanceFile.getReleaseDate(), "default");
			newTableName = newTableName + "_" + releaseDate;
			utilsService.dropTable(newTableName);

			cptCodeStandardRepository.saveAll(allCptCodeList);

			utilsService.createNewTableFromExisting(newTableName, "cpt_standard_versions");
			codeMaintenanceFileService.updateCodeMaintenanceFileStatusById(fileId, "In Process", "Dump Table Created",
					userId);
			codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "Dump Table Created",
					"Dump Table Created Successfully", userId);
			cptCodeStandardRepository.prepareCptDataForVerification(fileId, newTableName, userId);
			codeMaintenanceFileService.updateCodeMaintenanceFileStatusById(fileId, "Pending For Verification",
					"Pending For Verification", userId);
			codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "CPT Verification Data Prepared",
					"CPT Verification Data Prepared Successfully", userId);

		} catch (Exception e) {

			codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "CPT Verification Data Prepared Failed",
					"CPT Verification Data Preparation Failed", userId);
			System.out.println(e.getMessage());

		}
		// TODO Auto-generated method stub

	}

	public List<CptCodeStandardModel> mergeCptCodeAllDataFiles(Map<String, String> shortCptCodeMap,
			Map<String, String> mediumCptCodeMap, Map<String, String> longCptCodeMap) {

		Set<String> allCptCodes = new HashSet<>();
		allCptCodes.addAll(shortCptCodeMap.keySet());
		allCptCodes.addAll(mediumCptCodeMap.keySet());
		allCptCodes.addAll(longCptCodeMap.keySet());
		List<CptCodeStandardModel> cptCodesList = new ArrayList<>();
		for (String code : allCptCodes) {
			String shortDesc = shortCptCodeMap.getOrDefault(code, null);
			String mediumDesc = mediumCptCodeMap.getOrDefault(code, null);
			String longDesc = longCptCodeMap.getOrDefault(code, null);
			CptCodeStandardModel cptCode = new CptCodeStandardModel(code, shortDesc, mediumDesc, longDesc);
			cptCodesList.add(cptCode);
		}
		return cptCodesList;

	}

	public CustomResponse syncCptCodeMaintenanceDataWithExistingData(int fileId, int userId) {

		CodeMaintenanceFile codeMaintenanceFile = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
		log.info("===== CodeMaintenance File :: " + codeMaintenanceFile);
		asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync", "Code Sync Started", "In Process", 10,
				userId);

		if (codeMaintenanceFile != null) {
			String fileName = codeMaintenanceFile.getFileName();
			log.info("=====fileName:: " + fileName);
			String zipFolderName = fileName.replace(".zip", "");
			String targetFileName = zipFolderName;
			log.info("=====targetFileName:: " + targetFileName);
			String targetCodeStandardFolder = "cpt";

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
				cptSynchResultsRepository.cptCompareAndSyncTablesAdded(fileId, targetFileName, userId);
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync", "Added Codes Sync Process Completed",
						"In Process", 50, userId);

				log.info("==================After Adding");
				cptSynchResultsRepository.cptCompareAndSyncTablesUpdated(fileId, targetFileName, userId);
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync",
						"Updated Codes Sync Process Completed", "In Process", 70, userId);

				log.info("===================After Updating");
				cptSynchResultsRepository.cptCompareAndSyncTablesDeleted(fileId, targetFileName, userId);
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync",
						"Deleted Codes Sync Process Completed", "In Process", 90, userId);

				log.info("====================After Deleting");
				asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync", "File Sync Process Completed",
						"In Process", 95, userId);
				codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "CPT Data Synching Completed",
						"CPT Data Synching Completed Successfully", userId);
			} catch (Exception e) {

				System.out.println(e.getMessage());
			}

			utilsService.truncateTable("cpt_standard_versions");
			codeMaintenanceFileService.updateCodeMaintenanceFileStatusById(fileId, "Synced", "syncing completed",
					userId);

			codeMaintenanceFile = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);

			codeMaintenanceFile.setComments("CPT File Proceessed Successfully");
			Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());

			codeMaintenanceFile.setNextState("");
			codeMaintenanceFile.setModifiedDate(newModifiedDate);
			codeMaintenanceFileService.saveCodeMaintenanceFile(codeMaintenanceFile);
			asyncTasksStatusService.saveAsyncTaskStatusLog(fileId, "FileSync", "Code Sync process Completed",
					"Completed", 100, userId);

			// ================version summary table
			log.info("Call the Version Summary method After Sync...........");
			getCptVersionSummaryData(fileId, userId);

			String processedFilePath = rootFolderName + "/" + processedFolderName + "/" + targetCodeStandardFolder + "/"
					+ dateFormatPath + "/" + codeMaintenanceFile.getFileName();
			log.info("======= Processed FilePath :: " + processedFilePath);
			log.info("Source Key:" + inProcessFilePath + "......... Destination Key:" + processedFilePath);
			s3Service.moveFile(inProcessFilePath, processedFilePath, false);
			codeMaintenanceFileService.updateCodeMaintenanceFilePathById(fileId, processedFilePath);

			try {
				System.out.println("Before index====");
				cptCodeSearchService.createCptIndex();

			} catch (Exception e) {
				e.printStackTrace();
			}
			return new CustomResponse("CPT Code File Synched Successfully", "", HttpStatus.OK);
		} else {
			// Handle case where codeStandardFile is not found
			return new CustomResponse("CPT Code File Synch Failed", "", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public Page<CptDataVerificationModel> getCptCodeOrDescOrstatus(Integer fileId, String searchTerm, String status,
			int pageSize, int pageNumber) {
		Pageable paging = PageRequest.of(pageNumber, pageSize);
		List<CptDataVerificationModel> cptVerificationList = cptDataVerificationRepository
				.getCptCodeVerificationDetails(fileId, searchTerm, status);
		// System.out.println(">>>>>>>>>>>>>>TESTING>>>>>>>>>>>");
		Page<CptDataVerificationModel> pagedResult = new PageImpl<>(
				cptVerificationList.subList(Math.min(pageNumber * pageSize, cptVerificationList.size()),
						Math.min((pageNumber + 1) * pageSize, cptVerificationList.size())),
				paging, cptVerificationList.size());
		return pagedResult;
	}

	public Page<CptPostSyncResultsModel> getCptSearchByAfterSync(Integer fileId, String searchTerm, String status,
			int pageSize, int pageNumber) {
		Pageable paging = PageRequest.of(pageNumber, pageSize);
		// TODO Auto-generated method stub
		List<CptPostSyncResultsModel> cptSyncList = cptPostSyncResultsRepository.getCptSearchByAfterSync(fileId,
				searchTerm, status);
		// System.out.println(">>>>>>>>>>>>>>TESTING>>>>>>>>>>>");
		Page<CptPostSyncResultsModel> pagedResult = new PageImpl<>(
				cptSyncList.subList(Math.min(pageNumber * pageSize, cptSyncList.size()),
						Math.min((pageNumber + 1) * pageSize, cptSyncList.size())),
				paging, cptSyncList.size());
		return pagedResult;

	}

	public Page<CptPostSyncResultsModel> getCptPostSyncresults(int fileId, String status, int pageSize,
			int pageNumber) {

		Pageable paging = PageRequest.of(pageNumber, pageSize);
		List<CptPostSyncResultsModel> cptPostSyncList = cptPostSyncResultsRepository.cptPostSyncDataResults(fileId,
				status);
		Page<CptPostSyncResultsModel> pagedResult = new PageImpl<>(
				cptPostSyncList.subList(Math.min(pageNumber * pageSize, cptPostSyncList.size()),
						Math.min((pageNumber + 1) * pageSize, cptPostSyncList.size())),
				paging, cptPostSyncList.size());
		return pagedResult;

	}

	public List<TableRecordCounts> getTableRecordCounts() {
		return cptTableRecordCountsRepository.getTableRecordCounts();
	}

	public CustomResponse saveCodeVerificationLogDetails(CodeVerification codes, String codeset, int fileId, int userId,
			String notes) {
		// CodeMaintenanceFile zipFile =
		// codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
		CodeMaintenanceFile zipFile = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);

		int id = 0;
		List<CptDataVerificationModel> acceptedList = new LinkedList<>();
		List<CptDataVerificationModel> rejectedList = new LinkedList<>();

		List<String> accepted = codes.getAcceptedCodes();
		List<String> rejected = codes.getRejectedCodes();

		List<CptDataVerificationModel> verifiedAcceptedList = acceptedVerification(accepted, acceptedList);
		List<CptDataVerificationModel> verifiedRejectedList = rejectedVerification(rejected, rejectedList);

		try {
			System.out.println("inside the try...........");
			System.out.println("codeset :: " + codeset);
			System.out.println("fileId :: " + fileId);
			System.out.println("verifiedAcceptedList  :" + verifiedAcceptedList);

			saveAccepted(verifiedAcceptedList, codeset, fileId, userId, notes, zipFile);
			saveRejected(verifiedRejectedList, codeset, fileId, userId, notes, zipFile);

		} catch (Exception e) {
			return new CustomResponse("CPT Verification Log Saving Failed", e.getLocalizedMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (rejected.size() > 0) {
			zipFile.setProcessedState("Verification Rejected");
			zipFile.setStatus("Failed");
			zipFile.setNextState("");
			zipFile.setCurrentStatus("Verification Rejected");
			zipFile.setActive(0);
			String targetCodeStandardFolder = "cpt";
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
			return new CustomResponse("CPT Verification Rejected",
					"Due to some codes are marked as rejected while verification ", HttpStatus.INTERNAL_SERVER_ERROR);
		} else if (accepted.size() > 0) {
			zipFile.setProcessedState("Verified");
			zipFile.setCurrentStatus("Verification Complete");

			zipFile.setNextState("Sync");

			codeMaintenanceFileService.saveCodeMaintenanceFile(zipFile);
		}
		return new CustomResponse("CPT Verification Success", "", HttpStatus.OK);

	}

	public List<CptDataVerificationModel> acceptedVerification(List<String> accepted,
			List<CptDataVerificationModel> acceptedList) {

		for (String code : accepted) {

			CptDataVerificationModel model = cptDataVerificationRepository.findByCode(code);

			if (model != null) {

				model.setVerificationState("Accepted");

				acceptedList.add(model);
			}

		}
		return acceptedList;
	}

	public List<CptDataVerificationModel> rejectedVerification(List<String> rejected,
			List<CptDataVerificationModel> rejectedList) {

		for (String code : rejected) {

			CptDataVerificationModel model = cptDataVerificationRepository.findByCode(code);

			if (model != null) {

				model.setVerificationState("Rejected");

				rejectedList.add(model);
			}

		}
		return rejectedList;
	}

	public void saveAccepted(List<CptDataVerificationModel> verifiedAcceptedList, String codeset, int fileId,
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

			for (CptDataVerificationModel verifiedModel : verifiedAcceptedList) {

				cptDataVerificationRepository.save(verifiedModel);

				logModel.setFileId(fileId);
				logModel.setCodeset(codeset);
				logModel.setCode(verifiedModel.getCode());
				logModel.setUserId(userId);

				Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());
				logModel.setInsertedDate(newModifiedDate);
				logModel.setNotes(notes);
				logModel.setVerifiedState("Accepted");
				codeVerificationLogRepository.save(logModel);

				// zipFile.setProcessedState("Verified");
				// zipFile.setCurrentStatus("Verification Complete");
				// zipFile.setNextState("sync");
				// codeMaintenanceFileService.saveCodeMaintenanceFile(zipFile);
				// codeMaintenanceLoggerService.saveCodeMaintenanceLog(verifiedModel.getFileId(),
				// "code verification",
				// "Following cpt code verified:" + verifiedModel.getCode(), userId);
				codeMaintenanceLoggerService.saveCodeMaintenanceLog(verifiedModel.getFileId(), "code verification",
						"Following CPT Code verified:" + logModel.getCode(), userId);

			}
		}
	}

	public void saveRejected(List<CptDataVerificationModel> verifiedRejectedList, String codeset, int fileId,
			int userId, String notes, CodeMaintenanceFile zipFile) {
		for (CptDataVerificationModel verifiedModel : verifiedRejectedList) {

			cptDataVerificationRepository.save(verifiedModel);

			CodeVerificationLogModel logModel = new CodeVerificationLogModel();

			logModel.setFileId(fileId);
			logModel.setCodeset(codeset);
			logModel.setCode(verifiedModel.getCode());
			logModel.setUserId(userId);
			Timestamp newModifiedDate = Timestamp.valueOf(LocalDateTime.now());
			logModel.setInsertedDate(newModifiedDate);
			logModel.setNotes(notes);
			logModel.setVerifiedState("Rejected");
			codeVerificationLogRepository.save(logModel);

			zipFile.setProcessedState("Verification Rejected");
			zipFile.setNextState("");
			zipFile.setCurrentStatus("Verification Rejected");
			codeMaintenanceFileService.saveCodeMaintenanceFile(zipFile);
			// codeMaintenanceLoggerService.saveCodeMaintenanceLog(verifiedModel.getFileId(),
			// "code verification",
			// "Following cpt code verified:" + verifiedModel.getCode(), userId);
			codeMaintenanceLoggerService.saveCodeMaintenanceLog(verifiedModel.getFileId(), "code verification",
					"Following CPT Code rejected:" + logModel.getCode(), userId);

		}
	}

	public Page<CptPostSyncResultsModel> getCptVerificationSearch(String searchTerm, Pageable pageable) {
		// TODO Auto-generated method stub
		return cptPostSyncResultsRepository.getCptVerificationSearch(searchTerm, pageable);
	}

	public Page<CptPostSyncResultsModel> getAllCount(Pageable pageable) {
		// TODO Auto-generated method stub
		return cptPostSyncResultsRepository.findValidatedRecords(pageable);
	}

	public List<VersionSummary> getCptVersionSummaryData(int fileId, int userId) {
		// TODO Auto-generated method stub
		VersionSummary versionSummary = new VersionSummary();

		CodeMaintenanceFile file = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
		int totalValidRecords = getCptTotalRecords(fileId);

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

	public int getCptTotalRecords(int fileId) {

		return cptVersionSummaryRepository.currentVersionCptTotalRecords(fileId);
	}

}
