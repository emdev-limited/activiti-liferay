package net.emforge.activiti.task;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.task.IdentityLink;
import org.apache.commons.lang.StringUtils;

/**
 * Saves first assigned candidate role name in task local variable.
 * @author irina
 *
 */
public class SaveInitialRoleListener implements TaskListener {
	
	public static final String NAME_INITIAL_COMPANY_ID = "initialCompanyId";
	public static final String NAME_INITIAL_ROLE_NAME = "initialRoleName";
	
	@Override
	public void notify(DelegateTask task) {
	    for (IdentityLink lnk: task.getCandidates()) {
	        if (! StringUtils.isEmpty(lnk.getGroupId())) {
	            String[] ids = splitGroup(lnk.getGroupId());
	            task.setVariableLocal(NAME_INITIAL_COMPANY_ID, Long.parseLong(ids[0]));
	            task.setVariableLocal(NAME_INITIAL_ROLE_NAME, ids[1]);
	            break;
	        }
	    }
	}

	private static String[] splitGroup(String src) {
	    int n = src.indexOf('/');
	    if (n >= 0) {
	        return new String[] {src.substring(0, n), src.substring(n+1)};
	    } else {
	        return new String[] {src, null};
	    }
	}
}
