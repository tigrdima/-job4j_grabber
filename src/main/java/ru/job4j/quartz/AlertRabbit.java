package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {
    private static Connection cn;

    public static void main(String[] args) {
        try {
            int intervalInSeconds = Integer.parseInt(readProperties().getProperty("rabbit.interval"));
            init(readProperties());

            List<String> store = new ArrayList<>();
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("store", store);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(intervalInSeconds)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
            System.out.println(store);
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static void init(Properties properties) {
        try {
            Class.forName(properties.getProperty("driver-class-name"));
             cn = DriverManager.getConnection(
                    properties.getProperty("url"),
                    properties.getProperty("username"),
                    properties.getProperty("password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static Properties readProperties() {
       Properties pr = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
           pr.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pr;
    }

    public static class Rabbit implements Job {
        public Rabbit() {
            try (PreparedStatement pr = cn.prepareStatement("insert into rabbit (created_date) values (?)")) {
                pr.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                pr.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
            List<String> store = (List<String>) context.getJobDetail().getJobDataMap().get("store");
            store.add("Операция выполнена");
        }
    }
}
