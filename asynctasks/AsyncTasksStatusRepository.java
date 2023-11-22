package com.io.codesystem.asynctasks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AsyncTasksStatusRepository extends JpaRepository<AsyncTasksStatusModel, Integer> {

	@Query("SELECT u FROM AsyncTasksStatusModel u WHERE u.fileId=:fileId and u.taskType=:taskType")
	public AsyncTasksStatusModel getByFileIdAndTaskType(int fileId, String taskType);

}
