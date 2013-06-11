package org.activiti.engine.impl.cmd;

import java.io.Serializable;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;

public class GetDeploymentCmd implements Command<DeploymentEntity>, Serializable {
	  
	  private static final long serialVersionUID = 1L;
	  protected String deploymentId;
	  
	  public GetDeploymentCmd(String deploymentId) {
	    this.deploymentId = deploymentId;
	  }

	  public DeploymentEntity execute(CommandContext commandContext) {
	    if (deploymentId == null) {
	      throw new ActivitiException("deploymentId is null");
	    }
	    
	    DeploymentEntity resource = commandContext
	      .getDeploymentEntityManager()
	      .findDeploymentById(deploymentId);
	    return resource;
	  }
	  
	}
