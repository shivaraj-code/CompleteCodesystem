package com.io.codesystem.utils;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomResponse {

	private String status;
	private String comments;
	private HttpStatus statusCode;
}
