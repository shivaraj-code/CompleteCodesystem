package com.io.codesystem.pharmacy;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;



@Entity
@Table(name = "pharmacy_data_verification")
public class PharmacyDataVerificationModel {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ai_id")
	private int aid;
	@Column(name = "id")
	private int id;
	@Column(name = "ncpdp_id")
	private String ncpdpid;
	@Column(name = "store_number")
	private String number;
	@Column(name = "reference_number_alt1")
	private String referenceNumberAlt1;
	@Column(name = "reference_number_alt1qualifier")
	private String referenceNumberAlt1Qualifier;
	@Column(name = "store_name")
	private String name;
	@Column(name = "address_line1")
	private String address1;
	@Column(name = "address_line2")
	private String address2;
	@Column(name = "city")
	private String city;
	@Column(name = "state")
	private String state;
	@Column(name = "zip")
	private String zip;
	@Column(name = "phone_primary")
	private String phonePrimary;
	@Column(name = "fax")
	private String fax;
	@Column(name = "email")
	private String email;
	@Column(name = "phone_alt1")
	private String phoneAlt1;
	@Column(name = "phone_alt1_qualifier")
	private String phoneAlt1Qualifier;
	@Column(name = "phone_alt2")
	private String phoneAlt2;
	@Column(name = "phone_alt2_qualifier")
	private String phoneAlt2Qualifier;
	@Column(name = "phone_alt3")
	private String phoneAlt3;
	@Column(name = "phone_alt3_qualifier")
	private String phoneAlt3Qualifier;
	@Column(name = "phone_alt4")
	private String phoneAlt4;
	@Column(name = "phone_alt4_qualifier")
	private String phoneAlt4Qualifier;
	@Column(name = "phone_alt5")
	private String phoneAlt5;
	@Column(name = "phone_alt5_qualifier")
	private String phoneAlt5Qualifier;
	@Column(name = "active_start_time")
	private String activeStartTime;
	@Column(name = "active_end_time")
	private String activeEndTime;
	@Column(name = "service_level")
	private String serviceLevel;
	@Column(name = "partner_account")
	private String partnerAccount;
	@Column(name="last_modified_date")
	private String lastModifiedDate;
	@Column(name = "twenty_four_hour_flag")
	private String twentyFourHourFlag;
	@Column(name = "cross_street")
	private String crossStreet;
	@Column(name = "record_change")
	private String recordChange;
	@Column(name = "old_service_level")
	private String oldServiceLevel;
	@Column(name = "text_service_level")
	private String textServiceLevel;
	@Column(name = "text_service_level_change")
	private String textServiceLevelChange;
	@Column(name = "version")
	private String version;
	@Column(name = "npi")
	private String npi;
	@Column(name = "is_deleted")
	private String isDeleted;
	@Column(name = "specialty_type1")
	private String specialtyType1;
	@Column(name = "specialty_type2")
	private String specialtyType2;
	@Column(name = "specialty_type3")
	private String specialtyType3;
	@Column(name = "specialty_type4")
	private String specialtyType4;
	@Column(name = "type")
	private String type;
	@Column(name = "longitude")
	private String longitude;
	@Column(name = "latitude")
	private String latitude;
	@Column(name = "location")
	private String location;

	@Column(name="file_id")
   	public Integer fileId;
    
	@Column(name="inserted_date")
   	public Timestamp insertedDate;
	
	@Column(name="status")
   	public String status;
	
