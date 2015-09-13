package org.megafon.task.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashSet;

/**
 * Class for the topic entity with a list of runs within
 *
 * Created by Alexey on 12/09/2015.
 */
public class Topic implements Comparable<Topic>{
    private String topicName;
    private LinkedHashSet<Run> runs;

    public Topic() {
    }

    public Topic(String topicName, LinkedHashSet<Run> runs) {
        this.topicName = topicName;
        this.runs = runs;
    }

    @JsonProperty(value = "topic_name")
    public String getTopicName() {
        return topicName;
    }

    @JsonProperty(value = "topic_runs")
    public LinkedHashSet<Run> getRuns() {
        return runs;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public void setRuns(LinkedHashSet<Run> runs) {
        this.runs = runs;
    }

    @Override
    public int compareTo(Topic o) {
        return topicName.compareTo(o.topicName);
    }
}
