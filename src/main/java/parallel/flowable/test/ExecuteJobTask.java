package parallel.flowable.test;

import java.util.HashMap;
import java.util.Map;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.context.Context;

public class ExecuteJobTask implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) {
    System.out.println("Starting ExecuteJobTask");
    executeJob(execution);
    System.out.println("Finished ExecuteJobTask");
  }

  private void executeJob(DelegateExecution execution) {
    String jobId = (String) execution.getVariable("jobId");
    System.err.println("Executed job: " + jobId);
    signalJobExecuted(jobId);
  }

  private void signalJobExecuted(String jobId) {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("executedJob" + jobId, jobId); // Using a dynamic variable name to avoid FlowableOptimisticLockingExceptions when two ExecuteJobTasks signal TriggerJobExecutionTask at the same time.
    Context.getProcessEngineConfiguration().getRuntimeService().signalEventReceived("Start Job Selection", variables);
  }

}
