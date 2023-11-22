package com.io.codesystem.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class S3Service {

	@Value("${aws.s3.bucket-name}")
	private String bucketName;

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

	private AmazonS3 s3Service = null;

	public S3Service() {
		try {
			s3Service = AmazonS3ClientBuilder.standard().build();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String saveCodeZipFile(String zipFilename, String targetCodeTypeFolderName, InputStream zipFileStream)
			throws IOException {

		String fileExistStatus = isFileExistInS3Bucket(zipFilename, targetCodeTypeFolderName);
		String savedFilePath = "";

		if (fileExistStatus.equalsIgnoreCase("File Not Exists")) {

			String targetFolderPath = rootFolderName + "/" + uploadFolderName + "/" + targetCodeTypeFolderName + "/";
			log.info("====== TargetFolderPath:: " + targetFolderPath);
			ObjectMetadata objMetadata = new ObjectMetadata();
			objMetadata.setContentLength(zipFileStream.available());
			savedFilePath = targetFolderPath + zipFilename;
			log.info("===== Saved FilePath :: " + savedFilePath);
			s3Service.putObject(bucketName, savedFilePath, zipFileStream, objMetadata);
			log.info(MessageFormat.format("File saved in S3 Bucket: Buket Name:{0} and File Name:{1}", bucketName,
					zipFilename));

		} else {
			throw new IOException(fileExistStatus);
		}

		return savedFilePath;
	}

	public List<String> showFilesInS3BucketFolder(String rootFolderName) {

		List<String> list = new LinkedList<>();

		try {
			ListObjectsRequest lor = new ListObjectsRequest().withBucketName(bucketName).withPrefix(rootFolderName);

			ObjectListing objectListing = s3Service.listObjects(lor);
			List<S3ObjectSummary> s3FilesSummary = objectListing.getObjectSummaries();

			for (S3ObjectSummary summary : s3FilesSummary) {

				list.add(summary.getKey().toString());
			}
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
		}
		return list;
	}

	public String isFileExistInS3Bucket(String fileName, String targetCodeStandardFolder) {

		String uploadFolderPath = rootFolderName + "/" + uploadFolderName + "/" + targetCodeStandardFolder + "/"
				+ fileName;
		String inprocessFolderPath = rootFolderName + "/" + inprocessFolderName + "/" + targetCodeStandardFolder + "/"
				+ fileName;
		String processedFolderPath = rootFolderName + "/" + processedFolderName + "/" + targetCodeStandardFolder + "/"
				+ fileName;
		// System.out.println("$$$$$$$$$$$$$$$$$$$$"+uploadFolderPath
		// +":"+inprocessFolderPath +":"+processedFolderPath);
		if (s3Service.doesObjectExist(bucketName, uploadFolderPath)) {
			log.error(MessageFormat.format("Uploading File already exists in path {0}", uploadFolderPath));
			return "File uploading failed due to Same File already exists in S3 Bucket, Path:\"" + uploadFolderPath
					+ "\". Please delete existing file, If you want to upload new version of same file";
		}
		if (s3Service.doesObjectExist(bucketName, inprocessFolderPath)) {
			log.error(MessageFormat.format("Uploading File already exists in path {0}", inprocessFolderPath));
			return "File uploading failed due to Same File already exists in S3 Bucket Path:\"" + inprocessFolderPath
					+ "\". You cant upload same file, if it is already in In-Process Folder";
		}
		if (s3Service.doesObjectExist(bucketName, processedFolderPath)) {
			log.error(MessageFormat.format("Uploading File already exists in path {0}", processedFolderPath));
			return "File uploading failed due to Same File already exists in S3 Bucket Path:\"" + processedFolderPath
					+ "\". You cant upload same file, if it is already in Processed Folder";
		}
		return "File Not Exists";
	}

	public CustomResponse deleteFileInS3Bucket(String filePath) {

		try {
			s3Service.deleteObject(new DeleteObjectRequest(bucketName, filePath));

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			log.error(e.getLocalizedMessage());
			return new CustomResponse("File Delete Failed with Key:" + filePath, e.getLocalizedMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new CustomResponse("File Deleted Successfully with Key:" + filePath, "", HttpStatus.OK);
	}

	public InputStream getS3FileStream(String targetFilePath) {

		S3Object object = s3Service.getObject(new GetObjectRequest(bucketName, targetFilePath));
		byte[] byteArray = null;
		try {
			byteArray = object.getObjectContent().readAllBytes();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ByteArrayInputStream(byteArray);
	}

//	    public CustomResponse deleteAllFilesInS3Bucket(String bucketName) {
//	        try {
//	            AmazonS3 s3Client = ...; // Instantiate your AmazonS3 client here
//
//	            List<S3ObjectSummary> objectSummaries = s3Client.listObjects(bucketName).getObjectSummaries();
//	            if (!objectSummaries.isEmpty()) {
//	                DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(bucketName);
//	                List<String> keysToDelete = new ArrayList<>();
//
//	                for (S3ObjectSummary objectSummary : objectSummaries) {
//	                    keysToDelete.add(objectSummary.getKey());
//	                }
//
//	                deleteRequest.setKeys(keysToDelete);
//	                s3Client.deleteObjects(deleteRequest);
//	            }
//	        } catch (Exception e) {
//	            System.out.println(e.getLocalizedMessage());
//	            log.error(e.getLocalizedMessage());
//	            return new CustomResponse("Files Delete Failed in Bucket: " + bucketName, e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//	        }
//
//	        return new CustomResponse("All Files Deleted Successfully in Bucket: " + bucketName, "", HttpStatus.OK);
//	    }
//		
	public void moveFile(String sourcekey, String destinationKey, boolean sourceDelete) {

		System.out.println("SourceKey::" + sourcekey);
		System.out.println("DestinationKey::" + destinationKey);
		s3Service.copyObject(new CopyObjectRequest(bucketName, sourcekey, bucketName, destinationKey));
		if (sourceDelete) {
			System.out.println("Deleting with DestinationKey:" + destinationKey);
			s3Service.deleteObject(bucketName, sourcekey);
		}
	}

	public InputStream downloadFile(String fileKey) throws IOException {
		S3Object s3Object = s3Service.getObject(bucketName, fileKey);
		S3ObjectInputStream inputStream = s3Object.getObjectContent();
		return inputStream;
	}


}