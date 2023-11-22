package com.io.codesystem.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.io.codesystem.CodeMaintenanceService;
import com.io.codesystem.allergies.AllergiesCodeMaintenanceService;
import com.io.codesystem.codemaintenancefile.CodeMaintenanceFile;
import com.io.codesystem.codemaintenancefile.CodeMaintenanceFileService;
import com.io.codesystem.cpt.CptCodeMaintenanceService;
import com.io.codesystem.icd.IcdCodeMaintenanceService;
import com.io.codesystem.medicine.MedicineMaintenanceService;
import com.io.codesystem.pharmacy.PharmacyCodeMaintenanceService;

@Configuration
public class CodeMaintenanceServiceConfig {

	@Autowired
	AllergiesCodeMaintenanceService allergiesCodeMaintenanceService;

	@Autowired
	IcdCodeMaintenanceService icdCodeMaintenanceService;

	@Autowired
	MedicineMaintenanceService medicineMaintenanceService;

	@Autowired
	PharmacyCodeMaintenanceService pharmacyCodeMaintenanceService;

	@Autowired
	CptCodeMaintenanceService cptCodeMaintenanceService;

	@Autowired
	CodeMaintenanceFileService codeMaintenanceFileService;

	public CodeMaintenanceService getCodeMaintenanceServiceByCodeType(String codeType, int fileId) {

		if (codeType == null) {
			CodeMaintenanceFile file = codeMaintenanceFileService.getCodeMaintenanceFileById(fileId);
			codeType = file.getCodeStandard();
		}

		switch (codeType) {

		case "icd":
			return icdCodeMaintenanceService;

		case "medicine":
			return medicineMaintenanceService;

		case "cpt":
			return cptCodeMaintenanceService;

		case "pharmacy":
			return pharmacyCodeMaintenanceService;

		case "allergies":
			return allergiesCodeMaintenanceService;

		default:
			return null;

		}

	}

}