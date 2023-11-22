package com.io.codesystem.medicine;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "medicines_data_verification")
public class MedicineDataVerificationModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ai_id")
	private int aiid;

	@Column(name = "id")
	private int id;
	
	@Column(name = "ndc")
	private String ndc;

	@Column(name = "name")
	private String name;

	@Column(name = "dea")
	private int dea;

	@Column(name = "obsdtec")
	private String obsdtec;

	@Column(name = "repack")
	private int repack;

	@Column(name = "is_compounded")
	private String isCompounded;

	@Column(name = "file_id")
	private Integer fileId;

	@Column(name = "inserted_date")
	private Timestamp insertedDate;

	@Column(name = "status")
	private String status;

	@Column(name = "verification_state")
	private String verificationState;


}