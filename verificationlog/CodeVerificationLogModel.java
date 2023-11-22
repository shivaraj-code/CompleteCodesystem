package com.io.codesystem.verificationlog;

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

@Table(name = "code_verification_log")
public class CodeVerificationLogModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public int id;

	@Column(name = "codeset")
	public String codeset;

	@Column(name = "code")
	public String code;

	@Column(name = "file_id")
	public int fileId;

	@Column(name = "user_id")
	public int userId;

	@Column(name = "inserted_date")
	public Timestamp insertedDate;

	@Column(name = "verified_state")
	public String verifiedState;

	@Column(name = "notes")
	public String notes;

}
