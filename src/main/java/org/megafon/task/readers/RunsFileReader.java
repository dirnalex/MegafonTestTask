package org.megafon.task.readers;

import org.megafon.task.entities.Offset;
import org.megafon.task.entities.Run;
import org.megafon.task.entities.Topic;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class for reader entities - those which shall be able to read the run files structure from the base directory
 *
 * Created by Alexey on 11/09/2015.
 */
public class RunsFileReader {
    private static final String HISTORY_DIR_NAME = "history";
    private static final String OFFSETS_FILE_NAME = "offsets.csv";

    //unified date format, which should be used anywhere in the program. Therefore it should be easily manageable.
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private static final String OFFSETS_DELIMITER = ",";

    private File baseDir;

    /**
     * Constructor, which can translate string path to the real file object.
     * @param baseDirPath string for the path, relative or absolute
     * @throws FileNotFoundException if the path was not found in the filesystem, or not valid
     */
    public RunsFileReader(@NotNull String baseDirPath) throws FileNotFoundException {
        File baseDir = new File(baseDirPath);
        if (baseDir.isDirectory()) {
            this.baseDir = baseDir;
        } else {
            throw new FileNotFoundException(baseDirPath + " is not a directory path.");
        }
    }

    /**
     * Main method for getting the topics and all inside of them with the list of Topic entities
     * @return set of topics
     * @throws IOException if there were problems with file/directory reading
     * @throws ParseException if there were any problems with offsets files parsing
     */
    public LinkedHashSet<Topic> readTopics() throws IOException, ParseException {
        LinkedHashSet<Topic> topics = new LinkedHashSet<>();
        Topic topic;
        LinkedHashSet<Run> runs;
        Run run;

        for(File topicFolder : listSubFolders(baseDir)) {
            topic = new Topic();
            topic.setTopicName(topicFolder.getName());

            File historyFolder = getSubDirectoryByName(topicFolder, HISTORY_DIR_NAME);

            runs = new LinkedHashSet<>();
            for (File timestampFolder : listSubFolders(historyFolder)) {
                run = new Run();
                try {
                    run.setTimeStamp(dateFormat.parse(timestampFolder.getName()));
                } catch (ParseException e) {
                    throw new ParseException("Folder " + timestampFolder.getAbsolutePath() + " has unappropriated name format. Format should be next: " + dateFormat.toPattern(), e.getErrorOffset());
                }
                File offsetsFile = getSubFileByName(timestampFolder, OFFSETS_FILE_NAME);

                List<Offset> offsets = readOffsetsFile(offsetsFile);

                run.setOffsets(offsets);
                runs.add(run);
            }

            topic.setRuns(runs);
            topics.add(topic);
        }

        return topics;
    }

    /**
     * Method for reading the offsets.csv file
     *
     * @param offsetsFile the file itself
     * @return the list of Offset entities
     * @throws IOException if there were problems with file/directory reading
     * @throws ParseException if there were any problems with offsets files parsing
     */
    private List<Offset> readOffsetsFile(File offsetsFile) throws IOException, ParseException {
        List<Offset> offsets = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(offsetsFile));

        String currentLine;
        String[] splitCurrentLine;
        int lineNo = 1;

        while ((currentLine = br.readLine()) != null) {
            splitCurrentLine = currentLine.split(OFFSETS_DELIMITER);
            if (splitCurrentLine.length == 2) {
                try {
                    offsets.add(new Offset(Integer.parseInt(splitCurrentLine[0]), Long.parseLong(splitCurrentLine[1])));
                } catch (NumberFormatException e) {
                    throw new ParseException("File " + offsetsFile.getAbsolutePath() + "has unappropriated number format at line " + lineNo, lineNo);
                }
            } else {
                throw new ParseException("File " + offsetsFile.getAbsolutePath() + "has unappropriated format of line " + lineNo, lineNo);
            }
            lineNo++;
        }

        return offsets;
    }

    /**
     * Get the file from the specified folder with specified name
     *
     * @param parentDir specified folder where the file should be searched
     * @param fileName specified name of the file to search
     * @return File object of the file
     * @throws FileNotFoundException if there was no such file
     */
    private File getSubFileByName(File parentDir, String fileName) throws FileNotFoundException {
        File offsetsFile;
        try {
            offsetsFile = parentDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && fileName.equals(pathname.getName());
                }
            })[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new FileNotFoundException("'" + fileName + "' file was not found in " + parentDir.getAbsolutePath() + " folder");
        }
        return offsetsFile;
    }

    /**
     * Get the folder from the specified folder with specified name
     *
     * @param parentDir specified folder where the folder should be searched
     * @param dirName specified name of the folder to search
     * @return File object of the folder
     * @throws FileNotFoundException if there was no such folder
     */
    private File getSubDirectoryByName(File parentDir, String dirName) throws FileNotFoundException {
        File historyFolder;
        try {
            historyFolder = parentDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory() && dirName.equals(pathname.getName());
                }
            })[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new FileNotFoundException("'" + dirName + "' folder was not found in " + parentDir.getAbsolutePath() + " folder");
        }
        return historyFolder;
    }

    /**
     * get the list of all folders within the specified one
     */
    @NotNull
    private File[] listSubFolders(File parentDir) {
        return parentDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
    }

    /**
     * Setter for the baseDir field, which can translate string path to the real file object.
     */
    public void setBaseDir(@NotNull String baseDirPath) throws FileNotFoundException {
        File baseDir = new File(baseDirPath);
        if (baseDir.isDirectory()) {
            this.baseDir = baseDir;
        } else {
            throw new FileNotFoundException(baseDirPath + " is not a directory path.");
        }
    }
}
