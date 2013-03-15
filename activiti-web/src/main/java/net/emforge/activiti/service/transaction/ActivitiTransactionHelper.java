package net.emforge.activiti.service.transaction;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;

public class ActivitiTransactionHelper implements ActivitiTransactionHelperIF {
	private static Log _log = LogFactoryUtil
			.getLog(ActivitiTransactionHelper.class.getName());
	
	private static final String MODEL_DEFAULT_NAME = "New Model";
	private static final String MODEL_ID = "modelId";
	private static final String MODEL_NAME = "name";
	private static final String MODEL_REVISION = "revision";
	private static final String MODEL_DESCRIPTION = "description";
	
	@Override
	public String createNewModel(String modelName, String modelDescription) throws SystemException, PortalException {
		try {
			modelName = StringUtils.isEmpty(modelName) ? MODEL_DEFAULT_NAME : modelName;
			modelDescription = modelDescription == null ? StringPool.BLANK : modelDescription;
			RepositoryService repositoryService = ProcessEngines
					.getDefaultProcessEngine().getRepositoryService();
	        ObjectMapper objectMapper = new ObjectMapper();
	        ObjectNode editorNode = objectMapper.createObjectNode();
	        editorNode.put("id", "canvas");
	        editorNode.put("resourceId", "canvas");
	        ObjectNode stencilSetNode = objectMapper.createObjectNode();
	        stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
	        editorNode.put("stencilset", stencilSetNode);
	        Model modelData = repositoryService.newModel();
	          
	        ObjectNode modelObjectNode = objectMapper.createObjectNode();
	          //
	        modelObjectNode.put(MODEL_NAME, modelName);
	        modelObjectNode.put(MODEL_REVISION, 1);
	        modelObjectNode.put(MODEL_DESCRIPTION, modelDescription);
	        modelData.setMetaInfo(modelObjectNode.toString());
	        modelData.setName(modelName);
	          
	        repositoryService.saveModel(modelData);
	          
	        repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
	          
	        return modelData.getId();
	      } catch(Exception e) {
			throw new RuntimeException(e.getMessage());
	      }
	}

	public String test(String s) throws SystemException, PortalException,
			ActivitiException {
		try {
			User user = UserLocalServiceUtil.getUser(10196);
			user.setGreeting(user.getGreeting() + " hey!");
			UserLocalServiceUtil.updateUser(user);

			return doActivitiStuff();
		} catch (Exception e) {
			_log.error(e, e);
			throw new RuntimeException(e.getMessage());
		}

		//
		// Task task = taskService.newTask();
		// task.setName("XXX");
		// taskService.saveTask(task);
		//
		// return ">>> " + s + " taskService=" + taskService + " task=" + task +
		// "<<<";
	}

	// @Transactional(rollbackFor={ActivitiException.class})
	private String doActivitiStuff() {
//		ApplicationContext context = ApplicationContextProvider
//				.getApplicationContext();
//		DataSourceTransactionManager txManager = (DataSourceTransactionManager) context
//				.getBean("transactionManager");
//
//		TransactionDefinition def = new DefaultTransactionDefinition();
//		TransactionStatus status = txManager.getTransaction(def);
		String deploymentId = StringPool.BLANK;
		try {
			RepositoryService repositoryService = ProcessEngines
					.getDefaultProcessEngine().getRepositoryService();
			deploymentId = repositoryService
					.createDeployment()
					.addClasspathResource(
							"META-INF/resources/Simpliest.bpmn20.xml").deploy()
					.getId();
			_log.info("Deployed process def with id = " + deploymentId);

			if (true) {
//				txManager.rollback(status);
				throw new ActivitiException(deploymentId);
			} else {
//				txManager.commit(status);
				return deploymentId;
			}

		} catch (Exception e) {
			throw new ActivitiException(deploymentId);
		}

	}

}
