package org.activiti.rest.application;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.engine.application.StatusFilter;
import org.restlet.representation.Representation;
import org.restlet.service.StatusService;

public class CustomStatusFilter extends StatusFilter {

	/**
     * Constructor from a status service.
     * 
     * @param context
     *            The context.
     * @param statusService
     *            The helped status service.
     */
    public CustomStatusFilter(Context context, StatusService statusService) {
    	super(context, statusService);
    }
    
    /**
     * Returns a representation for the given status.<br>
     * In order to customize the default representation, this method can be
     * overridden.
     * 
     * @param status
     *            The status to represent.
     * @param request
     *            The request handled.
     * @param response
     *            The response updated.
     * @return The representation of the given status.
     */
    protected Representation getRepresentation(Status status, Request request,
            Response response) {
    	CustomStatusService statusService = new CustomStatusService();
        Representation result = statusService.getRepresentation(status,
                request, response);

        if (result == null) {
            result = getDefaultRepresentation(status, request, response);
        }

        return result;
    }
}
