package app;

import busca.infra.ExceptionHandler;
import captura.infra.RequestFilter;
import captura.infra.health.DefaultHealthCheck;
import captura.infra.jobs.ScrapperJob;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import ru.vyarus.dropwizard.guice.GuiceBundle;

public class JournalSearchApplication extends Application<JournalSearchConfiguration> {

    public static GuiceBundle guice;

    public static void main(String[] args) throws Exception {
        new JournalSearchApplication().run(args);
    }

    @Override
    public void run(JournalSearchConfiguration journalSearchConfiguration, Environment environment)
            throws Exception {
        environment.healthChecks().register("default", new DefaultHealthCheck());
        environment.servlets().addFilter("Custom-Filter", RequestFilter.class)
                .addMappingForUrlPatterns(java.util.EnumSet.allOf(javax.servlet.DispatcherType.class), true, "/*");
        environment.jersey().register(new ExceptionHandler());
        var scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.setJobFactory(new GuiceJobFactory(guice.getInjector()));
        scheduler.start();
        var job = JobBuilder.newJob(ScrapperJob.class)
                .withIdentity("scrapper", "diario")
                .build();
        var trigger = TriggerBuilder
                .newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule(journalSearchConfiguration.getSchedule()))
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    @Override
    public void initialize(Bootstrap<JournalSearchConfiguration> bootstrap) {
        guice = GuiceBundle.builder()
                .enableAutoConfig("captura", "busca", "envio")
                .printDiagnosticInfo()
                .modules(new DependencyModule())
                .build();
        bootstrap.addBundle(guice);
        bootstrap.addBundle(new SwaggerBundle<>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(JournalSearchConfiguration configuration) {
                return configuration.swaggerBundleConfiguration;
            }
        });
    }
}
