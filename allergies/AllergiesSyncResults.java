package com.io.codesystem.allergies;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class AllergiesSyncResults {

	@Id
	public Integer id;
	private int added_records;
	private int updated_records;
	private int deleted_records;
	private String status;

}
