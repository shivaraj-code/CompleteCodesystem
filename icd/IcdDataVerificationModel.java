package com.io.codesystem.icd;

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
@Table(name = "icd10_data_verification")
public class IcdDataVerificationModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public int id;

	@Column(name = "icd_order")
	public String icdOrder;

	@Column(name = "icd_code")
	public String icd10code;

	@Column(name = "icd_id")
	public Integer icd10id;

	@Column(name = "type")
	public Character type;

	@Column(name = "short_desc")
	public String shortDesc;

	@Column(name = "medium_desc")
	public String medDesc;

	@Column(name = "long_desc")
	public String longDesc;

	@Column(name = "file_id")
	public Integer fileId;

	@Column(name = "inserted_date")
	public Timestamp insertedDate;

	@Column(name = "status")
	public String status;

	@Column(name = "verification_state")
	public String verificationState;

}
