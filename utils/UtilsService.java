package com.io.codesystem.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.io.codesystem.codechanges.CodeChangeCounts;
import com.io.codesystem.codechanges.CodeChangeCountsRepository;
import com.io.codesystem.codemaintenancefile.CodeMaintenanceFile;
import com.io.codesystem.codemaintenancefile.CodeMaintenanceFileRepository;
import com.io.codesystem.codemaintenancefile.CodeMaintenanceFileService;
import com.io.codesystem.codemaintenancelog.CodeMaintenanceLoggerService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UtilsService {

	@Value("${aws.s3.icd-folder}")
	private String icdFolderName;

	@Value("${aws.s3.cpt-folder}")
	private String cptFolderName;

	@Value("${aws.s3.pharmacy-folder}")
	private String pharmacyFolderName;

	@Value("${aws.s3.medicine-folder}")
	private String medicineFolderName;

	@Value("${aws.s3.allergies-folder}")
	private String allergiesFolderName;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	CodeChangeCountsRepository codeChangeCountsRepository;

	@Autowired
	CodeMaintenanceFileService codeMaintenanceFileService;

	@Autowired
	ValidationCheckRepository validationCheckRepository;

	@Autowired
	CodeMaintenanceLoggerService codeMaintenanceLoggerService;

	@Autowired
	CodeMaintenanceFileRepository codeMaintenanceFileRepository;

	@Autowired
	S3Service s3Service;

	public String getTargetCodeTypeFolderName(String codeType) {

		switch (codeType) {

		case "icd":
			return icdFolderName;

		case "cpt":
			return cptFolderName;

		case "allergies":
			return allergiesFolderName;

		case "pharmacy":
			return pharmacyFolderName;

		case "medicine":
			return medicineFolderName;

		case "snomed":
			return "";

		default:
			return "invalid code type";

		}
	}

	public CodeMaintenanceFile prepareCodeMaintenaceFile(String codeType, String zipFileName, String filePath,
			String releaseVersion, Date releaseDate, int userId, String effectiveFrom, String effectiveTo) {

		CodeMaintenanceFile codeMaintenanceFile = new CodeMaintenanceFile();

		codeMaintenanceFile.setCodeStandard(codeType);
		codeMaintenanceFile.setFileName(zipFileName);
		codeMaintenanceFile.setFilePath(filePath);
		codeMaintenanceFile.setReleaseVersion(releaseVersion);
		codeMaintenanceFile.setReleaseDate(releaseDate);
		codeMaintenanceFile.setProcessedState("Uploaded");
		codeMaintenanceFile.setCurrentStatus("File Uploaded Successfully");
		codeMaintenanceFile.setNextState("Process");
		codeMaintenanceFile.setActive(1);
		codeMaintenanceFile.setUserId(userId);
//TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		long currentTimeMillis = System.currentTimeMillis();
		Date currentDate = new Date(currentTimeMillis);
		codeMaintenanceFile.setProcessedDate(currentDate);
		codeMaintenanceFile.setSource("AMA");
		codeMaintenanceFile.setStatus("Success");
//TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		codeMaintenanceFile.setModifiedDate(Timestamp.valueOf(LocalDateTime.now()));
		codeMaintenanceFile.setModifiedUserId(userId);
		codeMaintenanceFile.setComments("");
		codeMaintenanceFile.setEffectiveFrom(effectiveFrom);
		codeMaintenanceFile.setEffectiveTo(effectiveTo);

		return codeMaintenanceFile;

	}

	public InputStream getTargetFileStreamFromZipFile(InputStream zipInputStream, String targetFilePath)
			throws IOException {
		try (ZipInputStream zipStream = new ZipInputStream(zipInputStream)) {
			ZipEntry zipEntry;

			while ((zipEntry = zipStream.getNextEntry()) != null) {
				String entryName = zipEntry.getName();
				log.info("===== Entry Name ::" + entryName);
				log.info("===== Target FilePath :: " + targetFilePath);
				if (entryName.equalsIgnoreCase(targetFilePath)) {
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					byte[] buffer = new byte[4096];
					int bytesRead;

					while ((bytesRead = zipStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytesRead);
					}

					return new ByteArrayInputStream(outputStream.toByteArray());
				}
			}
		}

		return null;
	}

	@Transactional
	public String truncateTable(String tableName) {
		System.out.println("Truncating the Version Table");
		String status = "success";
		try {
			entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			status = "failed";
		}
		return status;
	}

	@Transactional
	public String dropTable(String tableName) {
		System.out.println("Dropping the dump Table if exists");
		String status = "success";
		try {
			entityManager.createNativeQuery("Drop TABLE IF EXISTS " + tableName).executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			status = "failed";
		}
		return status;
	}

	@Transactional
	public String createNewTableFromExisting(String newTableName, String standardTableName) {
		System.out.println("New Table Creating from version Table");
		String status = "success";
		try {
			System.out.println("Table have to created with dumpname");

			entityManager.createNativeQuery("CREATE TABLE " + newTableName + " AS SELECT * FROM " + standardTableName)
					.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			status = "failed";
		}
		return status;
	}

	public CodeChangeCounts getCodeChangeCounts(int fileId) {

		CodeMaintenanceFile file = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);

		String syncStatus = "";

		if (file != null) {

			if (file.getCurrentStatus().equalsIgnoreCase("Pending For Verification")) {
				syncStatus = "Pre Sync";
			}

			if (file.getCurrentStatus().equalsIgnoreCase("Syncing Completed")) {
				syncStatus = "Post Sync";
			}

		}
		System.out.println("===FileId :: " + fileId);
		return codeChangeCountsRepository.findByStatus(fileId, syncStatus);

	}

	public String getDateInStringFormat(Date date, String format) {

		if (format.equalsIgnoreCase("default"))
			format = "yyyyMMdd";

		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(date);
	}

	public boolean prepareVerificationStatus(String verificationType, String codeStandard, Date releaseDate) {

		ValidationCheck status = validationCheckRepository.prepareVerificationStatus("version-validation", codeStandard,
				releaseDate);
		if ("failed".equalsIgnoreCase(status.getMessage())) {

			return true;
		}
		return false;
	}
	
	public boolean checkingInprocessFilesList(String verificationType, String codeStandard, Date releaseDate) {
		// TODO Auto-generated method stub
		ValidationCheck status = validationCheckRepository.prepareVerificationStatus("version-validation", codeStandard,
				releaseDate);
		if ("aborted".equalsIgnoreCase(status.getMessage())) {

			return true;
		}
		return false;
	}

	public CustomResponse checkingInprocessFiles(String verificationType, String codeStandard, Date releaseDate) {

		ValidationCheck status = validationCheckRepository.prepareVerificationStatus("checking-inprocessfiles",
				codeStandard, releaseDate);
		if ("failed".equalsIgnoreCase(status.getMessage())) {

			return new CustomResponse("File Processed Failed due to other file is already inprocess",
					"Error:Latest File Version Already Processed Do You Want To Continue",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new CustomResponse("Success", "", HttpStatus.OK);
	}

	public ValidationCheck getInprocessFileId(String codeStandard, Date releaseDate) {

		ValidationCheck validationCheck = validationCheckRepository.prepareVerificationStatus("checking-inprocessfiles",
				codeStandard, releaseDate);
		return validationCheck;
	}

	public CustomResponse resetCodeFileSystem(int fileId, int userId) {
		Optional<CodeMaintenanceFile> codeMaintenanceFile = codeMaintenanceFileRepository.findById(fileId);
		if (codeMaintenanceFile.isPresent()) {
			CodeMaintenanceFile file = codeMaintenanceFile.get();

			String filePath = file.getFilePath();
			System.out.println("<<>>>>>>>>>inprocesspath" + filePath);
			s3Service.deleteFileInS3Bucket(filePath);
			String replacedPath = resetPathToUploadFolder(filePath, file.getCodeStandard());
			System.out.println("######################upload path:" + filePath);
			file.setFilePath(replacedPath);
			file.setProcessedState("Uploaded");
			file.setCurrentStatus("File Uploaded Successfully");
			file.setNextState("Process");

			try {
				deleteByFileId(fileId);
			} catch (Exception e) {
				e.printStackTrace();
			}

			codeMaintenanceLoggerService.saveCodeMaintenanceLog(fileId, "File Reset Successfully",
					"File Reset Successfully", userId);
			codeMaintenanceFileRepository.save(file);
			return new CustomResponse("File reset Successfully with Key:" + filePath, "", HttpStatus.OK);

		}
		return new CustomResponse("File reset Failed", "", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public String resetPathToUploadFolder(String inprocessPath, String codeStandard) {

		// String inputString =
		// "code-maintenance-source-files/inprocess/icd/20230831/icd102023.zip";
		String datePattern = "/(\\d{8})/";
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(datePattern);
		java.util.regex.Matcher matcher = pattern.matcher(inprocessPath);

		if (matcher.find()) {
			String date = matcher.group(1);
			String updatedFilePath = inprocessPath.replaceFirst("/inprocess/" + codeStandard + "/" + date + "/",
					"/upload/" + codeStandard + "/");
			System.out.println("<<<<<<<<<<>>>>>>>>>>updatedfilepath:" + updatedFilePath);

			return updatedFilePath;

		} else {
			// System.out.println("Date not found in the input string.");
			return "Failed";
		}
	}

	/*
	 * public void deleteById(int fileId) { // TODO Auto-generated method stub
	 * codeChangeCountsRepository.deleteByFileId(fileId); }
	 */
	public void deleteByFileId(Integer fileId) {
		// TODO Auto-generated method stub
		codeChangeCountsRepository.deleteByFileId(fileId);
	}

	

}