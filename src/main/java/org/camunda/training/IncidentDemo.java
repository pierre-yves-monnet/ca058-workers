package org.camunda.training;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.Deployment;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

@SpringBootApplication
@Deployment(resources = "classpath*:*.bpmn")
public class IncidentDemo {
  public static void main(String[] args) {
    SpringApplication.run(IncidentDemo.class, args);
  }

  @JobWorker(type = "incidentTask")
  public void createIncident(final JobClient client, final ActivatedJob job) {

  //   System.out.println("jobworker incidentTask called...");
    Map<String, Object> variables = job.getVariablesAsMap();
    Boolean shouldFail = (Boolean) variables.get("shouldFail");
    if (shouldFail) {
      System.out.println("jobworker incidentTask failed");
      client
          .newFailCommand(job.getKey())
          .retries(job.getRetries() - 1)
          .errorMessage("Should Fail")
          //.retryBackoff(Duration.ofSeconds(5))
          .send()
          .exceptionally((
              throwable -> {
                throw new RuntimeException("Could not complete job", throwable);
              }
          ));
    } else {
      System.out.println("jobworker incidentTask complete.");
      client
          .newCompleteCommand(job.getKey())
          .send()
          .exceptionally((
              throwable -> {
                throw new RuntimeException("Could not complete job", throwable);
              }
          ));

    }
  }
}
