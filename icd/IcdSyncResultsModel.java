package com.io.codesystem.icd;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class IcdSyncResultsModel {

	@Id
	public Integer id;
	private int added_records;
	private int updated_records;
	private int deleted_records;
	private String status;
}
