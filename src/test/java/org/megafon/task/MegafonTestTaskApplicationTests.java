package org.megafon.task;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.megafon.task.readers.RunsFileReader;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MegafonTestTaskApplication.class)
public class MegafonTestTaskApplicationTests {

	public static final int NON_EMPTY_TOPICS_COUNT = 1;
	public static final int EMPTY_TOPICS_COUNT = 1;
	public static final String TEST_BASE_DIR = "src/test/resources";
	private MockMvc mvc;

	/**
	 * Method which clears the specified folder and then populates it with the file structure needed.
	 */
	@Before
	public void setUp() throws Exception {
		clearFolder(new File(TEST_BASE_DIR));

		File dir = new File(TEST_BASE_DIR);

		if (dir.exists()) {
			for (int i = 1; i <= NON_EMPTY_TOPICS_COUNT + EMPTY_TOPICS_COUNT; i++) {
				File topicDir = new File(dir, "topic_" + i);
				topicDir.mkdir();
				File historyDir = new File(topicDir, "history");
				historyDir.mkdir();
				for(Date timeStamp = new Date(0L);
						timeStamp.before(new Date(300000000000L));
						timeStamp.setTime(timeStamp.getTime()+150000000000L)) {
					File runDir = new File(historyDir, RunsFileReader.dateFormat.format(timeStamp));
					runDir.mkdir();
					File offsets = new File(runDir, "offsets.csv");
					PrintWriter writer = new PrintWriter(offsets, "UTF-8");
					if (i <= NON_EMPTY_TOPICS_COUNT) {
						writer.println("1,0");
						writer.println("2,10");
					}
					writer.close();
				}
			}
		}

		//creating mock for the REST server
		mvc = MockMvcBuilders.standaloneSetup(new TestTaskController()).build();
		//setting the base directory as if we'd given it with a parameter
		TestTaskController.baseDir = TEST_BASE_DIR;
	}

