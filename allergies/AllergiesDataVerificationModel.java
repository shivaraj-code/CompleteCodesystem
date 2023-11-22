package com.io.codesystem.allergies;

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
@Table(name = "allergies_data_verification")
public class AllergiesDataVerificationModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)

	@Column(name = "ai_id")
	private int aiId;

	@Column(name = "id")
	private int id;

	@Column(name = "dam_concept_id")
	private String damConceptId;

	@Column(name = "dam_concept_id_desc")
	private String damConceptIdDesc;

	@Column(name = "dam_concept_id_type")
	private Integer damConceptIdType;

	@Column(name = "dam_alrgn_grp_desc")
	private String damAlrgnGrpDesc;

	@Column(name = "snomed_code")
	private String snomedCode;

	@Column(name = "snomed_concept")
	private String snomedConcept;

	@Column(name = "file_id")
	public Integer fileId;

	@Column(name = "inserted_date")
	public Timestamp insertedDate;

	@Column(name = "status")
	public String status;

	@Column(name = "verification_state")
	public String verificationState;

}
