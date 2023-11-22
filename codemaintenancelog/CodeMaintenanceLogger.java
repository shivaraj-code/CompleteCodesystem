package com.io.codesystem.codemaintenancelog;

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
@Table(name = "coding_maintenance_log")
public class CodeMaintenanceLogger {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@Column(name = "file_id")
	private int fileId;

	@Column(name = "event_type")
	private String eventType;

	@Column(name = "event_desc")
	private String eventDesc;

	@Column(name = "user_id")
	private int userId;

	@Column(name = "inserted_date")
	public Timestamp insertedDate;
}