	@Column(name="verification_state")
	public String verificationState;
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public int getAid() {
		return aid;
	}
	public void setAid(int aid) {
		this.aid = aid;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getNcpdpid() {
		return ncpdpid;
	}
	public void setNcpdpid(String ncpdpid) {
		this.ncpdpid = ncpdpid;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getReferenceNumberAlt1() {
		return referenceNumberAlt1;
	}
	public void setReferenceNumberAlt1(String referenceNumberAlt1) {
		this.referenceNumberAlt1 = referenceNumberAlt1;
	}
	public String getReferenceNumberAlt1Qualifier() {
		return referenceNumberAlt1Qualifier;
	}
	public void setReferenceNumberAlt1Qualifier(String referenceNumberAlt1Qualifier) {
		this.referenceNumberAlt1Qualifier = referenceNumberAlt1Qualifier;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress1() {
		return address1;
	}
	public void setAddress1(String address1) {
		this.address1 = address1;
	}
	public String getAddress2() {
		return address2;
	}
	public void setAddress2(String address2) {
		this.address2 = address2;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
	public String getPhonePrimary() {
		return phonePrimary;
	}
	public void setPhonePrimary(String phonePrimary) {
		this.phonePrimary = phonePrimary;
	}
	public String getFax() {
		return fax;
	}
	public void setFax(String fax) {
		this.fax = fax;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhoneAlt1() {
		return phoneAlt1;
	}
	public void setPhoneAlt1(String phoneAlt1) {
		this.phoneAlt1 = phoneAlt1;
	}
	public String getPhoneAlt1Qualifier() {
		return phoneAlt1Qualifier;
	}
	public void setPhoneAlt1Qualifier(String phoneAlt1Qualifier) {
		this.phoneAlt1Qualifier = phoneAlt1Qualifier;
	}
	public String getPhoneAlt2() {
		return phoneAlt2;
	}
	public void setPhoneAlt2(String phoneAlt2) {
		this.phoneAlt2 = phoneAlt2;
	}
	public String getPhoneAlt2Qualifier() {
		return phoneAlt2Qualifier;
	}
	public void setPhoneAlt2Qualifier(String phoneAlt2Qualifier) {
		this.phoneAlt2Qualifier = phoneAlt2Qualifier;
	}
	public String getPhoneAlt3() {
		return phoneAlt3;
	}
	public void setPhoneAlt3(String phoneAlt3) {
		this.phoneAlt3 = phoneAlt3;
	}
	public String getPhoneAlt3Qualifier() {
		return phoneAlt3Qualifier;
	}
	public void setPhoneAlt3Qualifier(String phoneAlt3Qualifier) {
		this.phoneAlt3Qualifier = phoneAlt3Qualifier;
	}
	public String getPhoneAlt4() {
		return phoneAlt4;
	}
	public void setPhoneAlt4(String phoneAlt4) {
		this.phoneAlt4 = phoneAlt4;
	}
	public String getPhoneAlt4Qualifier() {
		return phoneAlt4Qualifier;
	}
	public void setPhoneAlt4Qualifier(String phoneAlt4Qualifier) {
		this.phoneAlt4Qualifier = phoneAlt4Qualifier;
	}
	public String getPhoneAlt5() {
		return phoneAlt5;
	}
	public void setPhoneAlt5(String phoneAlt5) {
		this.phoneAlt5 = phoneAlt5;
	}
	public String getPhoneAlt5Qualifier() {
		return phoneAlt5Qualifier;
	}
	public void setPhoneAlt5Qualifier(String phoneAlt5Qualifier) {
		this.phoneAlt5Qualifier = phoneAlt5Qualifier;
	}
	public String getActiveStartTime() {
		return activeStartTime;
	}
	public void setActiveStartTime(String activeStartTime) {
		this.activeStartTime = activeStartTime;
	}
	public String getActiveEndTime() {
		return activeEndTime;
	}
	public void setActiveEndTime(String activeEndTime) {
		this.activeEndTime = activeEndTime;
	}
	public String getServiceLevel() {
		return serviceLevel;
	}
	public void setServiceLevel(String serviceLevel) {
		this.serviceLevel = serviceLevel;
	}
	public String getPartnerAccount() {
		return partnerAccount;
	}
	public void setPartnerAccount(String partnerAccount) {
		this.partnerAccount = partnerAccount;
	}
	public String getTwentyFourHourFlag() {
		return twentyFourHourFlag;
	}
	public void setTwentyFourHourFlag(String twentyFourHourFlag) {
		this.twentyFourHourFlag = twentyFourHourFlag;
	}
	public String getCrossStreet() {
		return crossStreet;
	}
	public void setCrossStreet(String crossStreet) {
		this.crossStreet = crossStreet;
	}
	public String getRecordChange() {
		return recordChange;
	}
	public void setRecordChange(String recordChange) {
		this.recordChange = recordChange;
	}
	public String getOldServiceLevel() {
		return oldServiceLevel;
	}
	public void setOldServiceLevel(String oldServiceLevel) {
		this.oldServiceLevel = oldServiceLevel;
	}
	public String getTextServiceLevel() {
		return textServiceLevel;
	}
	public void setTextServiceLevel(String textServiceLevel) {
		this.textServiceLevel = textServiceLevel;
	}
	public String getTextServiceLevelChange() {
		return textServiceLevelChange;
	}
	public void setTextServiceLevelChange(String textServiceLevelChange) {
		this.textServiceLevelChange = textServiceLevelChange;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getNpi() {
		return npi;
	}
	public void setNpi(String npi) {
		this.npi = npi;
	}
	public String getIsDeleted() {
		return isDeleted;
	}
	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}
	public String getSpecialtyType1() {
		return specialtyType1;
	}
	public void setSpecialtyType1(String specialtyType1) {
		this.specialtyType1 = specialtyType1;
	}
	public String getSpecialtyType2() {
		return specialtyType2;
	}
	public void setSpecialtyType2(String specialtyType2) {
		this.specialtyType2 = specialtyType2;
	}
	public String getSpecialtyType3() {
		return specialtyType3;
	}
	public void setSpecialtyType3(String specialtyType3) {
		this.specialtyType3 = specialtyType3;
	}
	public String getSpecialtyType4() {
		return specialtyType4;
	}
	public void setSpecialtyType4(String specialtyType4) {
		this.specialtyType4 = specialtyType4;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	
	public void setVerificationState(String verificationState) {
		this.verificationState = verificationState;
	}
	public String getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	public String getVerificationState() {
		return verificationState;
	}
	
	public Integer getFileId() {
		return fileId;
	}
	public void setFileId(Integer fileId) {
		this.fileId = fileId;
	}
	public Timestamp getInsertedDate() {
		return insertedDate;
	}
	public void setInsertedDate(Timestamp insertedDate) {
		this.insertedDate = insertedDate;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public PharmacyDataVerificationModel(int aid, int id, String ncpdpid, String number, String referenceNumberAlt1,
			String referenceNumberAlt1Qualifier, String name, String address1, String address2, String city,
			String state, String zip, String phonePrimary, String fax, String email, String phoneAlt1,
			String phoneAlt1Qualifier, String phoneAlt2, String phoneAlt2Qualifier, String phoneAlt3,
			String phoneAlt3Qualifier, String phoneAlt4, String phoneAlt4Qualifier, String phoneAlt5,
			String phoneAlt5Qualifier, String activeStartTime, String activeEndTime, String serviceLevel,
			String partnerAccount, String lastModifiedDate, String twentyFourHourFlag, String crossStreet,
			String recordChange, String oldServiceLevel, String textServiceLevel, String textServiceLevelChange,
			String version, String npi, String isDeleted, String specialtyType1, String specialtyType2,
			String specialtyType3, String specialtyType4, String type, String longitude, String latitude,
			String location, Integer fileId, Timestamp insertedDate, String status, String verificationState) {
		super();
		this.aid = aid;
		this.id = id;
		this.ncpdpid = ncpdpid;
		this.number = number;
		this.referenceNumberAlt1 = referenceNumberAlt1;
		this.referenceNumberAlt1Qualifier = referenceNumberAlt1Qualifier;
		this.name = name;
		this.address1 = address1;
		this.address2 = address2;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.phonePrimary = phonePrimary;
		this.fax = fax;
		this.email = email;
		this.phoneAlt1 = phoneAlt1;
		this.phoneAlt1Qualifier = phoneAlt1Qualifier;
		this.phoneAlt2 = phoneAlt2;
		this.phoneAlt2Qualifier = phoneAlt2Qualifier;
		this.phoneAlt3 = phoneAlt3;
		this.phoneAlt3Qualifier = phoneAlt3Qualifier;
		this.phoneAlt4 = phoneAlt4;
		this.phoneAlt4Qualifier = phoneAlt4Qualifier;
		this.phoneAlt5 = phoneAlt5;
		this.phoneAlt5Qualifier = phoneAlt5Qualifier;
		this.activeStartTime = activeStartTime;
		this.activeEndTime = activeEndTime;
		this.serviceLevel = serviceLevel;
		this.partnerAccount = partnerAccount;
		this.lastModifiedDate = lastModifiedDate;
		this.twentyFourHourFlag = twentyFourHourFlag;
		this.crossStreet = crossStreet;
		this.recordChange = recordChange;
		this.oldServiceLevel = oldServiceLevel;
		this.textServiceLevel = textServiceLevel;
		this.textServiceLevelChange = textServiceLevelChange;
		this.version = version;
		this.npi = npi;
		this.isDeleted = isDeleted;
		this.specialtyType1 = specialtyType1;
		this.specialtyType2 = specialtyType2;
		this.specialtyType3 = specialtyType3;
		this.specialtyType4 = specialtyType4;
		this.type = type;
		this.longitude = longitude;
		this.latitude = latitude;
		this.location = location;
		this.fileId = fileId;
		this.insertedDate = insertedDate;
		this.status = status;
		this.verificationState = verificationState;
	}
	
	public PharmacyDataVerificationModel() {
		
	}
	
}