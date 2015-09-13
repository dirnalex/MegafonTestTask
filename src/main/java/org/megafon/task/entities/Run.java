package org.megafon.task.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.megafon.task.readers.RunsFileReader;

import java.util.Date;
import java.util.List;


/**
 * Class for the run entity with a list of offsets within
 *
 * Created by Alexey on 11/09/2015.
 */

public class Run implements Comparable<Run>{
    private Date timeStamp;

    private List<Offset> offsets;

    public Run() {
    }

    public Run(Date timeStamp, List<Offset> offsets) {
        this.timeStamp = timeStamp;
        this.offsets = offsets;
    }

    @JsonProperty(value = "time_stamp")
    public String getStringTimeStamp() {
        return RunsFileReader.dateFormat.format(timeStamp);
    }

    @JsonIgnore
    public Date getTimeStamp() {
        return timeStamp;
    }

    @JsonProperty(value = "offsets")
    public List<Offset> getOffsets() {
        return offsets;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setOffsets(List<Offset> offsets) {
        this.offsets = offsets;
    }

    @Override
    public int compareTo(Run o) {
        return timeStamp.compareTo(o.timeStamp);
    }
}
