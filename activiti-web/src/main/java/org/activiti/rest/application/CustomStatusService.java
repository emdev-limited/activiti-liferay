package org.activiti.rest.application;

import org.activiti.engine.ActivitiException;
import org.activiti.rest.api.exceptions.AbstractRestException;
import org.activiti.rest.api.exceptions.BadRequestException;
import org.activiti.rest.api.exceptions.ConflictException;
import org.activiti.rest.api.exceptions.NotAuthorizedException;
import org.activiti.rest.api.exceptions.NotFoundException;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.service.StatusService;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.StringPool;

public class CustomStatusService extends StatusService {
	
	@Override
    public org.restlet.routing.Filter createInboundFilter(Context context) {
        return new CustomStatusFilter(context, this);
    }
	
	@Override
	public Representation getRepresentation(Status status, Request request,
			Response response) {
		String ret = StringPool.BLANK;
	    if (status.getDescription() != null) {
	        ret = status.getDescription();
	    } else {
	        ret = "Unknown error";
	    }
	    JSONObject json = JSONFactoryUtil.createJSONObject();
	    json.put("responseSuccess", false);
	    json.put("responseMessage", ret);
	    json.put("responseCode", status.getCode());
	    return new StringRepresentation(json.toString(), MediaType.APPLICATION_JSON);
	}

	public Status getStatus(Throwable throwable, Request request,
            Response response) {
        Status result = null;

        if (throwable instanceof ResourceException) {
            ResourceException re = (ResourceException) throwable;
            result = re.getStatus();
        } if (throwable instanceof BadRequestException 
        		|| throwable instanceof NotFoundException
        		|| throwable instanceof NotAuthorizedException
        		|| throwable instanceof ConflictException) {
        	AbstractRestException ae = (AbstractRestException) throwable;
        	result = new Status(new Status(ae.getCode()), ae.getMessage());
        } if (throwable instanceof ActivitiException) { 
        	ActivitiException ae = (ActivitiException) throwable;
        	result = new Status(Status.SERVER_ERROR_INTERNAL, ae.getMessage());
        } else {
            result = new Status(Status.SERVER_ERROR_INTERNAL, throwable);
        }

        return result;
    }
}
