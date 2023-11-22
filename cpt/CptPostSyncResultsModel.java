package com.io.codesystem.cpt;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "cptnew")
public class CptPostSyncResultsModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@Column(name = "code")
	private String code;

	@Column(name = "short_desc")
	private String shortDesc;

	@Column(name = "medium_desc")
	private String mediumDesc;

	//@Column(name = "description")
	//private String description;
	
	@Column(name = "long_desc")
	private String longDesc;

	@Column(name = "file_id")
	private Integer fileId;

	@Column(name = "data_source")
	private String dataSource;

	@Column(name = "version_state")
	private String versionState;

	@Column(name = "status")
	private Character status;

	@Column(name = "created_by")
	private Integer createdBy;

	@Column(name = "modified_by")
	private Integer modifiedBy;

	@Column(name = "modified_date")
	private Date modifiedDate;

	@Column(name = "created_date")
	private Date createdDate;

	@Column(name = "ref_id")
	private Integer refId;

	@Column(name = "original_ref_id")
	private Integer originalRefId;

	@Column(name = "sync_status")
	private String syncStatus;

}