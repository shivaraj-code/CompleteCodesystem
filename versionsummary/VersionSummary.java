package com.io.codesystem.versionsummary;

import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name="version_summary")
public class VersionSummary {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	
	@Column(name = "file_id")
	private int fileId;
	
	@Column(name = "user_id")
	private int userId;
	
	@Column(name = "code_standard")
	private String codeStandard;
	
	@Column(name = "release_date")
	public Date releaseDate;
	
	@Column(name = "previous_version")
	private String previousVersion;
	
	@Column(name = "current_version")
	private String currentVersion;
	
	@Column(name="previous_version_total_records")
	private int previousVersionTotalRecords;
	
	@Column(name="added_records")
	private int addedRecords;
	
	@Column(name="updated_records")
	private int updatedRecords;
	
	@Column(name="deleted_records")
	private int deletedRecords;
	
	@Column(name="current_version_total_records")
	private int currentVersionTotalRecords;
	
	@Column(name="previous_version_processed_date")
	private Timestamp previousVersionProcessedDate;
	
	@Column(name="current_version_processed_date")
	private Timestamp currentVersionProcessedDate;
	

}