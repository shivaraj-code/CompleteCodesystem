package com.io.codesystem.search.icd;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Where;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexingDependency;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ObjectPath;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.PropertyValue;

import lombok.Data;

@Entity
@Table(name="icdnew")
@Where(clause = "(type='V' OR type='H') AND version_state='Validated' AND status='Y'")
@Indexed
@Data
public class IcdCode {

  @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
   
    @Column(name = "icd_id")
//    @KeywordField
    private Integer icd10id;
    
    @FullTextField(analyzer = "edgengram",searchAnalyzer = "stdquery")
    @Column(name = "icd_code")
    private String icd10code;
    
    @Column(name = "icd_order")
    private String icdOrder;
    
    @Column(name = "type")
    //@KeywordField
    private String type;
   
    @FullTextField
    @Column(name = "short_desc")
    private String shortDesc;
   
    @FullTextField
    @Column(name = "medium_desc")
    private String medDesc;
   
    @FullTextField
    @Column(name = "long_desc")
    private String longDesc;
   
//    @Column(name = "effective_from")
//    private Date effectiveFrom;
//   
//    @Column(name = "effective_to")
//    private Date effectiveTo;
//   
    @Column(name = "file_id")
    private Integer fileId;
   
    @Column(name = "data_source")
    private String dataSource;
    
    @Column(name = "version_state")
    private String versionState;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "created_by")
    private Integer createdBy;
    
    @Column(name = "modified_by")
    private Integer modifiedBy;
    
    @Column(name = "created_date")
    private Timestamp createdDate;
    
    @Column(name = "modified_date")
    private Timestamp modifiedDate;
    
    @Column(name = "ref_id")
    private Integer refId;
    
    @Column(name = "original_ref_id")
    private Integer originalRefId;
    
    @Column(name = "sync_status")
    private String syncStatus;
    
}    
    
