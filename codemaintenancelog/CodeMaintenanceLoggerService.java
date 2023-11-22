package com.io.codesystem.codemaintenancelog;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CodeMaintenanceLoggerService {

	@Autowired
	CodeMaintenanceLoggerRepository codeMaintenanceLoggerRepository;

	public CodeMaintenanceLogger saveCodeMaintenanceLog(int fileId, String eventType, String eventDesc, int userId) {
		log.info("===testing Starts:: ");
		CodeMaintenanceLogger codeMaintenanceLogger = new CodeMaintenanceLogger();
		codeMaintenanceLogger.setEventType(eventType);
		codeMaintenanceLogger.setEventDesc(eventDesc);
		codeMaintenanceLogger.setFileId(fileId);
		codeMaintenanceLogger.setUserId(userId);
		codeMaintenanceLogger.setInsertedDate(Timestamp.valueOf(LocalDateTime.now()));

		log.info("===testing Ends:: " + codeMaintenanceLogger);

		return codeMaintenanceLoggerRepository.save(codeMaintenanceLogger);
	}
}
