package org.megafon.task.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class for the offset entity
 *
 * Created by Alexey on 11/09/2015.
 */
public class Offset {
    private Integer partitionNo;
    private Long msgCount;

    public Offset(Integer partitionNo, Long msgCount) {
        this.partitionNo = partitionNo;
        this.msgCount = msgCount;
    }

    @JsonProperty(value = "partition_no")
    public Integer getPartitionNo() {
        return partitionNo;
    }

    @JsonProperty(value = "message_count")
    public Long getMsgCount() {
        return msgCount;
    }
}
