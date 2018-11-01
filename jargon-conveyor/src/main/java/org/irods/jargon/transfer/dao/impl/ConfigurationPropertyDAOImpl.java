package org.irods.jargon.transfer.dao.impl;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.irods.jargon.transfer.dao.ConfigurationPropertyDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.ConfigurationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * DAO for persistence of configuration properties. This is where preferences
 * and other data are stored.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Component
public class ConfigurationPropertyDAOImpl  implements
		ConfigurationPropertyDAO {

    /**
     * @return the sessionFactory
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * @param sessionFactory the sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

	private static final Logger log = LoggerFactory
			.getLogger(ConfigurationPropertyDAOImpl.class);
        
        @Autowired
        private SessionFactory sessionFactory;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.ConfigurationPropertyDAO#saveOrUpdate(org
	 * .irods.jargon.transfer.dao.domain.ConfigurationProperty)
	 */
	@Override
	public void saveOrUpdate(final ConfigurationProperty configurationProperty)
			throws TransferDAOException {
		log.info("entering save(ConfigurationProperty)");
		if (configurationProperty == null) {
			throw new IllegalArgumentException("null configurationProperty");
		}

		log.info("configurationProperty:{}", configurationProperty);

		if (configurationProperty.getPropertyKey() == null
				|| configurationProperty.getPropertyKey().isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty configuration property key");
		}

		log.info("see if prop already exists...");
		// see if prop already exists
		ConfigurationProperty prop = findByPropertyKey(configurationProperty
				.getPropertyKey());

		if (prop == null) {
			log.info("prop is new, go ahead and save");
			prop = configurationProperty;
			prop.setCreatedAt(new Date());
		} else {
			log.info("prop already found, update the value");
			prop.setPropertyValue(configurationProperty.getPropertyValue());
			prop.setUpdatedAt(new Date());
		}

		getSessionFactory().getCurrentSession().saveOrUpdate(prop);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.ConfigurationPropertyDAO#findById(java.
	 * lang.Long)
	 */
	@Override
	public ConfigurationProperty findById(final Long id)
			throws TransferDAOException {
		log.info("entering findById with id:{}", id);
		return (ConfigurationProperty) getSessionFactory().getCurrentSession()
				.get(ConfigurationProperty.class, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.ConfigurationPropertyDAO#findByPropertyKey
	 * (java.lang.String)
	 */
	@Override
	public ConfigurationProperty findByPropertyKey(final String propertyKey)
			throws TransferDAOException {
		if (propertyKey == null || propertyKey.isEmpty()) {
			throw new IllegalArgumentException("null or empty property key");
		}
		log.info("findByPropertyKey key=", propertyKey);
		try {
			Criteria criteria = getSessionFactory().getCurrentSession()
					.createCriteria(ConfigurationProperty.class);
			criteria.add(Restrictions.eq("propertyKey", propertyKey));
			return (ConfigurationProperty) criteria.uniqueResult();

		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error", e);
			throw new TransferDAOException("exception in findById", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.ConfigurationPropertyDAO#findAll()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<ConfigurationProperty> findAll() throws TransferDAOException {
		Criteria criteria = getSessionFactory().getCurrentSession()
				.createCriteria(ConfigurationProperty.class);

		return criteria.list();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.ConfigurationPropertyDAO#delete(org.irods
	 * .jargon.transfer.dao.domain.ConfigurationProperty)
	 */
	@Override
	public void delete(final ConfigurationProperty configurationProperty)
			throws TransferDAOException {
		try {

			getSessionFactory().getCurrentSession().delete(
					configurationProperty);
		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {

			log.error("error in delete(ConfigurationProperty)", e);
			throw new TransferDAOException(
					"Failed delete(ConfigurationProperty)", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.ConfigurationPropertyDAO#deleteAllProperties
	 * ()
	 */
	@Override
	public void deleteAllProperties() throws TransferDAOException {
		log.info("deleteAllProperties()");
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ConfigurationProperty as prop");

		log.debug("delete properties sql:{}", sb.toString());
                this.getSessionFactory().getCurrentSession().createQuery(sb.toString()).executeUpdate();
                log.info("done!");

	}

}
