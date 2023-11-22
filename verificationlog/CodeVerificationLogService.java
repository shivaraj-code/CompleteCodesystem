package com.io.codesystem.verificationlog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CodeVerificationLogService {

	@Autowired
	CodeVerificationLogRepository codeVerificationLogRepository;

}
