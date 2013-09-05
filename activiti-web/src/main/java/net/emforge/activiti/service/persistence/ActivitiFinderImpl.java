package net.emforge.activiti.service.persistence;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.liferay.portal.kernel.dao.orm.QueryPos;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.SQLQuery;
import com.liferay.portal.kernel.dao.orm.Session;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.service.persistence.impl.BasePersistenceImpl;
import com.liferay.util.dao.orm.CustomSQLUtil;

public class ActivitiFinderImpl extends BasePersistenceImpl implements ActivitiFinder {
	
	private static Log log = LogFactoryUtil.getLog(ActivitiFinderImpl.class);
	
	private static final String FIND_TOP_EXECUTIONS = ActivitiFinderImpl.class.getName() + ".findTopExecutions";	
	private static final String FIND_SUB_EXECUTIONS = ActivitiFinderImpl.class.getName() + ".findSubExecutions";
	private static final String FIND_UNIQUE_TASK_NAMES = ActivitiFinderImpl.class.getName() + ".findUniqueUserTaskNames";
	private static final String FIND_UNIQUE_TASK_ASSIGNEES = ActivitiFinderImpl.class.getName() + ".findUniqueUserTaskAssignees";

	public List<String> findTopExecutions(List<String> processInstanceIds) throws SystemException {
        Session session = null;
        try {
                session = openSession();
                String sql = CustomSQLUtil.get(FIND_TOP_EXECUTIONS);

                String sgrp = "(" + StringUtils.join(processInstanceIds, ",") + ")";
                sql = StringUtil.replace(sql, "[$INSTANCE_IDS$]", sgrp);                	
                                
                SQLQuery sqlQuery = session.createSQLQuery(sql);
                
                List<String> itemIds = (List<String>)QueryUtil.list(sqlQuery, getDialect(), QueryUtil.ALL_POS, QueryUtil.ALL_POS);                        
                
                return itemIds;                                 
        } catch (Exception e) {
                throw new SystemException(e);
        } finally {
                closeSession(session);
        }     			
	}
	
	public List<String> findSubExecutions(List<String> execIds) throws SystemException {
        Session session = null;
        try {
                session = openSession();
                String sql = CustomSQLUtil.get(FIND_SUB_EXECUTIONS);

                String sgrp = "(" + StringUtils.join(execIds, ",") + ")";
                sql = StringUtil.replace(sql, "[$EXEC_IDS$]", sgrp);                	
                                
                SQLQuery sqlQuery = session.createSQLQuery(sql);
                
                List<String> itemIds = (List<String>)QueryUtil.list(sqlQuery, getDialect(), QueryUtil.ALL_POS, QueryUtil.ALL_POS);                        
                
                return itemIds;                                 
        } catch (Exception e) {
                throw new SystemException(e);
        } finally {
                closeSession(session);
        }     			
	}
	
	public List<String> findUniqueUserTaskNames(List<String> execIds) throws SystemException {
        Session session = null;
        try {
                session = openSession();
                String sql = CustomSQLUtil.get(FIND_UNIQUE_TASK_NAMES);

                String sgrp = "(" + StringUtils.join(execIds, ",") + ")";
                sql = StringUtil.replace(sql, "[$EXEC_IDS$]", sgrp);                	
                                
                SQLQuery sqlQuery = session.createSQLQuery(sql);
                
                List<String> itemIds = (List<String>)QueryUtil.list(sqlQuery, getDialect(), QueryUtil.ALL_POS, QueryUtil.ALL_POS);                        
                
                return itemIds;                                 
        } catch (Exception e) {
                throw new SystemException(e);
        } finally {
                closeSession(session);
        }     			
	}
	
	public List findUniqueUserTaskAssignees(List<String> execIds) throws SystemException {
        Session session = null;
        try {
                session = openSession();
                String sql = CustomSQLUtil.get(FIND_UNIQUE_TASK_ASSIGNEES);

                String sgrp = "(" + StringUtils.join(execIds, ",") + ")";
                sql = StringUtil.replace(sql, "[$EXEC_IDS$]", sgrp);                	
                                
                SQLQuery sqlQuery = session.createSQLQuery(sql);
                
                List itemIds = QueryUtil.list(sqlQuery, getDialect(), QueryUtil.ALL_POS, QueryUtil.ALL_POS);                        
                
                return itemIds;                                 
        } catch (Exception e) {
                throw new SystemException(e);
        } finally {
                closeSession(session);
        }     			
	}	
}
