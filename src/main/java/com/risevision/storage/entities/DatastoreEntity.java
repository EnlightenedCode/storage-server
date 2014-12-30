package com.risevision.storage.entities;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.OnSave;
import com.risevision.storage.Utils;

import java.util.Date;

/**
 * Created by rodrigopavezi on 12/10/14.
 */
@Entity
public class DatastoreEntity {

    @Id
    private String id;
    private Date creationDate;
    private Date changedDate;
    private String changedBy;
    private String createdBy;

    public DatastoreEntity(){
        this.id = Utils.generateUUID();
    }

    public DatastoreEntity(String id){
        this.id = id;
    }

    @OnSave
    public void setStatusParameters(){
        Date now = new Date();
        if(creationDate == null){
            creationDate = now;
        }
        changedDate = now;
        if(createdBy == null || createdBy.isEmpty()){
            createdBy = changedBy;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getChangedDate() {
        return changedDate;
    }

    public void setChangedDate(Date changedDate) {
        this.changedDate = changedDate;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
