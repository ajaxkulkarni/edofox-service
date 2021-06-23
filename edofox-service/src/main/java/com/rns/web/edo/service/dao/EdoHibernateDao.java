package com.rns.web.edo.service.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class EdoHibernateDao {

	Session session;
	
	public EdoHibernateDao(Session session) {
		this.session = session;
	}

	public <T> T getEntityByKey(Class<T> type, String key, Object value) {
		Criteria criteria = session.createCriteria(type).add(Restrictions.eq(key, value));
		Object result = criteria.uniqueResult();
		if (result != null) {
			return (T) result;
		}
		return null;
	}

	
}
