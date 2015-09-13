package org.megafon.task.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Class for the run statistics entity
 *
 * Created by Alexey on 11/09/2015.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RunStatistics extends SimpleRunInfo {
    private Long totalMsgCount = null;

    private Long minMsgCount = null;
    private Long maxMsgCount = null;
    private Double avgMsgCount = null;
    public RunStatistics(Date timestamp, String topicName) {
        super(timestamp, topicName);
    }

    @JsonProperty(value = "total_message_count")
    public Long getTotalMsgCount() {
        return totalMsgCount;
    }

    @JsonProperty(value = "min_message_count")
    public Long getMinMsgCount() {
        return minMsgCount;
    }

    @JsonProperty(value = "max_message_count")
    public Long getMaxMsgCount() {
        return maxMsgCount;
    }

    @JsonProperty(value = "average_message_count")
    public Double getAvgMsgCount() {
        return avgMsgCount;
    }

    public void setTotalMsgCount(Long totalMsgCount) {
        this.totalMsgCount = totalMsgCount;
    }

    public void setMinMsgCount(Long minMsgCount) {
        this.minMsgCount = minMsgCount;
    }

    public void setMaxMsgCount(Long maxMsgCount) {
        this.maxMsgCount = maxMsgCount;
    }

    public void setAvgMsgCount(Double avgMsgCount) {
        this.avgMsgCount = avgMsgCount;
    }
}
