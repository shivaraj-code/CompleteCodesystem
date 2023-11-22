package com.io.codesystem.search.icd;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Data;
@Entity
@Data
public class IcdCodeTree {
    	
    	    @Id
    		public Integer id;
    		public Integer icdId;
    	    public String parentId;
    		public String icdCode;
    		public String longDesc;
    		public Character type;
    		public Integer level;
    		
	}