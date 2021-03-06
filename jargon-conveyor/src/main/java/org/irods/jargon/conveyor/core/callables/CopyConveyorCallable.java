/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.irods.jargon.conveyor.core.callables;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callable for handling an iRODS copy operation
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public class CopyConveyorCallable extends AbstractConveyorCallable {

	private static final Logger log = LoggerFactory
			.getLogger(PutConveyorCallable.class);

	public CopyConveyorCallable(final TransferAttempt transferAttempt,
			final ConveyorService conveyorService) {
		super(transferAttempt, conveyorService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.core.callables.AbstractConveyorCallable#
	 * processCallForThisTransfer
	 * (org.irods.jargon.core.transfer.TransferControlBlock,
	 * org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	void processCallForThisTransfer(final TransferControlBlock tcb,
			final IRODSAccount irodsAccount) throws ConveyorExecutionException,
			JargonException {
		log.info("processCallForThisTransfer()");

		DataTransferOperations dataTransferOperationsAO = getIrodsAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dataTransferOperationsAO.copy(getTransferAttempt().getTransfer()
				.getIrodsAbsolutePath(), getTransferAttempt().getTransfer()
				.getGridAccount().getDefaultResource(), getTransferAttempt()
				.getTransfer().getLocalAbsolutePath(), this, tcb);

	}

}
