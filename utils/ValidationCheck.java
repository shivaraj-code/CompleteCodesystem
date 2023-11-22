package com.io.codesystem.utils;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationCheck {
	@Id
	private Integer id;
	private String message;

	
}
