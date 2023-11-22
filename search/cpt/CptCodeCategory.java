package com.io.codesystem.search.cpt;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class CptCodeCategory {

	@Id
	private int id;
	private String code;
	private String shortDesc;
	private String mediumDesc;
	private String longDesc;

	private String versionState;
	// private Integer isHcpcs;
	/*
	 * private Integer refId; private String retired; private Integer originalRefId;
	 * private Integer createdBy; private Date createdDate; private Integer
	 * modifiedBy; private Date modifiedDate; private Date retiredOn; private String
	 * retiredReason;
	 */
	private String cptMajorCategory;
	private String cptMinorCategory;

}
