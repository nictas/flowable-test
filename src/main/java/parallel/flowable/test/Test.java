package parallel.flowable.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;

public class Test {
  public static void main(String[] args) throws InterruptedException {
    ProcessEngineConfiguration processEngineConfiguration = new StandaloneProcessEngineConfiguration().setJdbcUrl("jdbc:h2:mem:flowable;DB_CLOSE_DELAY=-1").setJdbcUsername("sa").setJdbcPassword("").setJdbcDriver("org.h2.Driver")
        .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
    processEngineConfiguration.setAsyncExecutorActivate(true);

    ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();
    RepositoryService repositoryService = processEngine.getRepositoryService();
    Deployment mainProcessDeployment = repositoryService.createDeployment().addClasspathResource("diagrams/main-process.bpmn").deploy();
    Deployment subProcessDeployment = repositoryService.createDeployment().addClasspathResource("diagrams/sub-process.bpmn").deploy();

    ProcessDefinition mainProcessDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(mainProcessDeployment.getId()).singleResult();
    System.out.println("Found process definition : " + mainProcessDefinition.getKey());
    ProcessDefinition subProcessDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(subProcessDeployment.getId()).singleResult();
    System.out.println("Found process definition : " + subProcessDefinition.getKey());

    RuntimeService runtimeService = processEngine.getRuntimeService();
    Map<String, Object> variables = new HashMap<String, Object>();
    Map<String, Object> job1 = new HashMap<String, Object>();
    job1.put("id", "foo");
    job1.put("dependencies", Collections.emptyList());
    Map<String, Object> job2 = new HashMap<String, Object>();
    job2.put("id", "bar");
    job2.put("dependencies", Arrays.asList("foo"));
    Map<String, Object> job3 = new HashMap<String, Object>();
    job3.put("id", "baz");
    job3.put("dependencies", Arrays.asList("foo"));
    Map<String, Object> job4 = new HashMap<String, Object>();
    job4.put("id", "qux");
    job4.put("dependencies", Arrays.asList("bar"));
    variables.put("jobs", Arrays.asList(job4, job2, job3, job1));

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("mainProcess", variables);
    while (processInstance != null) {
      System.out.println("Process still running. Current task: " + processInstance.getActivityId());
      processInstance = processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
      Thread.sleep(2000);
    }

  }
}
