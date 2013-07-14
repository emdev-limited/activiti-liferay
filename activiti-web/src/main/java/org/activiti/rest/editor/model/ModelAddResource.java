package org.activiti.rest.editor.model;

import net.emforge.activiti.service.ActivitiLocalServiceUtil;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.cmd.SaveResourceCmd;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.api.model.ModelResponse;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;

public class ModelAddResource extends SecuredResource {
	
	private static Log _log = LogFactoryUtil.getLog(ModelAddResource.class);

	@Put
	public ModelResponse addResource(Representation entity) {
		try {
			if (authenticate() == false) {
				return null;
			}
			
			String modelParams = entity.getText();
			JsonNode modelJSON = new ObjectMapper().readTree(modelParams);
			
			String name = modelJSON.path("name").getTextValue();
			_log.info("About to create model with name = " + name);
			long companyId = modelJSON.path("companyId").getLongValue();
			String companyIdStr = String.valueOf(companyId);
			if (StringUtils.isEmpty(companyIdStr)) {
				throw new Exception("Company id is empty");
			}
			String description = null;
			if (modelJSON.path("description") != null) {
				description = modelJSON.path("description").getTextValue();
			}
			String modelId = ActivitiLocalServiceUtil.createNewModel(name, description);
			
			//add company id
			ResourceEntity resource = new ResourceEntity();
            resource.setGenerated(false);
            resource.setName("model" + StringPool.COLON + modelId + StringPool.COLON +"company");
            resource.setBytes(companyIdStr.getBytes("UTF-8"));
			RepositoryServiceImpl serviceImpl = (RepositoryServiceImpl) ActivitiUtil.getRepositoryService();
			serviceImpl.getCommandExecutor().execute(new SaveResourceCmd(resource));
			
			ModelResponse response = new ModelResponse();
			response.setId(modelId);
			
			return response;
		} catch (Exception e) {
			throw new ActivitiException("Failed to add new model", e);
		}
	}
}
