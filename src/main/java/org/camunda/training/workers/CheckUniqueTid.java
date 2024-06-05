package org.camunda.training.workers;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CheckUniqueTid implements JobHandler {

  Logger LOGGER = LoggerFactory.getLogger(ChargeCreditCardWorker.class);
Map<String,Object> uniqueTid = new HashMap<>();

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {

    Map<String, Object> variablesAsMap = job.getVariablesAsMap();
    String tid = (String) variablesAsMap.get("tid");
    synchronized (uniqueTid) {
      if (uniqueTid.containsKey(tid)) {
        jobClient.newThrowErrorCommand(job)
            .errorCode("duplicateID")
            .variables(Map.of("errorCode", tid, "masterPI", uniqueTid.get(tid)))
            .errorMessage("a TID is duplicated")
            .send()
            .join();

      }
      uniqueTid.put(tid, job.getProcessDefinitionKey());
    }

    // nothing to return
    Map<String, Object> variablesToUpdate = new HashMap<>();

    // Complete the Job
    jobClient.newCompleteCommand(job.getKey()).variables(variablesToUpdate).send().join();
  }
}