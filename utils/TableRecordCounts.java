package com.io.codesystem.utils;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class TableRecordCounts {

	@Id
	public int id;
	public String tableName;
	public int recordsCount;
}