package org.activiti.rest.editor.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import net.emforge.activiti.service.ActivitiLocalServiceUtil;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.cmd.SaveResourceCmd;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.repository.Model;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.api.exceptions.NotFoundException;
import org.activiti.rest.api.model.ModelResponse;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

import com.liferay.portal.kernel.util.StringPool;

public class ModelDiagramResource extends SecuredResource {

	@Get
	public InputRepresentation getDiagram() {
		String modelId = (String) getRequest().getAttributes().get(
				"modelId");
		try {
			if (NumberUtils.isNumber(modelId)) {
				RepositoryService repositoryService = ProcessEngines
						.getDefaultProcessEngine().getRepositoryService();
				byte[] editorSourceExtra = repositoryService
						.getModelEditorSourceExtra(modelId);
				if (editorSourceExtra != null) {
					InputStream svgStream = new ByteArrayInputStream(
							editorSourceExtra);
					TranscoderInput input = new TranscoderInput(svgStream);

					PNGTranscoder transcoder = new PNGTranscoder();

					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					TranscoderOutput output = new TranscoderOutput(outStream);

					// Do the transformation
					transcoder.transcode(input, output);
					final byte[] result = outStream.toByteArray();
					outStream.close();
					return new InputRepresentation(new ByteArrayInputStream(result), MediaType.IMAGE_ALL);
				}
			}
			throw new NotFoundException("Diagram resource could not be found");
		} catch (Exception e) {
			throw new ActivitiException("Failed to get png from model id = "
					+ modelId, e);
		}
		
	}
}
