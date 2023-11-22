package com.io.codesystem.utils;

import java.util.List;

public class CodeVerfication {

	List<String> acceptedCodes;
	List<String> rejectedCodes;

	public List<String> getAcceptedCodes() {
		return acceptedCodes;
	}

	public void setAcceptedCodes(List<String> acceptedCodes) {
		this.acceptedCodes = acceptedCodes;
	}

	public List<String> getRejectedCodes() {
		return rejectedCodes;
	}

	public void setRejectedCodes(List<String> rejectedCodes) {
		this.rejectedCodes = rejectedCodes;
	}

}