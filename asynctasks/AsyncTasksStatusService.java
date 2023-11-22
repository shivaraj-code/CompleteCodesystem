package com.io.codesystem.asynctasks;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AsyncTasksStatusService {

	@Autowired
	AsyncTasksStatusRepository asyncTasksStatusRepository;

	public void saveAsyncTaskStatusLog(AsyncTasksStatusModel asyncTaskLog) {
		asyncTasksStatusRepository.save(asyncTaskLog);
	}

	public void saveAsyncTaskStatusLog(int fileId, String taskType, String currentStatusDesc, String completionStatus,
			int completionPercent, int userId) {

		AsyncTasksStatusModel model = new AsyncTasksStatusModel();
		model.setFileId(fileId);
		model.setTaskType(taskType);
		model.setCurrentStatusDesc(currentStatusDesc);
		model.setCompletionStatus(completionStatus);
		model.setCompletionPercent(completionPercent);
		model.setUserId(userId);
		Timestamp insertedDate = Timestamp.valueOf(LocalDateTime.now());
		model.setInsertedDate(insertedDate);

		AsyncTasksStatusModel existingLog = getAsyncTaskStatus(fileId, taskType);

		if (existingLog != null) {
			existingLog.setCurrentStatusDesc(currentStatusDesc);
			existingLog.setCompletionStatus(completionStatus);
			existingLog.setCompletionPercent(completionPercent);
			asyncTasksStatusRepository.save(existingLog);
		} else {
			asyncTasksStatusRepository.save(model);

		}
	}

	public AsyncTasksStatusModel getAsyncTaskStatus(int fileId, String taskType) {

		return asyncTasksStatusRepository.getByFileIdAndTaskType(fileId, taskType);

	}

/*	public void saveProcessAsyncTaskStatusLog(AsyncTasksStatusModel processAsyncTaskLog) {
		asyncTasksStatusRepository.save(processAsyncTaskLog);
	}

	public void saveProcessAsyncTaskStatusLog(int fileId, String taskType, String currentStatusDesc,
			String completionStatus, int completionPercent, int userId) {

		AsyncTasksStatusModel model2 = new AsyncTasksStatusModel();
		model2.setFileId(fileId);
		model2.setTaskType(taskType);
		model2.setCurrentStatusDesc(currentStatusDesc);
		model2.setCompletionStatus(completionStatus);
		model2.setCompletionPercent(completionPercent);
		model2.setUserId(userId);
		Timestamp insertedDate = Timestamp.valueOf(LocalDateTime.now());
		model2.setInsertedDate(insertedDate);
		AsyncTasksStatusModel existingLog2 = getProcessAsyncTaskStatus(fileId, taskType);

		if (existingLog2 != null) {
			existingLog2.setTaskType(taskType);
			existingLog2.setCurrentStatusDesc(currentStatusDesc);
			existingLog2.setCompletionStatus(completionStatus);
			existingLog2.setCompletionPercent(completionPercent);
			asyncTasksStatusRepository.save(existingLog2);
		} else {

			asyncTasksStatusRepository.save(model2);

		}
	}

	public AsyncTasksStatusModel getProcessAsyncTaskStatus(int fileId, String taskType) {
		return asyncTasksStatusRepository.getByFileIdAndTaskType(fileId, taskType);
	}
*/
}
