package com.io.codesystem.asynctasks;

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
@Table(name = "coding_asynch_tasks_status")
public class AsyncTasksStatusModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public int id;

	@Column(name = "file_id")
	public Integer fileId;

	@Column(name = "task_type")
	public String taskType;

	@Column(name = "current_status_desc")
	public String currentStatusDesc;

	@Column(name = "completion_status")
	public String completionStatus;

	@Column(name = "completion_percent")
	public Integer completionPercent;

	@Column(name = "user_id")
	private int userId;

	@Column(name = "inserted_date")
	public Timestamp insertedDate;

}
