//
//import OddsApi.Application;
//import org.quartz.*;
//import org.quartz.impl.StdSchedulerFactory;
//
//import java.util.Arrays;
//
//public class ScheduleExample implements Job {
//    public void execute(JobExecutionContext context) {
//
//        System.out.println("Executing method....");
//        try {
//            Application.getHomeXAwayOddPerMatch();
//        } catch (final Throwable ex) {
//            System.out.println("Unable to retrieve odds: " + Arrays.toString(ex.getStackTrace()));
//        }
//    }
//
//    public static void main(String[] args) throws SchedulerException {
//
//        JobDetail job = JobBuilder.newJob(ScheduleExample.class)
//                .withIdentity("Odds Retriever", "group1").build();
//
//        // Trigger the job to run on the next round minute
//        Trigger trigger = TriggerBuilder
//                .newTrigger()
//                .withIdentity("dummyTriggerName", "group1")
//                .withSchedule(
//                        SimpleScheduleBuilder.simpleSchedule()
//                                .withIntervalInMinutes(30).repeatForever())
//                .build();
//
//        // schedule it
//        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
//        scheduler.start();
//        scheduler.scheduleJob(job, trigger);
//    }
//
//}
