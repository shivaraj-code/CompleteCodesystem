package com.io.codesystem;

import java.sql.Date;

import org.springframework.web.multipart.MultipartFile;

import com.io.codesystem.utils.CodeVerification;
import com.io.codesystem.utils.CustomResponse;

public abstract class CodeMaintenanceService {

	// This method uploads a file to S3 bucket and returns the path of the file in
	// the bucket.
	protected abstract CustomResponse uploadFileToS3(String codeType, String releaseVersion, Date releaseDate,
			MultipartFile releaseFile, int userId, String effectiveFrom, String effectiveTo);

	// This method extracts files from the uploaded zip.
	protected abstract void processData(int fileId, int userId);

	// This method parses a file and creates a list of entities (Your Entity class)
	// to be stored in a temporary table.
	protected abstract CustomResponse markAsVerified(int fileId, String verifiedType, int userId);

	// This method saves the entities to the temporary table.
	protected abstract void syncVerifiedData(int fileId, int userId);

	protected abstract CustomResponse saveCodeVerificationLogDetails(CodeVerification codes, String codeset, int fileId,
			int userId, String notes);


}
