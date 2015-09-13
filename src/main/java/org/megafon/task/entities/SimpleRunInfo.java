package org.megafon.task.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.megafon.task.readers.RunsFileReader;

import java.util.Date;

/**
 * Class for the run entity without list of offsets
 *
 * Created by Alexey on 10/09/2015.
 */
public class SimpleRunInfo {
    private Date timeStamp;
    private String topicName;

    public SimpleRunInfo(Date timestamp, String topicName) {
        this.timeStamp = timestamp;
        this.topicName = topicName;
    }

    @JsonProperty(value = "time_stamp")
    public String getStringTimeStamp() {
        return RunsFileReader.dateFormat.format(timeStamp);
    }

    @JsonIgnore
    public Date getTimeStamp() {
        return timeStamp;
    }

    @JsonProperty(value = "topic_name")
    public String getTopicName() {
        return topicName;
    }
}
