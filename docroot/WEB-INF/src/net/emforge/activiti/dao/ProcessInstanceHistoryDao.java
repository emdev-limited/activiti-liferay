package net.emforge.activiti.dao;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import net.emforge.activiti.entity.ProcessInstanceHistory;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.util.OrderByComparator;

@Repository
public class ProcessInstanceHistoryDao extends HibernateTemplate {
	@Autowired
    public void setHibernateSessionFactory(SessionFactory sessionFactory) {
    	setSessionFactory(sessionFactory);
    }
	
	public void saveOrUpdate(ProcessInstanceHistory processInstanceHistory) {
		if (processInstanceHistory.getProcessInstanceHistoryId() == 0) {
			processInstanceHistory.setCreateDate(new Date());
		}
		
		super.saveOrUpdate(processInstanceHistory);
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessInstanceHistory> searchByWorkflowInstance(final long workflowInstanceId, 
															   final List<Integer> logTypes, 
															   final int start, final int end, final OrderByComparator orderByComparator) {
		return execute(new HibernateCallback<List<ProcessInstanceHistory>>() {
            @Override
            public List<ProcessInstanceHistory> doInHibernate(Session session) throws HibernateException, SQLException {

            	String queryStr = "from ProcessInstanceHistory where workflowInstanceId = :workflowInstanceId";
            	if (logTypes != null && logTypes.size() > 0) {
            		queryStr += " and type in (:logTypes)";
                }

                final Query query = session.createQuery(queryStr);
                
                // set params
                query.setLong("workflowInstanceId", workflowInstanceId);
                if (logTypes != null && logTypes.size() > 0) {
                	query.setParameterList("logTypes", logTypes);
                }

                if (QueryUtil.ALL_POS != start && QueryUtil.ALL_POS != end) {
                	query.setFirstResult(start);
                	query.setMaxResults(end - start);
                }
                if (orderByComparator != null) {
                	// TODO Support order By
                }
                
                return (List<ProcessInstanceHistory>)query.list();
            }
        });
	}
}
