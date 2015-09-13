package org.megafon.task;

import org.megafon.task.entities.*;
import org.megafon.task.readers.RunsFileReader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Class for the RESTful response logic for the /get_topics, /get_time_stamps, /get_stats and /get_offsets requests
 *
 * Created by Alexey on 11/09/2015.
 */

@RestController
class TestTaskController {
    private static final String STAT_TYPES_DELIM = ",";
    public static String baseDir;


    /**
     * Method for processing the request of getting topics list
     * @param empty the flag for the topic emptiness. Could be 'y', 'yes' or 'true' for getting only empty topics,
     *              'n', 'no' or 'false' for getting only topics that are not empty and
     *              any other for all topics
     * @return  responses with the JSON with list of topic names.
     */
    @RequestMapping("/get_topics")
    public ResponseEntity<Object> getTopics(@RequestParam(value = "empty", defaultValue = "no_matter") String empty) {
        if (baseDir == null) {
            return new ResponseEntity<>("Base directory is not set at servlet startup", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        LinkedHashSet<Topic> filteredTopics;
        LinkedHashSet<Topic> topics;

        try {
            RunsFileReader reader = new RunsFileReader(baseDir);
            topics = reader.readTopics();
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Boolean shouldBeEmpty = parseBooleanFlag(empty);

        if (shouldBeEmpty == null) {
            filteredTopics = topics;
        } else {
            filteredTopics = filterEmptyTopics(topics, shouldBeEmpty);
        }

        return new ResponseEntity<>(getTopicsNames(filteredTopics), HttpStatus.OK);
    }

    /**
     * Method for processing the request of getting topics' runs information
     * @param pos the flag for the timestamp filter. Could be 'last' or 'newest' for the last runs,
     *            or 'first' or 'oldest' for the oldest runs,
     *            or any other for no filter
     * @return  responses with the JSON with list of topics with their names and timestamps.
     */
    @RequestMapping("/get_time_stamps")
    public ResponseEntity<Object> getTimeStamps(@RequestParam(value = "pos", defaultValue = "no_matter") String pos) {
        if (baseDir == null) {
            return new ResponseEntity<>("Base directory is not set at servlet startup", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        LinkedHashSet<Topic> topics;
        LinkedHashSet<Topic> filteredTopics;

        try {
            RunsFileReader reader = new RunsFileReader(baseDir);
            topics = reader.readTopics();
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }


        filteredTopics = filterTopicsByTimestamp(topics, parsePositionFlag(pos));

        return new ResponseEntity<>(getRunInfo(filteredTopics), HttpStatus.OK);
    }

    /**
     * Method for processing the request of getting statistics for the offsets files
     * @param pos the flag for the timestamp filter. Could be 'last' or 'newest' for the last runs,
     *            or 'first' or 'oldest' for the oldest runs,
     *            or any other for no filter
     * @param statTypes the statistics needed to be in the response. Separated with the commas:
     *                  'total' or 'sum' or 'ttl' for total amount of messages;
     *                  'minimum' or 'min' for minimum amount of messages;
     *                  'maximum' or 'min' for minimum amount of messages;
     *                  'average' or 'avg' for average amount of messages;
     *                  values could be repeated, it doesn't affect the program run
     * @return  responses with the JSON with list of topics with their names, timestamps and statistics.
     */
    @RequestMapping("/get_stats")
    public ResponseEntity<Object> getStats(@RequestParam(value = "pos", defaultValue = "no_matter") String pos, @RequestParam(value = "stats") String statTypes) {
        if (baseDir == null) {
            return new ResponseEntity<>("Base directory is not set at servlet startup", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        LinkedHashSet<Topic> topics;
        LinkedHashSet<Topic> filteredTopics;

        try {
            RunsFileReader reader = new RunsFileReader(baseDir);
            topics = reader.readTopics();
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }


        filteredTopics = filterTopicsByTimestamp(topics, parsePositionFlag(pos));

        return new ResponseEntity<>(getRunsStats(filteredTopics, statTypes), HttpStatus.OK);
    }

    /**
     * Method for processing the request of getting partition list out of the offsets table
     * @param pos the flag for the timestamp filter. Could be 'last' or 'newest' for the last runs,
     *            or 'first' or 'oldest' for the oldest runs,
     *            or any other for no filter
     * @return  responses with the JSON with list of topics and their runs with offsets within.
     */
    @RequestMapping("/get_offsets")
    public ResponseEntity<Object> getOffsets(@RequestParam(value = "pos", defaultValue = "no_matter") String pos) {
        if (baseDir == null) {
            return new ResponseEntity<>("Base directory is not set at servlet startup", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        LinkedHashSet<Topic> topics;
        LinkedHashSet<Topic> filteredTopics;

        try {
            RunsFileReader reader = new RunsFileReader(baseDir);
            topics = reader.readTopics();
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }


        filteredTopics = filterTopicsByTimestamp(topics, parsePositionFlag(pos));

        return new ResponseEntity<>(filteredTopics, HttpStatus.OK);
    }

    /**
     * method for converting Topic list into the list of SimpleRunInfo
     */
    private LinkedHashSet<SimpleRunInfo> getRunInfo(LinkedHashSet<Topic> topics) {
        LinkedHashSet<SimpleRunInfo> runInfo = new LinkedHashSet<>();

        for (Topic topic : topics) {
            for (Run run : topic.getRuns()) {
                runInfo.add(new SimpleRunInfo(run.getTimeStamp(), topic.getTopicName()));
            }
        }

        return runInfo;
    }

    /**
     * method for converting Topic list into the list of RunStatistics
     */
    private LinkedHashSet<RunStatistics> getRunsStats(LinkedHashSet<Topic> topics, String statTypes) {
        List<String> statTypesArr = Arrays.asList(statTypes.split(STAT_TYPES_DELIM));

        LinkedHashSet<RunStatistics> runStats = new LinkedHashSet<>();

        for (Topic topic : topics) {
            for (Run run : topic.getRuns()) {
                RunStatistics runStatistics = new RunStatistics(run.getTimeStamp(), topic.getTopicName());

                //here the stats are calculated
                if (statTypesArr.contains("total") || statTypesArr.contains("sum") || statTypesArr.contains("ttl")) {
                    Long total = 0L;
                    for (Offset offset : run.getOffsets()) {
                        total = total + offset.getMsgCount();
                    }
                    runStatistics.setTotalMsgCount(total);
                }
                if (statTypesArr.contains("min") || statTypesArr.contains("minimum")) {
                    if (run.getOffsets().size() > 0) {
                        Long min = Long.MAX_VALUE;
                        for (Offset offset : run.getOffsets()) {
                            if (offset.getMsgCount() < min) {
                                min = offset.getMsgCount();
                            }
                        }
                        runStatistics.setMinMsgCount(min);
                    }
                }
                if (statTypesArr.contains("max") || statTypesArr.contains("maximum")) {
                    if (run.getOffsets().size() > 0) {
                        Long max = Long.MIN_VALUE;
                        for (Offset offset : run.getOffsets()) {
                            if (offset.getMsgCount() > max) {
                                max = offset.getMsgCount();
                            }
                        }
                        runStatistics.setMaxMsgCount(max);
                    }
                }
                if (statTypesArr.contains("avg") || statTypesArr.contains("average")) {
                    if (run.getOffsets().size() > 0) {
                        Double avg = 0d;
                        for (Offset offset : run.getOffsets()) {
                            avg = avg + offset.getMsgCount();
                        }
                        avg = avg / run.getOffsets().size();
                        runStatistics.setAvgMsgCount(avg);
                    }
                }

                runStats.add(runStatistics);
            }
        }

        return runStats;
    }

    /**
     * method for filtering topics by their timestamp
     */
    private LinkedHashSet<Topic> filterTopicsByTimestamp(LinkedHashSet<Topic> topics, String filter) {
        LinkedHashSet<Topic> filteredTopics = new LinkedHashSet<>(topics.size());

        for (Topic topic : topics) {
            LinkedHashSet<Run> filteredRuns = new LinkedHashSet<>();
            if ("last".equals(filter)) {
                filteredRuns.add(Collections.max(topic.getRuns()));
            } else if ("first".equals(filter)) {
                filteredRuns.add(Collections.min(topic.getRuns()));
            } else if (filter == null) {
                filteredRuns = topic.getRuns();
            }
            filteredTopics.add(new Topic(topic.getTopicName(), filteredRuns));
        }

        return filteredTopics;
    }

    /**
     * method for converting String flag to Boolean value
     */
    @Nullable
    private Boolean parseBooleanFlag(String flag) {
        if ("true".equalsIgnoreCase(flag) || "y".equalsIgnoreCase(flag) || "yes".equalsIgnoreCase(flag)) {
            return true;
        } else if ("false".equalsIgnoreCase(flag) || "n".equalsIgnoreCase(flag) || "no".equalsIgnoreCase(flag)) {
            return false;
        } else {
            return null;
        }
    }

    /**
     * method for converting String flag to more strict String flag
     */
    @Nullable
    private String parsePositionFlag(String flag) {
        if ("last".equalsIgnoreCase(flag) || "newest".equalsIgnoreCase(flag)) {
            return "last";
        } else if ("first".equalsIgnoreCase(flag) || "oldest".equalsIgnoreCase(flag)) {
            return "first";
        } else {
            return null;
        }
    }

    /**
     * method for converting the list of topics to the list of their names
     */
    private LinkedHashSet<String> getTopicsNames(LinkedHashSet<Topic> topics) {
        LinkedHashSet<String> topicsNames = new LinkedHashSet<>(topics.size());

        for (Topic topic : topics) {
            topicsNames.add(topic.getTopicName());
        }

        return topicsNames;
    }

    /**
     * method for filtering the empty topics
     */
    private LinkedHashSet<Topic> filterEmptyTopics(LinkedHashSet<Topic> topics, boolean shouldBeEmpty) {
        LinkedHashSet<Topic> filteredTopics = new LinkedHashSet<>();

        for (Topic topic : topics) {
            boolean isTopicPass = shouldBeEmpty;
            for (Run run : topic.getRuns()) {
                if (run.getOffsets().size() > 0) {
                    isTopicPass = !shouldBeEmpty;
                    //if we've found the needed run - we don't need to look the others
                    break;
                }
            }
            if (isTopicPass) {
                filteredTopics.add(topic);
            }
        }
        return filteredTopics;
    }
}
