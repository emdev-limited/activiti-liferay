package net.emforge.activiti.query;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.AbstractQuery;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

public class ResourceByCompanyQueryImpl extends AbstractQuery<ResourceByCompanyQuery, String> implements ResourceByCompanyQuery {

	private String companyId;
	private String nameLike;
	
	public ResourceByCompanyQueryImpl(CommandContext commandContext) {
		super(commandContext);
	}
	
	public ResourceByCompanyQueryImpl(CommandExecutor commandExecutor) {
		super(commandExecutor);
	}
	
	@Override
	public ResourceByCompanyQuery companyAndNameLike(String companyId,
			String nameLike) {
		if(StringUtils.isEmpty(companyId) || !NumberUtils.isDigits(companyId) || Long.valueOf(companyId) <= 0) {
			throw new ActivitiException("Company id less then 0");
		}
		if(StringUtils.isEmpty(nameLike)) {
			throw new ActivitiException("nameLike is null");
		}
		this.companyId = companyId;
		this.nameLike = nameLike;
		return this;
	}

	@Override
	public long executeCount(CommandContext commandContext) {
		return (Long) commandContext.getDbSqlSession().selectOne("selectNamesByCompanyCount", this);
	}

	@Override
	public List<String> executeList(CommandContext commandContext,
			Page page) {
//		ensureVariablesInitialized();
		return commandContext.getDbSqlSession().selectList("selectNamesByCompany", this, page);
	}

}
