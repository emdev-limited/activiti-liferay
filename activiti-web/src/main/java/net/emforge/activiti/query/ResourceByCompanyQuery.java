package net.emforge.activiti.query;

import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.query.Query;

public interface ResourceByCompanyQuery extends Query<ResourceByCompanyQuery, String> {

	public ResourceByCompanyQuery companyAndNameLike(String companyId, String nameLike);
}
