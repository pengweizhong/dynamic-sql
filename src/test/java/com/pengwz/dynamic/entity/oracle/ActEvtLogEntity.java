package com.pengwz.dynamic.entity.oracle;

import com.pengwz.dynamic.anno.Column;
import com.pengwz.dynamic.anno.Table;
import com.pengwz.dynamic.config.OracleDatabase156Config;

import java.time.LocalDateTime;

@Table(value = "ACT_EVT_LOG", dataSourceClass = OracleDatabase156Config.class)
public class ActEvtLogEntity {
    /**
     * log_nr_
     */
    @Column("LOG_NR_")
    private Integer logNr;

    /**
     * type_
     */
    @Column("TYPE_")
    private String type;

    /**
     * proc_def_id_
     */
    @Column("PROC_DEF_ID_")
    private String procDefId;

    /**
     * proc_inst_id_
     */
    @Column("PROC_INST_ID_")
    private String procInstId;

    /**
     * execution_id_
     */
    @Column("EXECUTION_ID_")
    private String executionId;

    /**
     * task_id_
     */
    @Column("TASK_ID_")
    private String taskId;

    /**
     * time_stamp_
     */
    @Column("TIME_STAMP_")
    private LocalDateTime timeStamp;

    /**
     * user_id_
     */
    @Column("USER_ID_")
    private String userId;

    /**
     * data_
     */
    @Column("DATA_")
    private Byte data;

    /**
     * lock_owner_
     */
    @Column("LOCK_OWNER_")
    private String lockOwner;

    /**
     * lock_time_
     */
    @Column("LOCK_TIME_")
    private LocalDateTime lockTime;

    /**
     * is_processed_
     */
    @Column("IS_PROCESSED_")
    private Integer isProcessed;

    public Integer getLogNr() {
        return logNr;
    }

    public void setLogNr(Integer logNr) {
        this.logNr = logNr;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProcDefId() {
        return procDefId;
    }

    public void setProcDefId(String procDefId) {
        this.procDefId = procDefId;
    }

    public String getProcInstId() {
        return procInstId;
    }

    public void setProcInstId(String procInstId) {
        this.procInstId = procInstId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Byte getData() {
        return data;
    }

    public void setData(Byte data) {
        this.data = data;
    }

    public String getLockOwner() {
        return lockOwner;
    }

    public void setLockOwner(String lockOwner) {
        this.lockOwner = lockOwner;
    }

    public LocalDateTime getLockTime() {
        return lockTime;
    }

    public void setLockTime(LocalDateTime lockTime) {
        this.lockTime = lockTime;
    }

    public Integer getIsProcessed() {
        return isProcessed;
    }

    public void setIsProcessed(Integer isProcessed) {
        this.isProcessed = isProcessed;
    }

    @Override
    public String toString() {
        return "ActEvtLogEntity{" +
                "logNr=" + logNr +
                ", type='" + type + '\'' +
                ", procDefId='" + procDefId + '\'' +
                ", procInstId='" + procInstId + '\'' +
                ", executionId='" + executionId + '\'' +
                ", taskId='" + taskId + '\'' +
                ", timeStamp=" + timeStamp +
                ", userId='" + userId + '\'' +
                ", data='" + data + '\'' +
                ", lockOwner='" + lockOwner + '\'' +
                ", lockTime=" + lockTime +
                ", isProcessed=" + isProcessed +
                '}';
    }
}
