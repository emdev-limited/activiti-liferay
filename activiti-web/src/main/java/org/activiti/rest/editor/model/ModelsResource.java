package org.activiti.rest.editor.model;

import java.util.List;

import net.emforge.activiti.query.ResourceByCompanyQuery;
import net.emforge.activiti.query.ResourceByCompanyQueryImpl;

import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.cmd.GetModelsCmd;
import org.activiti.engine.repository.Model;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.SecuredResource;
import org.apache.commons.lang.math.NumberUtils;
import org.restlet.resource.Get;

import com.liferay.portal.kernel.util.StringPool;

public class ModelsResource extends SecuredResource {

	@Get
	public DataResponse getModels() {
		if(authenticate() == false) return null;

		int start = 0;
		int end = 0;
		
		String startStr = getQuery().getValues("start");
	    String endStr = getQuery().getValues("end");
	    String page = getQuery().getValues("page");
	    String companyId = getQuery().getValues("companyId");
	    
	    if (NumberUtils.isNumber(startStr)) {
	    	start = Integer.valueOf(startStr);
	    }
	    
	    if (NumberUtils.isNumber(endStr)) {
	    	end = Integer.valueOf(endStr);
	    }
		
		RepositoryServiceImpl serviceImpl = (RepositoryServiceImpl) ActivitiUtil.getRepositoryService();
        List<Model> list = serviceImpl.getCommandExecutor().execute(new GetModelsCmd(companyId, start, end));
        
		ResourceByCompanyQuery rbc = new ResourceByCompanyQueryImpl(serviceImpl.getCommandExecutor());
		rbc.companyAndNameLike(companyId, "model:%:company");
        
        DataResponse response = new DataResponse();
        response.setStart(start);
        response.setSize(list.size()); 
        response.setSort(StringPool.BLANK);
        response.setOrder(StringPool.BLANK);
        response.setTotal(rbc.count());
        response.setData(list);
        
	    return response;
	}
}
