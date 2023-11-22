package com.io.codesystem.verificationlog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeVerificationLogRepository extends JpaRepository<CodeVerificationLogModel, Integer> {

}
