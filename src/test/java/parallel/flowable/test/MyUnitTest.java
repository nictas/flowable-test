package parallel.flowable.test;

import java.util.List;

import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.FlowableRule;
import org.junit.Rule;
import org.junit.Test;

public class MyUnitTest {

    @Rule
    public FlowableRule flowableRule = new FlowableRule();

    @Test
    @org.flowable.engine.test.Deployment(resources = { "diagrams/main-process.bpmn", "diagrams/sub-process.bpmn" })
    public void test() throws InterruptedException {
        List<Deployment> deployment = flowableRule.getRepositoryService()
            .createDeploymentQuery()
            .list();
        for (Deployment deployment2 : deployment) {
            System.err.println(deployment2.getId());
        }
        List<ProcessDefinition> procDef = flowableRule.getRepositoryService()
            .createProcessDefinitionQuery()
            .deploymentId(deployment.get(0)
                .getId())
            .list();
        for (ProcessDefinition processDefinition : procDef) {
            System.out.println(processDefinition.getKey());
        }

        System.out.println("Starting the process with deployment id: " + procDef.get(0)
            .getName());

        ProcessInstance procInst = flowableRule.getRuntimeService()
            .startProcessInstanceByKey("mainProcess");
        ProcessInstance processInstanceRunning = flowableRule.getRuntimeService()
            .createProcessInstanceQuery()
            .processInstanceId(procInst.getProcessInstanceId())
            .singleResult();
        while (!processInstanceRunning.isEnded()) {
            processInstanceRunning = flowableRule.getRuntimeService()
                .createProcessInstanceQuery()
                .processInstanceId(procInst.getProcessInstanceId())
                .singleResult();
            List<Execution> executions = flowableRule.getRuntimeService().createExecutionQuery().list();
            for (Execution execution : executions) {
                System.out.println("--------------------------");
                System.out.println("ID: " + execution.getId());
                System.out.println("Activity ID: " + execution.getActivityId());
                System.out.println("Name: " + execution.getName());
                System.out.println("Is suspended: " + execution.isSuspended());
                System.out.println("Is ended: " + execution.isEnded());
                System.out.println("Super execution ID: " + execution.getSuperExecutionId());
                System.out.println("Parent ID: " + execution.getParentId());
                System.out.println("Process instance ID: " + execution.getProcessInstanceId());
                System.out.println("--------------------------");
                System.out.println();
            }
            System.out.println("==========================================================================");
            System.out.println("==========================================================================");
            System.out.println("==========================================================================");
//            System.out.println(processInstanceRunning.getActivityId());
            Thread.sleep(2000);
        }

    }

}
