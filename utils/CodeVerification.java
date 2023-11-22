package com.io.codesystem.utils;

import java.util.List;

public class CodeVerification {

	List<String> acceptedCodes;
	List<String> rejectedCodes;

	List<AcceptedCodesdto> acceptedAllergiesCodes;
	List<RejectedCodesdto> rejectedAllergiesCodes;

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

	public List<AcceptedCodesdto> getAcceptedAllergiesCodes() {
		return acceptedAllergiesCodes;
	}

	public void setAcceptedAllergiesCodes(List<AcceptedCodesdto> acceptedAllergiesCodes) {
		this.acceptedAllergiesCodes = acceptedAllergiesCodes;
	}

	public List<RejectedCodesdto> getRejectedAllergiesCodes() {
		return rejectedAllergiesCodes;
	}

	public void setRejectedAllergiesCodes(List<RejectedCodesdto> rejectedAllergiesCodes) {
		this.rejectedAllergiesCodes = rejectedAllergiesCodes;
	}

}