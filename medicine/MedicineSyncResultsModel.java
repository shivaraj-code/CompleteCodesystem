package com.io.codesystem.medicine;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;


@Data
@Entity
public class MedicineSyncResultsModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;
	private int added_records;
	private int updated_records;
	private int deleted_records;
	private String status;
}