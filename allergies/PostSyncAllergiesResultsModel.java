package com.io.codesystem.allergies;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "allergies_new")
public class PostSyncAllergiesResultsModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)

	@Column(name = "ai_id")
	private int aiId;

	@Column(name = "id")
	private Integer id;

	@Column(name = "dam_concept_id")
	private String damConceptId;

	@Column(name = "dam_concept_id_desc")
	private String damConceptIdDesc;

	@Column(name = "dam_concept_id_type")
	private Integer damConceptIdType;

	@Column(name = "dam_alrgn_grp_desc")
	private String damAlrgnGrpDesc;

	@Column(name = "allergy_desc")
	private String allergyDesc;

	@Column(name = "snomed_code")
	private String snomedCode;

	@Column(name = "snomed_concept")
	private String snomedConcept;

	@Column(name = "file_id")
	private Integer fileId;

	@Column(name = "data_source")
	private String dataSource;

	@Column(name = "version_state")
	private String versionState;

	@Column(name = "status")
	private String status;

	@Column(name = "created_date")
	public Date createdDate;

	@Column(name = "created_by")
	public String createdBy;

	@Column(name = "modified_date")
	public Date ModifiedDate;

	@Column(name = "modified_by")
	public String ModifiedBy;

	@Column(name = "ref_id")
	private Integer refId;

	@Column(name = "original_ref_id")
	private Integer originalRefId;

	// @Column(name = "original_created_date")
	// private Date originalCreatedDate;

	@Column(name = "sync_status")
	private String syncStatus;

}