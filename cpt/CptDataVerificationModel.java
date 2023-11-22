package com.io.codesystem.cpt;

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
@Table(name = "cpt_data_verification")
public class CptDataVerificationModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public int id;

	@Column(name = "code")
	public String code;

	@Column(name = "short_desc")
	public String shortDesc;

	@Column(name = "medium_desc")
	public String mediumDesc;

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