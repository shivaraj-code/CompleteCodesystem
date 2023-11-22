package com.io.codesystem.codemaintenancefile;

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
@Table(name = "coding_standard_files")
public class CodeMaintenanceFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public int id;

	@Column(name = "code_standard")
	public String codeStandard;

	@Column(name = "file_name")
	public String fileName;

	@Column(name = "file_path")
	public String filePath;

	@Column(name = "processed_state")
	public String processedState;

	@Column(name = "next_state")
	public String nextState;

	@Column(name = "current_status")
	public String currentStatus;

	@Column(name = "status")
	public String status;

	@Column(name = "release_version")
	public String releaseVersion;

	@Column(name = "release_date")
	public Date releaseDate;

	@Column(name = "comments")
	public String comments;

	@Column(name = "user_id")
	public Integer userId;

	@Column(name = "source")
	public String source;

	@Column(name = "processed_date")
	public Date processedDate;

	@Column(name = "modified_user_id")
	public Integer modifiedUserId;

	@Column(name = "modified_date")
	public Timestamp modifiedDate;

	@Column(name = "active")
	public Integer active;

	@Column(name = "effective_from")
	public String effectiveFrom;

	@Column(name = "effective_to")
	public String effectiveTo;

}
