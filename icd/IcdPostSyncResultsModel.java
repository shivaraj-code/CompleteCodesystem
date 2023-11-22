package com.io.codesystem.icd;

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
@Table(name = "icdnew")
public class IcdPostSyncResultsModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@Column(name = "icd_id")
	private Integer icd10id;

	@Column(name = "icd_code")
	private String icd10code;

	@Column(name = "icd_order")
	private String icdOrder;

	@Column(name = "type")
	private Character type;

	@Column(name = "short_desc")
	private String shortDesc;

	@Column(name = "medium_desc")
	private String medDesc;

	@Column(name = "long_desc")
	private String longDesc;

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

	@Column(name = "file_id")
	private Integer fileId;

	@Column(name = "sync_status")
	private String syncStatus;

}