	/**
	 * Test for the /get_topics request with different parameter values
	 */
	@Test
	public void getTopicsTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/get_topics").param("empty", "n"))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("[\"topic_1\"]")));

		mvc.perform(MockMvcRequestBuilders.get("/get_topics").param("empty", "y"))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("[\"topic_2\"]")));

		mvc.perform(MockMvcRequestBuilders.get("/get_topics").param("empty", "any"))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("[\"topic_1\",\"topic_2\"]")));
	}

	/**
	 * Test for the /get_time_stamps request with different parameter values
	 */
	@Test
	public void getTimeStampsTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/get_time_stamps").param("pos", "last"))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("[{\"topic_name\":\"topic_1\",\"time_stamp\":\"1974-10-03-05-40-00\"}," +
						"{\"topic_name\":\"topic_2\",\"time_stamp\":\"1974-10-03-05-40-00\"}]")));

		mvc.perform(MockMvcRequestBuilders.get("/get_time_stamps").param("pos", "first"))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("[{\"topic_name\":\"topic_1\",\"time_stamp\":\"1970-01-01-03-00-00\"}," +
						"{\"topic_name\":\"topic_2\",\"time_stamp\":\"1970-01-01-03-00-00\"}]")));

		mvc.perform(MockMvcRequestBuilders.get("/get_time_stamps").param("pos", "any"))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("[{\"topic_name\":\"topic_1\",\"time_stamp\":\"1970-01-01-03-00-00\"}," +
						"{\"topic_name\":\"topic_1\",\"time_stamp\":\"1974-10-03-05-40-00\"}," +
						"{\"topic_name\":\"topic_2\",\"time_stamp\":\"1970-01-01-03-00-00\"}," +
						"{\"topic_name\":\"topic_2\",\"time_stamp\":\"1974-10-03-05-40-00\"}]")));
	}

	/**
	 * Test for the /get_stats request with different parameter values
	 */
	@Test
	public void getStatsTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/get_stats").param("pos", "last").param("stats","sum,min,max,avg"))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("[{\"topic_name\":\"topic_1\",\"total_message_count\":10,\"min_message_count\":0,\"max_message_count\":10,\"average_message_count\":5.0,\"time_stamp\":\"1974-10-03-05-40-00\"}," +
						"{\"topic_name\":\"topic_2\",\"total_message_count\":0,\"time_stamp\":\"1974-10-03-05-40-00\"}]")));

		mvc.perform(MockMvcRequestBuilders.get("/get_stats").param("pos", "last").param("stats",""))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("[{\"topic_name\":\"topic_1\",\"time_stamp\":\"1974-10-03-05-40-00\"}," +
						"{\"topic_name\":\"topic_2\",\"time_stamp\":\"1974-10-03-05-40-00\"}]")));

		mvc.perform(MockMvcRequestBuilders.get("/get_stats").param("pos", "any").param("stats","sum,min,max,avg"))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("[{\"topic_name\":\"topic_1\",\"total_message_count\":10,\"min_message_count\":0,\"max_message_count\":10,\"average_message_count\":5.0,\"time_stamp\":\"1970-01-01-03-00-00\"}," +
						"{\"topic_name\":\"topic_1\",\"total_message_count\":10,\"min_message_count\":0,\"max_message_count\":10,\"average_message_count\":5.0,\"time_stamp\":\"1974-10-03-05-40-00\"}," +
						"{\"topic_name\":\"topic_2\",\"total_message_count\":0,\"time_stamp\":\"1970-01-01-03-00-00\"}," +
						"{\"topic_name\":\"topic_2\",\"total_message_count\":0,\"time_stamp\":\"1974-10-03-05-40-00\"}]")));

		mvc.perform(MockMvcRequestBuilders.get("/get_stats").param("pos", "any").param("stats",""))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("[{\"topic_name\":\"topic_1\",\"time_stamp\":\"1970-01-01-03-00-00\"}," +
						"{\"topic_name\":\"topic_1\",\"time_stamp\":\"1974-10-03-05-40-00\"}," +
						"{\"topic_name\":\"topic_2\",\"time_stamp\":\"1970-01-01-03-00-00\"}," +
						"{\"topic_name\":\"topic_2\",\"time_stamp\":\"1974-10-03-05-40-00\"}]")));
	}

	/**
	 * Test for the /get_offsets request with different parameter values
	 */
	@Test
	public void getOffsets() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/get_offsets").param("pos", "last"))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("[{\"topic_name\":\"topic_1\",\"topic_runs\":[{\"offsets\":[{\"partition_no\":1,\"message_count\":0},{\"partition_no\":2,\"message_count\":10}],\"time_stamp\":\"1974-10-03-05-40-00\"}]}," +
						"{\"topic_name\":\"topic_2\",\"topic_runs\":[{\"offsets\":[],\"time_stamp\":\"1974-10-03-05-40-00\"}]}]")));

		mvc.perform(MockMvcRequestBuilders.get("/get_offsets").param("pos", "first"))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("[{\"topic_name\":\"topic_1\",\"topic_runs\":[{\"offsets\":[{\"partition_no\":1,\"message_count\":0},{\"partition_no\":2,\"message_count\":10}],\"time_stamp\":\"1970-01-01-03-00-00\"}]}," +
						"{\"topic_name\":\"topic_2\",\"topic_runs\":[{\"offsets\":[],\"time_stamp\":\"1970-01-01-03-00-00\"}]}]")));

		mvc.perform(MockMvcRequestBuilders.get("/get_offsets").param("pos", "any"))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("[{\"topic_name\":\"topic_1\",\"topic_runs\":[{\"offsets\":[{\"partition_no\":1,\"message_count\":0},{\"partition_no\":2,\"message_count\":10}],\"time_stamp\":\"1970-01-01-03-00-00\"},{\"offsets\":[{\"partition_no\":1,\"message_count\":0},{\"partition_no\":2,\"message_count\":10}],\"time_stamp\":\"1974-10-03-05-40-00\"}]}," +
						"{\"topic_name\":\"topic_2\",\"topic_runs\":[{\"offsets\":[],\"time_stamp\":\"1970-01-01-03-00-00\"},{\"offsets\":[],\"time_stamp\":\"1974-10-03-05-40-00\"}]}]")));
	}


	/**
	 * Recursive method for directory cleaning
	 */
	private void clearFolder(File folder) {
		File[] files = folder.listFiles();
		if(files!=null) { //some JVMs return null for empty dirs
			for(File f: files) {
				if(f.isDirectory()) {
					clearFolder(f);
					f.delete();
				} else {
					f.delete();
				}
			}
		}
	}

}
