package com.io.codesystem.cpt;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "cpt_standard_versions")
public class CptCodeStandardModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public int id;

	@Column(name = "code")
	public String code;

	@Column(name = "short_desc")
	public String shortDesc;

	@Column(name = "medium_desc")
	public String mediumDesc;

	@Column(name = "long_desc")
	public String longDesc;

	
	/*
	 * @Column(name = "effective_from") public Date effectiveFrom;
	 * 
	 * @Column(name = "effective_to") public Date effectiveTo;
	 */

	public CptCodeStandardModel(String code, String shortDesc, String mediumDesc, String longDesc) {
		super();
		this.code = code;
		this.shortDesc = shortDesc;
		this.mediumDesc = mediumDesc;
		this.longDesc = longDesc;
	}
	public CptCodeStandardModel() {
		
	}	

}