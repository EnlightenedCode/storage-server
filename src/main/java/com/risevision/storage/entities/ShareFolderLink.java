package com.risevision.storage.entities;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import java.io.Serializable;

@Entity
@Cache
public class ShareFolderLink implements Serializable{
    @Id public Long id;
    @Index public String originCompanyId;
    @Index public String sharedCompanyId;
    @Index public String folderName;
    public boolean edit;

    public ShareFolderLink() {

    }

    public ShareFolderLink (String originCompanyId, String sharedCompanyId, String folderName, boolean edit){
        this.originCompanyId = originCompanyId;
        this.sharedCompanyId = sharedCompanyId;
        this.folderName = folderName;
        this.edit = edit;
    }
}
