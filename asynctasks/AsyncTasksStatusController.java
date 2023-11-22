package com.io.codesystem.asynctasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AsyncTasksStatusController {

	@Autowired
	AsyncTasksStatusService asyncTasksStatusService;

	@GetMapping(path = "/asynctasks/status/{fileId}/{taskType}")
	public ResponseEntity<AsyncTasksStatusModel> getAsyncTasksStatus(@PathVariable Integer fileId,
			@PathVariable String taskType)

	{
		HttpHeaders headers = new HttpHeaders();

		AsyncTasksStatusModel response = asyncTasksStatusService.getAsyncTaskStatus(fileId, taskType);

		return new ResponseEntity<>(response, headers, HttpStatus.OK);
	}

}