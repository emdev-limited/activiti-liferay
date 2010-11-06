package net.emforge.activiti.dao;

import java.io.Serializable;
import java.util.List;

import net.emforge.activiti.entity.ProcessInstanceExtensionImpl;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	@SuppressWarnings("unchecked")
	public int count(long companyId, Long userId, String assetClassName, Long assetClassPK, Boolean completed) {
		DetachedCriteria criteria = DetachedCriteria.forClass(ProcessInstanceExtensionImpl.class);
		if (companyId != 0l) {
			criteria.add(Restrictions.eq("companyId", companyId));
		}
		
		if (userId != null) {
			criteria.add(Restrictions.eq("userId", userId));
		}

		if (Validator.isNotNull(assetClassName)) {
			criteria.add(Restrictions.like("className", assetClassName));
		}

		if (Validator.isNotNull(assetClassPK)) {
			criteria.add(Restrictions.eq("classPK", assetClassPK));
		}

		if (completed != null) {
			_log.error("This functionality is not implemented yet");
			/*
			DetachedCriteria completionCriteria =
				criteria.createCriteria("processInstance");

			if (completed) {
				completionCriteria.add(Restrictions.eq("state", "end"));
			}
			else {
				completionCriteria.add(Restrictions.eq("state", "active"));
			}
			*/
		}
	
		criteria.setProjection(Projections.rowCount());
		
		List<Object> result = findByCriteria(criteria);
		return GetterUtil.getInteger((Serializable)result.get(0));
	}
	
	public List<ProcessInstanceExtensionImpl> find(long companyId, Long userId, 
			   									   String assetClassName, Long assetClassPK,
			   									   Boolean completed, int start, int end, OrderByComparator orderByComparator) {
		DetachedCriteria criteria = DetachedCriteria.forClass(ProcessInstanceExtensionImpl.class);
		if (companyId != 0l) {
			criteria.add(Restrictions.eq("companyId", companyId));
		}
		
		if (userId != null) {
			criteria.add(Restrictions.eq("userId", userId));
		}

		if (Validator.isNotNull(assetClassName)) {
			criteria.add(Restrictions.like("className", assetClassName));
		}

		if (Validator.isNotNull(assetClassPK)) {
			criteria.add(Restrictions.eq("classPK", assetClassPK));
		}

		if (completed != null) {
			_log.error("This functionality is not implemented yet");
			/*
			DetachedCriteria completionCriteria =
				criteria.createCriteria("processInstance");

			if (completed) {
				completionCriteria.add(Restrictions.not((Restrictions.eq("state", "active"))));
			}
			else {
				completionCriteria.add(Restrictions.eq("state", "active"));
			}
			*/
		}
		
		// TODO Add support for pagination - probably like described here:
		// http://labs.jodd.org/d/paginate-with-hibernate.html
		// add pagination
		if ((start != QueryUtil.ALL_POS) && (end != QueryUtil.ALL_POS)) {
			_log.warn("Method is partially implemented");
			//criteria.setFirstResult(start);
			//criteria.setMaxResults(end - start);
		}
		
		
		
		List<ProcessInstanceExtensionImpl> result = findByCriteria(criteria);
		
		return result;
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
