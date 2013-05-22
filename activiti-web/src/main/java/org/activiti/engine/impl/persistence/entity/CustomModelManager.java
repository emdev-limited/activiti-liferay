package org.activiti.engine.impl.persistence.entity;

import java.util.List;

import net.emforge.activiti.query.ResourceByCompanyQuery;
import net.emforge.activiti.query.ResourceByCompanyQueryImpl;

import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.repository.Model;
import org.activiti.rest.api.ActivitiUtil;

public class CustomModelManager extends ModelEntityManager {

	public List<Model> findModelsByIds(List<String> ids, int start, int end) {
	    return (List<Model>) getDbSqlSession().selectList("selectModelsByIds", ids, start, end);
	}
	
	public void deleteModel(String companyId, String modelId) {
		super.deleteModel(modelId);
		RepositoryServiceImpl serviceImpl = (RepositoryServiceImpl) ActivitiUtil.getRepositoryService();
		ResourceByCompanyQuery rbc = new ResourceByCompanyQueryImpl(serviceImpl.getCommandExecutor());
		rbc.companyAndNameLike(companyId, "model:" + modelId + ":company");
		ResourceEntity res = (ResourceEntity) getDbSqlSession().selectOne("selectResourceByNameAndCompany", rbc);
		String resourceId = res.getId();
		getDbSqlSession().delete("deleteResourceById", resourceId);
	}
}
