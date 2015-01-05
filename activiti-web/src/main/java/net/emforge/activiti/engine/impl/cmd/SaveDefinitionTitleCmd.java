package net.emforge.activiti.engine.impl.cmd;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.cmd.SaveResourceCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.apache.commons.lang3.StringUtils;

import com.liferay.portal.kernel.util.StringPool;

/**
 * 
 * @author Dmitry Farafonov
 */
public class SaveDefinitionTitleCmd  implements Command<Void>, Serializable {

	private static final long serialVersionUID = 1L;
	protected String deploymentId;
	protected String resourceName;
	protected String title;

	public SaveDefinitionTitleCmd(String deploymentId, String definitionId, String title) {
		this.deploymentId = deploymentId;
		this.resourceName = definitionId + StringPool.COLON + "title";
		this.title = title;
	}

	@Override
	public Void execute(CommandContext commandContext) {
		if (StringUtils.isEmpty(title)){
			return null;
		}
		if (deploymentId == null) {
			throw new ActivitiIllegalArgumentException("deploymentId is null");
		}
		if(resourceName == null) {
			throw new ActivitiIllegalArgumentException("resourceName is null");
		}

		try {
			ResourceEntity resourceEntity = Context
					.getCommandContext()
					.getResourceEntityManager()
					.findResourceByDeploymentIdAndResourceName(deploymentId, resourceName);

			if(resourceEntity == null) {
				resourceEntity = new ResourceEntity();
				resourceEntity.setDeploymentId(deploymentId);
				resourceEntity.setGenerated(false);
				resourceEntity.setName(resourceName);
				resourceEntity.setBytes(title.getBytes("UTF-8"));

				Context
					.getCommandContext()
					.getResourceEntityManager()
					.insertResource(resourceEntity);
			} else {
				resourceEntity.setBytes(title.getBytes("UTF-8"));
				(new SaveResourceCmd(resourceEntity)).execute(commandContext);
			}
		} catch (UnsupportedEncodingException e) {
			throw new ActivitiIllegalArgumentException("Unsupported encoding of:" + title, e);
		}
		return null;
	}

}
