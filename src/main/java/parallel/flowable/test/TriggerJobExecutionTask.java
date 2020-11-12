package parallel.flowable.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.context.Context;

public class TriggerJobExecutionTask implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) {
    System.out.println("Starting TriggerJobExecutionTask");
    triggerJobExecution(execution);
    System.out.println("Finished TriggerJobExecutionTask");
  }

  private void triggerJobExecution(DelegateExecution execution) {
    List<Map<String, Object>> jobs = getJobs(execution);
    Set<String> executedJobIds = getExecutedJobIds(execution);
    if (executedJobIds.size() == jobs.size()) {
      System.out.println("Executed jobs: " + executedJobIds);
      Context.getProcessEngineConfiguration().getRuntimeService().signalEventReceived("Jobs Executed");
      return;
    }
    triggerNonExecutedJobs(jobs, executedJobIds);
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> getJobs(DelegateExecution execution) {
    return (List<Map<String, Object>>) execution.getVariable("jobs");
  }

  private Set<String> getExecutedJobIds(DelegateExecution execution) {
    Set<String> executedJobs = new HashSet<String>();
    for (Map.Entry<String, Object> variable : execution.getVariables().entrySet()) {
      if (variable.getKey().startsWith("executedJob")) {
        executedJobs.add((String) variable.getValue());
      }
    }
    return executedJobs;
  }

  private void triggerNonExecutedJobs(List<Map<String, Object>> jobs, Set<String> executedJobIds) {
    List<Map<String, Object>> nonExecutedJobs = getNonExecutedJobs(jobs, executedJobIds);
    boolean anyJobStarted = false;
    for (Map<String, Object> job : nonExecutedJobs) {
      if (canExecuteJob(job, executedJobIds)) {
        triggerJobExecution(job);
        anyJobStarted = true;
      }
    }
    if (!anyJobStarted) {
      throw new RuntimeException("No jobs can be started, but not all are executed!");
    }
  }

  private List<Map<String, Object>> getNonExecutedJobs(List<Map<String, Object>> jobs, Set<String> executedJobs) {
    List<Map<String, Object>> nonExecutedJobs = new ArrayList<Map<String, Object>>();
    for (Map<String, Object> job : jobs) {
      String id = (String) job.get("id");
      if (!executedJobs.contains(id)) {
        nonExecutedJobs.add(job);
      }
    }
    return nonExecutedJobs;
  }

  @SuppressWarnings("unchecked")
  private boolean canExecuteJob(Map<String, Object> job, Set<String> executedJobIds) {
    List<String> jobDependencies = (List<String>) job.get("dependencies");
    Set<String> nonExecutedDependencies = new HashSet<String>(jobDependencies);
    nonExecutedDependencies.removeAll(executedJobIds);
    return nonExecutedDependencies.isEmpty();
  }

  private void triggerJobExecution(Map<String, Object> job) {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("jobId", job.get("id"));
    Context.getProcessEngineConfiguration().getRuntimeService().startProcessInstanceByKey("subProcess", variables);
  }

}
