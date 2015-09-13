package org.megafon.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MegafonTestTaskApplication {

    private static final String PREFIX = "--";
    private static final String EQUALS_DELIM = "=";

    public static void main(String[] args) {

        //looking for the --base_dir=<path> program argument
        for (String arg : args) {
            if (arg.startsWith(PREFIX)) {
                String[] argArr = arg.substring(PREFIX.length()).split(EQUALS_DELIM);
                if (argArr.length == 2) {
                    if ("base_dir".equals(argArr[0])) {
                        TestTaskController.baseDir = argArr[1];
                    }
                }
            }
        }


        SpringApplication.run(MegafonTestTaskApplication.class, args);
    }
}
