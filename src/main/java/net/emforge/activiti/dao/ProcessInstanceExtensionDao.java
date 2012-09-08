package net.emforge.activiti.dao;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import net.emforge.activiti.entity.ProcessInstanceExtensionImpl;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.Validator;

@Repository
public class ProcessInstanceExtensionDao extends HibernateTemplate {
	private static Log _log = LogFactoryUtil.getLog(ProcessInstanceExtensionDao.class);

	@Autowired
    public void setHibernateSessionFactory(SessionFactory sessionFactory) {
    	setSessionFactory(sessionFactory);
    }
	
	@SuppressWarnings("unchecked")
	public ProcessInstanceExtensionImpl findByProcessInstanceId(String processInstanceId) {
		DetachedCriteria criteria = DetachedCriteria.forClass(ProcessInstanceExtensionImpl.class);
		criteria.add(Restrictions.eq("processInstanceId", processInstanceId));
		
		List<ProcessInstanceExtensionImpl> result = findByCriteria(criteria);
		if (result.size() > 0) {
			return result.get(0);
		} else {
			return null;
		}
	}
	
	public int count(final long companyId, final Long userId, final String assetClassName, final Long assetClassPK, final Boolean completed) {
		return execute(new HibernateCallback<Integer>() {
			@Override
	        public Integer doInHibernate(Session session) throws HibernateException, SQLException {
	
	        	String queryStr = "SELECT count(*) FROM ACT_PIE_LIFERAY PI, ACT_HI_PROCINST HI where PI.process_instance_id = HI.proc_inst_id_";
	        	if (companyId != 0l) {
	    			queryStr += " and PI.company_id = :companyId";
	    		}
	    		
	    		if (userId != null) {
	    			queryStr += " and PI.user_id = :userId";
	    		}
	
	    		if (Validator.isNotNull(assetClassName)) {
	    			queryStr += " and PI.class_name like :assetClassName";
	    		}
	
	    		if (Validator.isNotNull(assetClassPK)) {
	    			queryStr += " and PI.class_pk like :assetClassPK";
	    		}
	
	    		if (completed != null) {
	    			if (completed) {
	    				queryStr += " and HI.end_time_ is not null";
	    			} else {
	    				queryStr += " and HI.end_time_ is null";
	    			}
	    		}
	        	
	        	final SQLQuery query = session.createSQLQuery(queryStr);
	            
	        	if (companyId != 0l) {
	    			query.setLong("companyId", companyId);
	    		}
	    		
	    		if (userId != null) {
	    			query.setLong("userId", userId);
	    		}
	
	    		if (Validator.isNotNull(assetClassName)) {
	    			query.setString("assetClassName", assetClassName);
	    		}
	
	    		if (Validator.isNotNull(assetClassPK)) {
	    			query.setLong("assetClassPK", assetClassPK);
	    		}
	        	
	            return GetterUtil.getInteger((Serializable)query.uniqueResult());
	        }
	    });
	}
	
	public List<ProcessInstanceExtensionImpl> find(final long companyId, final Long userId, 
			   									   final String assetClassName, final Long assetClassPK,
			   									   final Boolean completed, final int start, final int end, final OrderByComparator orderByComparator) {
		return execute(new HibernateCallback<List<ProcessInstanceExtensionImpl>>() {
			@SuppressWarnings("unchecked")
			@Override
	        public List<ProcessInstanceExtensionImpl> doInHibernate(Session session) throws HibernateException, SQLException {
	
	        	String queryStr = "SELECT PI.* FROM ACT_PIE_LIFERAY PI, ACT_HI_PROCINST HI where PI.process_instance_id = HI.proc_inst_id_";
	        	if (companyId != 0l) {
	    			queryStr += " and PI.company_id = :companyId";
	    		}
	    		
	    		if (userId != null) {
	    			queryStr += " and PI.user_id = :userId";
	    		}
	
	    		if (Validator.isNotNull(assetClassName)) {
	    			queryStr += " and PI.class_name like :assetClassName";
	    		}
	
	    		if (Validator.isNotNull(assetClassPK)) {
	    			queryStr += " and PI.class_pk like :assetClassPK";
	    		}
	
	    		if (completed != null) {
	    			if (completed) {
	    				queryStr += " and HI.end_time_ is not null";
	    			} else {
	    				queryStr += " and HI.end_time_ is null";
	    			}
	    		}
	        	
	        	final SQLQuery query = session.createSQLQuery(queryStr);
	            
	        	if (companyId != 0l) {
	    			query.setLong("companyId", companyId);
	    		}
	    		
	    		if (userId != null) {
	    			query.setLong("userId", userId);
	    		}
	
	    		if (Validator.isNotNull(assetClassName)) {
	    			query.setString("assetClassName", assetClassName);
	    		}
	
	    		if (Validator.isNotNull(assetClassPK)) {
	    			query.setLong("assetClassPK", assetClassPK);
	    		}
	        	
	            if (QueryUtil.ALL_POS != start && QueryUtil.ALL_POS != end) {
	            	query.setFirstResult(start);
	            	query.setMaxResults(end - start);
	            }
	            if (orderByComparator != null) {
	            	// TODO Support order By
	            }
	            
	            query.addEntity(ProcessInstanceExtensionImpl.class);
	            
	            return (List<ProcessInstanceExtensionImpl>)query.list();
	        }
	    });
	}
	
	protected void addOrder(DetachedCriteria criteria, OrderByComparator orderByComparator) {
		if (orderByComparator == null) {
			return;
		}

		String[] orderByFields = orderByComparator.getOrderByFields();

		for (String orderByField : orderByFields) {
			Order order = null;

			if (orderByComparator.isAscending()) {
				order = Order.asc(orderByField);
			}
			else {
				order = Order.desc(orderByField);
			}

			criteria.addOrder(order);
		}
	}	
}
