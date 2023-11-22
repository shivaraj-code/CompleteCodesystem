package com.io.codesystem.search.allergies;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Where;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

@Entity
@Table(name = "allergies_new")
@Where(clause = "version_state='Validated' AND status='Y'")
@Indexed

//@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Allergies {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	
	@Column(name = "dam_concept_id")
	private String damConceptId;

	@FullTextField
	@Column(name = "dam_concept_id_desc")
	private String damConceptIdDesc;

	@Column(name = "dam_concept_id_type")
	private Integer damConceptIdType;

	@FullTextField
	@Column(name = "dam_alrgn_grp_desc")
	private String damAlrgnGrpDesc;

	@FullTextField
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDamConceptId() {
		return damConceptId;
	}

	public void setDamConceptId(String damConceptId) {
		this.damConceptId = damConceptId;
	}

	public String getDamConceptIdDesc() {
		return damConceptIdDesc;
	}

	public void setDamConceptIdDesc(String damConceptIdDesc) {
		this.damConceptIdDesc = damConceptIdDesc;
	}

	public Integer getDamConceptIdType() {
		return damConceptIdType;
	}

	public void setDamConceptIdType(Integer damConceptIdType) {
		this.damConceptIdType = damConceptIdType;
	}

	public String getDamAlrgnGrpDesc() {
		return damAlrgnGrpDesc;
	}

	public void setDamAlrgnGrpDesc(String damAlrgnGrpDesc) {
		this.damAlrgnGrpDesc = damAlrgnGrpDesc;
	}

	public String getAllergyDesc() {
		return allergyDesc;
	}

	public void setAllergyDesc(String allergyDesc) {
		this.allergyDesc = allergyDesc;
	}

	public String getSnomedCode() {
		return snomedCode;
	}

	public void setSnomedCode(String snomedCode) {
		this.snomedCode = snomedCode;
	}

	public String getSnomedConcept() {
		return snomedConcept;
	}

	public void setSnomedConcept(String snomedConcept) {
		this.snomedConcept = snomedConcept;
	}

	public Integer getFileId() {
		return fileId;
	}

	public void setFileId(Integer fileId) {
		this.fileId = fileId;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public String getVersionState() {
		return versionState;
	}

	public void setVersionState(String versionState) {
		this.versionState = versionState;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getModifiedDate() {
		return ModifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		ModifiedDate = modifiedDate;
	}

	public String getModifiedBy() {
		return ModifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		ModifiedBy = modifiedBy;
	}

	public Integer getRefId() {
		return refId;
	}

	public void setRefId(Integer refId) {
		this.refId = refId;
	}

	public Integer getOriginalRefId() {
		return originalRefId;
	}

	public void setOriginalRefId(Integer originalRefId) {
		this.originalRefId = originalRefId;
	}

	public String getSyncStatus() {
		return syncStatus;
	}

	public void setSyncStatus(String syncStatus) {
		this.syncStatus = syncStatus;
	}

	@Override
	public String toString() {
		return "Allergy [id=" + id + ", damConceptId=" + damConceptId + ", damConceptIdDesc=" + damConceptIdDesc
				+ ", damConceptIdType=" + damConceptIdType + ", damAlrgnGrpDesc=" + damAlrgnGrpDesc + ", allergyDesc="
				+ allergyDesc + ", snomedCode=" + snomedCode + ", snomedConcept=" + snomedConcept + ", fileId=" + fileId
				+ ", dataSource=" + dataSource + ", versionState=" + versionState + ", status=" + status
				+ ", createdDate=" + createdDate + ", createdBy=" + createdBy + ", ModifiedDate=" + ModifiedDate
				+ ", ModifiedBy=" + ModifiedBy + ", refId=" + refId + ", originalRefId=" + originalRefId
				+ ", syncStatus=" + syncStatus + "]";
	}

}
