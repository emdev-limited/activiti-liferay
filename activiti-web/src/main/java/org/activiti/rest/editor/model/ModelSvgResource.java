package org.activiti.rest.editor.model;

import java.io.ByteArrayInputStream;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.api.exceptions.NotFoundException;
import org.apache.commons.lang.math.NumberUtils;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.resource.Get;

public class ModelSvgResource extends SecuredResource {

	@Get
	public InputRepresentation getSvg() {
		String modelId = (String) getRequest().getAttributes().get(
				"modelId");
		try {
			if (NumberUtils.isNumber(modelId)) {
				RepositoryService repositoryService = ProcessEngines
						.getDefaultProcessEngine().getRepositoryService();
				byte[] extraBytes = repositoryService.getModelEditorSourceExtra(modelId);
				return new InputRepresentation(new ByteArrayInputStream(extraBytes), MediaType.TEXT_ALL);
			}
			throw new NotFoundException("Diagram resource could not be found");
		} catch (Exception e) {
			throw new ActivitiException("Failed to get png from model id = "
					+ modelId, e);
		}
		
	}
}
