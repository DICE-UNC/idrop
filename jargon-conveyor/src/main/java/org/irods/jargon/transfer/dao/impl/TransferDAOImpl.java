package org.irods.jargon.transfer.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.irods.jargon.transfer.dao.TransferDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferStateEnum;
import org.irods.jargon.transfer.dao.domain.TransferStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * 
 * @author jdr0887
 * 
 */
public class TransferDAOImpl extends HibernateDaoSupport implements TransferDAO {

	private static final Logger log = LoggerFactory
			.getLogger(TransferDAOImpl.class);

	public TransferDAOImpl() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.TransferDAO#save(org.irods.jargon
	 * .transfer.dao.domain.Transfer)
	 */
	@Override
	public void save(final Transfer transfer) throws TransferDAOException {
		logger.info("entering save(Transfer)");
		getSessionFactory().getCurrentSession().saveOrUpdate(transfer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.TransferDAO#merge(org.irods.jargon.transfer
	 * .dao.domain.Transfer)
	 */
	@Override
	public void merge(final Transfer transfer) throws TransferDAOException {
		logger.info("entering merge(Transfer)");
		getSessionFactory().getCurrentSession().merge(transfer);
	}

	@Override
	public Transfer initializeChildrenForTransfer(final Transfer transfer)
			throws TransferDAOException {
		log.info("initializeChildrenForTransfer");
		if (transfer == null) {
			throw new IllegalArgumentException("null transfer");
		}

		log.info("merging transfer");
		Transfer merged = (Transfer) getSessionFactory().getCurrentSession()
				.merge(transfer);

		for (TransferAttempt attempt : merged.getTransferAttempts()) {
			attempt.getAttemptStatus();

			/*
			 * for (TransferItem item : attempt.getTransferItems()) {
			 * item.getSourceFileAbsolutePath(); }
			 */

		}
		return merged;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.TransferDAO#findById(java.lang .Long)
	 */
	@Override
	public Transfer findById(final Long id) throws TransferDAOException {
		logger.debug("entering findById(Long)");
		return (Transfer) getSessionFactory().getCurrentSession().get(
				Transfer.class, id);
	}

	@Override
	public Transfer load(final Long id) throws TransferDAOException {
		logger.debug("entering load(Long)");
		return (Transfer) getSessionFactory().getCurrentSession().load(
				Transfer.class, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.TransferDAO#findInitializedById
	 * (java.lang.Long)
	 */
	@Override
	public Transfer findInitializedById(final Long id)
			throws TransferDAOException {
		logger.debug("entering findInitializedById(Long)");
		Transfer transfer = (Transfer) getSessionFactory().getCurrentSession()
				.get(Transfer.class, id);
		Hibernate.initialize(transfer);
		return transfer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.TransferDAO#findByTransferState
	 * (org.irods.jargon.transfer.dao.domain.TransferState[])
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Transfer> findByTransferState(
			final TransferStateEnum... transferState)
			throws TransferDAOException {
		log.debug("entering findByTransferState(TransferState...)");

		try {
			Criteria criteria = getSessionFactory().getCurrentSession()
					.createCriteria(Transfer.class);
			criteria.add(Restrictions.in("transferState", transferState));
			criteria.addOrder(Order.desc("createdAt"));
			// date instead?
			criteria.setFetchMode("synchronization", FetchMode.JOIN);
			return criteria.list();
		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error in findByTransferState(TransferState...)", e);
			throw new TransferDAOException(
					"Failed findByTransferState(TransferState...)", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.TransferDAO#findByTransferState (int,
	 * org.irods.jargon.transfer.dao.domain.TransferState[])
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Transfer> findByTransferState(final int maxResults,
			final TransferStateEnum... transferState)
			throws TransferDAOException {
		log.debug("entering findByTransferState(int, TransferState...)");
		try {
			Criteria criteria = getSessionFactory().getCurrentSession()
					.createCriteria(Transfer.class);
			criteria.add(Restrictions.in("transferState", transferState));
			criteria.setMaxResults(maxResults);
			criteria.addOrder(Order.desc("createdAt"));
			criteria.setFetchMode("synchronization", FetchMode.JOIN);
			return criteria.list();

		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error in findByTransferState(int, TransferState...)", e);
			throw new TransferDAOException(
					"Failed findByTransferState(int, TransferState...)", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.TransferDAO#findByTransferStatus (int,
	 * org.irods.jargon.transfer.dao.domain.TransferStatus[])
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Transfer> findByTransferStatus(final int maxResults,
			final TransferStatusEnum... transferStatus)
			throws TransferDAOException {
		log.debug("entering findByTransferState(int, TransferStatus...)");

		try {
			Criteria criteria = getSessionFactory().getCurrentSession()
					.createCriteria(Transfer.class);
			criteria.add(Restrictions.in("transferStatus", transferStatus));
			criteria.setFetchMode("synchronization", FetchMode.JOIN);
			criteria.setMaxResults(maxResults);
			criteria.addOrder(Order.desc("createdAt"));
			return criteria.list();
		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error in findByTransferState(int, TransferStatus...)", e);
			throw new TransferDAOException(
					"Failed findByTransferState(int, TransferStatus...)", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.TransferDAO#findAllSortedDesc (int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Transfer> findAllSortedDesc(final int maxResults)
			throws TransferDAOException {

		try {
			Criteria criteria = getSessionFactory().getCurrentSession()
					.createCriteria(Transfer.class);
			criteria.setMaxResults(maxResults);
			criteria.addOrder(Order.desc("createdAt"));
			criteria.setFetchMode("synchronization", FetchMode.JOIN);
			return criteria.list();
		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error in findAllSortedDesc(int)", e);
			throw new TransferDAOException("Failed findAllSortedDesc(int)", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.TransferDAO#findAll()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Transfer> findAll() throws TransferDAOException {
		log.debug("entering findAll()");
		try {
			Criteria criteria = getSessionFactory().getCurrentSession()
					.createCriteria(Transfer.class);
			criteria.addOrder(Order.desc("createdAt"));
			// criteria.setFetchMode("synchronization", FetchMode.JOIN);
			// criteria.setFetchMode("transferAttempts", FetchMode.JOIN);
			return criteria.list();
		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error in findAll()", e);
			throw new TransferDAOException("Failed findAll()", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.TransferDAO#purgeQueue()
	 */
	@Override
	public void purgeQueue() throws TransferDAOException {
		log.debug("entering purgeQueue()");

		try {

			StringBuilder sb = new StringBuilder();
			sb.append("delete from TransferItem as item where ");
			sb.append("item.transfer.id in (");
			sb.append("select id from Transfer as transfer where transfer.transferState <> ?)");

			log.debug("delete items sql:{}", sb.toString());

			HibernateTemplate hibernateTemplate = super.getHibernateTemplate();

			int rows = hibernateTemplate.bulkUpdate(sb.toString(),
					TransferStateEnum.PROCESSING);
			log.debug("deleted items count of: {}", rows);

			sb = new StringBuilder();
			sb.append("delete from Transfer  where transferState <> ?");

			log.debug("delete items sql:{}", sb.toString());

			rows = super.getHibernateTemplate().bulkUpdate(sb.toString(),
					TransferStateEnum.PROCESSING);
			log.debug("deleted transfers count of: {}", rows);

		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error in purgeQueue()", e);
			throw new TransferDAOException("Failed purgeQueue()", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.TransferDAO#purgeEntireQueue()
	 */
	@Override
	public void purgeEntireQueue() throws TransferDAOException {
		log.debug("entering purgeQueue()");

		try {

			HibernateTemplate hibernateTemplate = super.getHibernateTemplate();
			StringBuilder sb = new StringBuilder();
			sb.append("delete from TransferItem");
			log.debug("delete items sql:{}", sb.toString());

			int rows = hibernateTemplate.bulkUpdate(sb.toString());
			log.debug("deleted items count of: {}", rows);

			sb = new StringBuilder();
			sb.append("delete from TransferAttempt");
			rows = hibernateTemplate.bulkUpdate(sb.toString());
			log.debug("deleted attempts count of: {}", rows);

			sb = new StringBuilder();
			sb.append("delete from Transfer");

			log.debug("delete transfers sql:{}", sb.toString());

			rows = super.getHibernateTemplate().bulkUpdate(sb.toString());
			log.debug("deleted transfers count of: {}", rows);

		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error in purgeQueue()", e);
			throw new TransferDAOException("Failed purgeQueue()", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.TransferDAO#purgeSuccessful()
	 */
	@Override
	public void purgeSuccessful() throws TransferDAOException {
		log.debug("entering purgeSuccessful()");

		List<Transfer> transfers = findAll();

		for (Transfer transfer : transfers) {
			if ((transfer.getTransferState() == TransferStateEnum.COMPLETE || transfer
					.getTransferState() == TransferStateEnum.CANCELLED)
					&& (transfer.getLastTransferStatus() == TransferStatusEnum.OK)) {
				log.info("deleting...{}", transfer);

			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.TransferDAO#delete(org.irods.
	 * jargon.transfer.dao.domain.Transfer)
	 */
	@Override
	public void delete(final Transfer transfer) throws TransferDAOException {
		logger.debug("entering delete()");

		try {
			getSessionFactory().getCurrentSession().delete(transfer);
		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {

			log.error("error in delete(Transfer entity)", e);
			throw new TransferDAOException("Failed delete(Transfer entity)", e);
		}
	}

}
