package org.activiti.rest.editor.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.api.exceptions.NotFoundException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.lang.math.NumberUtils;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.resource.Get;

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
				} else {
					// build image from model
					// TODO
				}
			}
			throw new NotFoundException("Diagram resource could not be found");
		} catch (Exception e) {
			throw new ActivitiException("Failed to get png from model id = "
					+ modelId, e);
		}
		
	}
}
