package com.risevision.storage.entities;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;

/**
 * Created by Andrew on 10/29/2014.
 */
@Entity
@Cache
public class ShareFolderLink implements Serializable{
    @Id public Long id;
    @Index public String originCompanyId;
    @Index public String sharedCompanyId;
    @Index public String folderName;
    public boolean view;
    public boolean edit;
    public boolean add;

    public ShareFolderLink() {

    }

    public ShareFolderLink (String originCompanyId, String sharedCompanyId, String folderName, boolean view, boolean edit, boolean add){
        this.originCompanyId = originCompanyId;
        this.sharedCompanyId = sharedCompanyId;
        this.folderName = folderName;
        this.view = view;
        this.edit = edit;
        this.add = add;
    }
}
