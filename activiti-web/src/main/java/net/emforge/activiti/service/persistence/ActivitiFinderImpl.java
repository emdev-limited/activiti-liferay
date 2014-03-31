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
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.service.persistence.impl.BasePersistenceImpl;
import com.liferay.util.dao.orm.CustomSQLUtil;

public class ActivitiFinderImpl extends BasePersistenceImpl implements
        ActivitiFinder {

    private static Log log = LogFactoryUtil.getLog(ActivitiFinderImpl.class);

    private static final String FIND_TOP_EXECUTIONS = ActivitiFinderImpl.class.getName() + ".findTopExecutions";
    private static final String FIND_SUB_EXECUTIONS     = ActivitiFinderImpl.class.getName() + ".findSubExecutions";
    private static final String FIND_UNIQUE_TASK_NAMES   = ActivitiFinderImpl.class.getName() + ".findUniqueUserTaskNames";
    private static final String FIND_UNIQUE_TASK_ASSIGNEES = ActivitiFinderImpl.class.getName() + ".findUniqueUserTaskAssignees";
    private static final String FIND_USER_TASKS         = ActivitiFinderImpl.class.getName() + ".findUserTasks";
    private static final String FIND_SUPER_EXECUTIONS     = ActivitiFinderImpl.class.getName() + ".findSuperExecutions";
    private static final String FIND_HI_ACTIVITIES     = ActivitiFinderImpl.class.getName() + ".findHiActivities";
    private static final String FIND_EXEC_BY_TASKS         = ActivitiFinderImpl.class.getName() + ".findExecByTask";

    private static final String FIND_USER_TASKS_TASKNAME   = " and (t.NAME_ = ?)";
    private static final String FIND_USER_TASKS_ASSIGNEE   = " and (t.ASSIGNEE_ = ?)";
    private static final String FIND_USER_TASKS_CANDIDATE  = " and (l.GROUP_ID_ = ?)";

    @Override
    public List<String> findTopExecutions(List<String> processInstanceIds)
            throws SystemException {
        Session session = null;
        try {
            session = openSession();
            String sql = CustomSQLUtil.get(FIND_TOP_EXECUTIONS);

            String sgrp = "(" + StringUtils.join(processInstanceIds, ",") + ")";
            sql = StringUtil.replace(sql, "[$INSTANCE_IDS$]", sgrp);

            SQLQuery sqlQuery = session.createSQLQuery(sql);

            List<String> itemIds = (List<String>) QueryUtil.list(sqlQuery,
                    getDialect(), QueryUtil.ALL_POS, QueryUtil.ALL_POS);

            return itemIds;
        } catch (Exception e) {
            throw new SystemException(e);
        } finally {
            closeSession(session);
        }
    }

    @Override
    public List<String> findSubExecutions(List<String> execIds)
            throws SystemException {
        Session session = null;
        try {
            session = openSession();
            String sql = CustomSQLUtil.get(FIND_SUB_EXECUTIONS);

            String sgrp = "(" + StringUtils.join(execIds, ",") + ")";
            sql = StringUtil.replace(sql, "[$EXEC_IDS$]", sgrp);

            SQLQuery sqlQuery = session.createSQLQuery(sql);

            List<String> itemIds = (List<String>) QueryUtil.list(sqlQuery,
                    getDialect(), QueryUtil.ALL_POS, QueryUtil.ALL_POS);

            return itemIds;
        } catch (Exception e) {
            throw new SystemException(e);
        } finally {
            closeSession(session);
        }
    }

    @Override
    public List<String> findUniqueUserTaskNames(List<String> execIds)
            throws SystemException {
        Session session = null;
        try {
            session = openSession();
            String sql = CustomSQLUtil.get(FIND_UNIQUE_TASK_NAMES);

            String sgrp = "(" + StringUtils.join(execIds, ",") + ")";
            sql = StringUtil.replace(sql, "[$EXEC_IDS$]", sgrp);

            SQLQuery sqlQuery = session.createSQLQuery(sql);

            List<String> itemIds = (List<String>) QueryUtil.list(sqlQuery,
                    getDialect(), QueryUtil.ALL_POS, QueryUtil.ALL_POS);

            return itemIds;
        } catch (Exception e) {
            throw new SystemException(e);
        } finally {
            closeSession(session);
        }
    }

    @Override
    public List findUniqueUserTaskAssignees(List<String> execIds)
            throws SystemException {
        Session session = null;
        try {
            session = openSession();
            String sql = CustomSQLUtil.get(FIND_UNIQUE_TASK_ASSIGNEES);

            String sgrp = "(" + StringUtils.join(execIds, ",") + ")";
            sql = StringUtil.replace(sql, "[$EXEC_IDS$]", sgrp);

            SQLQuery sqlQuery = session.createSQLQuery(sql);

            List itemIds = QueryUtil.list(sqlQuery, getDialect(),
                    QueryUtil.ALL_POS, QueryUtil.ALL_POS);

            return itemIds;
        } catch (Exception e) {
            throw new SystemException(e);
        } finally {
            closeSession(session);
        }
    }
    
    @Override
    public List<Object[]> findExecByTask(String taskId) throws SystemException {

        Session session = null;
        try {
            session = openSession();
            String sql = CustomSQLUtil.get(FIND_EXEC_BY_TASKS);

            SQLQuery sqlQuery = session.createSQLQuery(sql);

            QueryPos qPos = QueryPos.getInstance(sqlQuery);
            qPos.add(taskId);

            List<Object[]> itemIds = (List<Object[]>) QueryUtil.list(sqlQuery,
                    getDialect(), QueryUtil.ALL_POS, QueryUtil.ALL_POS);

            return itemIds;
        } catch (Exception e) {
            throw new SystemException(e);
        } finally {
            closeSession(session);
        }
    }    

    @Override
    public List<Object[]> findUserTasks(String taskName, String assigneeUser,
            String candidateRole) throws SystemException {

        Session session = null;
        try {
            session = openSession();
            String sql = CustomSQLUtil.get(FIND_USER_TASKS);

            if (Validator.isNotNull(taskName)) {
                sql = sql + FIND_USER_TASKS_TASKNAME;
            }
            if (Validator.isNotNull(assigneeUser)) {
                sql = sql + FIND_USER_TASKS_ASSIGNEE;
            }
            if (Validator.isNotNull(candidateRole)) {
                sql = sql + FIND_USER_TASKS_CANDIDATE;
            }

            SQLQuery sqlQuery = session.createSQLQuery(sql);

            QueryPos qPos = QueryPos.getInstance(sqlQuery);
            if (Validator.isNotNull(taskName)) {
                qPos.add(taskName);
            }
            if (Validator.isNotNull(assigneeUser)) {
                qPos.add(assigneeUser);
            }
            if (Validator.isNotNull(candidateRole)) {
                qPos.add(candidateRole);
            }

            List<Object[]> itemIds = (List<Object[]>) QueryUtil.list(sqlQuery,
                    getDialect(), QueryUtil.ALL_POS, QueryUtil.ALL_POS);

            return itemIds;
        } catch (Exception e) {
            throw new SystemException(e);
        } finally {
            closeSession(session);
        }
    }

    @Override
    public List<Object[]> findSuperExecutions(List<String> execIds)
            throws SystemException {
        Session session = null;
        try {
            session = openSession();
            String sql = CustomSQLUtil.get(FIND_SUPER_EXECUTIONS);

            String sgrp = "(" + StringUtils.join(execIds, ",") + ")";
            sql = StringUtil.replace(sql, "[$EXEC_IDS$]", sgrp);

            SQLQuery sqlQuery = session.createSQLQuery(sql);

            List<Object[]> itemIds = (List<Object[]>) QueryUtil.list(sqlQuery,
                    getDialect(), QueryUtil.ALL_POS, QueryUtil.ALL_POS);

            return itemIds;
        } catch (Exception e) {
            throw new SystemException(e);
        } finally {
            closeSession(session);
        }
    }
    
    @Override
    public List<String> findHiActivities(String activityName, List<String> execIds)
            throws SystemException {
        Session session = null;
        try {
            session = openSession();
            String sql = CustomSQLUtil.get(FIND_HI_ACTIVITIES);

            String sgrp = "(" + StringUtils.join(execIds, ",") + ")";
            sql = StringUtil.replace(sql, "[$EXEC_IDS$]", sgrp);

            SQLQuery sqlQuery = session.createSQLQuery(sql);
            
            QueryPos qPos = QueryPos.getInstance(sqlQuery);
            qPos.add(activityName);

            List<String> itemIds = (List<String>) QueryUtil.list(sqlQuery,
                    getDialect(), QueryUtil.ALL_POS, QueryUtil.ALL_POS);

            return itemIds;
        } catch (Exception e) {
            throw new SystemException(e);
        } finally {
            closeSession(session);
        }
    }    
}
