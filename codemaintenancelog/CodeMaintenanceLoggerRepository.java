package com.io.codesystem.codemaintenancelog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeMaintenanceLoggerRepository extends JpaRepository<CodeMaintenanceLogger, Integer> {

}