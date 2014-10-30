package com.risevision.storage.entities;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Andrew on 10/30/2014.
 */
@Entity
@Cache
public class ShareFolder implements Serializable {
    @Id public Long id;
    @Index public String originCompanyId;
    @Index public String folderName;
    public List<SharePermission> permissions;

    public ShareFolder(){

    }

    public ShareFolder(String originCompanyId, String folderName, List<SharePermission> permissions){
        this.originCompanyId = originCompanyId;
        this.folderName = folderName;
        this.permissions = permissions;
    }
}
