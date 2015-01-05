package net.emforge.activiti.engine.impl.cmd;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.repository.Deployment;

import com.liferay.portal.kernel.util.StringPool;

/**
 * 
 * @author Dmitry Farafonov
 */
public class GetDefinitionTitleCmd  implements Command<String>, Serializable {

	private static final long serialVersionUID = 1L;
	protected String deploymentId;
	protected String resourceName;
	protected String definitionId;

	public GetDefinitionTitleCmd(String deploymentId, String definitionId) {
		this.deploymentId = deploymentId;
		this.resourceName = definitionId + StringPool.COLON + "title";
		this.definitionId = definitionId;
	}

	@Override
	public String execute(CommandContext commandContext) {
		if (deploymentId == null) {
			throw new ActivitiIllegalArgumentException("deploymentId is null");
		}
		if(resourceName == null) {
			throw new ActivitiIllegalArgumentException("resourceName is null");
		}

		ResourceEntity resourceEntity = Context
				.getCommandContext()
				.getResourceEntityManager()
				.findResourceByDeploymentIdAndResourceName(deploymentId, resourceName);

		if(resourceEntity == null) {
			if(commandContext.getDeploymentEntityManager().findDeploymentById(deploymentId) == null) {
				throw new ActivitiObjectNotFoundException("deployment does not exist: " + deploymentId, Deployment.class);
			} else {
				throw new ActivitiObjectNotFoundException("Definition Title for '" + definitionId + "' does not exist", String.class);
			}
		}

		byte[] resourceBytes = resourceEntity.getBytes();
		String encoding = "UTF-8";
		String titleString = "";
		try {
			titleString = new String(resourceBytes, encoding);
		} catch (UnsupportedEncodingException e) {
			throw new ActivitiException("Unsupported encoding of :" + encoding, e);
		}
		return titleString;
	}

}
