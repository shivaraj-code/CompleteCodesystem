package com.io.codesystem.search.cpt;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Where;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

import lombok.Data;

@Entity
@Table(name = "cptnew_ctg")
@Where(clause = "version_state='Validated' AND status='Y'")
@Indexed
@Data
public class CptCode {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@FullTextField(analyzer = "edgengram", searchAnalyzer = "stdquery")
	@Column(name = "code")
	private String code;

	@FullTextField
	@Column(name = "short_desc")
	private String shortDesc;

	@FullTextField
	@Column(name = "medium_desc")
	private String mediumDesc;

	@FullTextField
	@Column(name = "long_desc")
	private String longDesc;

	@Column(name = "file_id")
	private Integer fileId;

	@Column(name = "data_source")
	private String dataSource;

	@Column(name = "version_state")
	private String versionState;

	@Column(name = "status")
	private String status;

	@Column(name = "created_by")
	private Integer createdBy;

	@Column(name = "modified_by")
	private Integer modifiedBy;

	@Column(name = "created_date")
	private Timestamp createdDate;

	@Column(name = "modified_date")
	private Timestamp modifiedDate;

	@Column(name = "ref_id")
	private Integer refId;

	@Column(name = "original_ref_id")
	private Integer originalRefId;

	@Column(name = "sync_status")
	private String syncStatus;

	@Column(name = "cpt_major_category")
	private String cptMajorCategory;

	@Column(name = "cpt_minor_category")
	private String cptMinorCategory;

}
