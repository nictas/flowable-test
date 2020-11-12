package parallel.flowable.test;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.context.Context;

public class StartJobSelectionTask implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) {
    System.out.println("In StartJobSelectionTask");
    Context.getProcessEngineConfiguration().getRuntimeService().signalEventReceived("Start Job Selection");
    System.out.println("Finished StartJobSelectionTask");
  }

}
