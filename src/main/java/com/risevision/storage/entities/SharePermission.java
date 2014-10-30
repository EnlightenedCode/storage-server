package com.risevision.storage.entities;

import java.util.Objects;

/**
 * Created by Andrew on 10/30/2014.
 */
public class SharePermission {
    public String sharedCompanyId;
    public boolean view;
    public boolean edit;
    public boolean add;

    public SharePermission(){

    }

    public SharePermission(String sharedCompanyId, boolean view, boolean edit, boolean add){
        this.sharedCompanyId = sharedCompanyId;
        this.view = view;
        this.edit = edit;
        this.add = add;
    }

    @Override
    public boolean equals (Object o) {
        if ( (o != null) && (o instanceof SharePermission)){
            return this.sharedCompanyId.equals(((SharePermission) o).sharedCompanyId);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(this.sharedCompanyId);
    }
}
