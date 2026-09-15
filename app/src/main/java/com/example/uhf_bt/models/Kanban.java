package com.example.uhf_bt.models;

import com.google.gson.annotations.SerializedName;

public class Kanban {

    @SerializedName("id")
    private long id;

    @SerializedName("rowGuid")
    private String rowGuid;

    @SerializedName("createdAtUtc")
    private String createdAtUtc;

    @SerializedName("kanbanNo")
    private String kanbanNo;

    @SerializedName("kanbanName")
    private String kanbanName;

    @SerializedName("kanbanTagId")
    private String kanbanTagId;

    @SerializedName("number")
    private String number;
    @SerializedName("kanbanType")
    private int kanbanType;
    @SerializedName("kanbanStatus")
    private int kanbanStatus;


    // Getters
    public long getId() { return id; }
    public String getRowGuid() { return rowGuid; }
    public String getCreatedAtUtc() { return createdAtUtc; }
    public String getKanbanNo() { return kanbanNo; }
    public String getKanbanName() { return kanbanName; }
    public String getKanbanTagId() { return kanbanTagId; }
    public String getNumber() { return number; }
    public  int getKanbanType() { return kanbanType; }
    public int getKanbanStatus() { return kanbanStatus; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setRowGuid(String rowGuid) { this.rowGuid = rowGuid; }
    public void setCreatedAtUtc(String createdAtUtc) { this.createdAtUtc = createdAtUtc; }
    public void setKanbanNo(String kanbanNo) { this.kanbanNo = kanbanNo; }
    public void setKanbanName(String kanbanName) { this.kanbanName = kanbanName; }
    public void setKanbanTagId(String kanbanTagId) { this.kanbanTagId = kanbanTagId; }
    public void setNumber(String number) { this.number = number; }
    public  void setKanbanType(int kanbanType) { this.kanbanType = kanbanType; }
    public void setKanbanStatus(int kanbanStatus) { this.kanbanStatus = kanbanStatus; }
}
