/**
 * 
 */
package org.irods.jargon.conveyor.core.callables;

import java.util.List;
import java.util.concurrent.Callable;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorExecutionFuture;
import org.irods.jargon.conveyor.core.ConveyorExecutorService.ErrorStatus;
import org.irods.jargon.conveyor.core.ConveyorRuntimeException;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.conveyor.core.FlowManagerService;
import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice.ExecResult;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Abstract super class for a transfer running process. This class, and its
 * collaborators, are responsible for:
 * <ul>
 * <li>Running the actual transfer process via Jargon</li>
 * <li>Intercepting any call-backs from the Jargon process, and making the
 * appropriate updates to the transfer accounting database</li>
 * <li>Catching any not-trapped errors and making a best effort to log, account
 * for them in the accounting database, and notifying the client if any
 * unresolved errors occur</li>
 * <li>Forwarding any call-backs to the client, such as file and intra-file
 * updates of transfers</li>
 * <li>Evaluating a transfer at completion for any errors</li>
 * <li>Modifying the error and running status of the execution queue when the
 * transfer completes or has errors</li>
 * </ul>
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Transactional(noRollbackFor = { JargonException.class }, rollbackFor = { ConveyorExecutionException.class })
public abstract class AbstractConveyorCallable implements
		Callable<ConveyorExecutionFuture>, TransferStatusCallbackListener {

	private final TransferAttempt transferAttempt;
	private final ConveyorService conveyorService;
	private TransferControlBlock transferControlBlock;
	private FlowCoProcessor flowCoProcessor;

	private static final Logger log = LoggerFactory
			.getLogger(AbstractConveyorCallable.class);

	private List<FlowSpec> candidateFlowSpecs = null;
	private FlowSpec selectedFlowSpec = null;

	/**
	 * Convenience method to get a <code>IRODSAccount</code> with a decrypted
	 * password
	 * 
	 * @param gridAccount
	 * @return
	 * @throws ConveyorExecutionException
	 */
	IRODSAccount getIRODSAccountForGridAccount(final GridAccount gridAccount)
			throws ConveyorExecutionException {

		log.info("getIRODSAccountForGridAccount()");
		if (gridAccount == null) {
			throw new IllegalArgumentException("null gridAccount");
		}

		return getConveyorService().getGridAccountService()
				.irodsAccountForGridAccount(gridAccount);
	}

	/**
	 * Convenience method to get the <code>IRODSAccessObjectFactory</code>
	 * 
	 * @return
	 */
	IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return conveyorService.getIrodsAccessObjectFactory();
	}

	/**
	 * 
	 * Default constructor takes required hooks for bi-directional communication
	 * with caller of the transfer processor
	 * 
	 * @param transfer
	 * @param conveyorService
	 */
	public AbstractConveyorCallable(final TransferAttempt transferAttempt,
			final ConveyorService conveyorService) {

		if (transferAttempt == null) {
			throw new IllegalArgumentException("null transferAttempt");
		}

		if (conveyorService == null) {
			throw new IllegalArgumentException("null conveyorService");
		}

		this.transferAttempt = transferAttempt;
		this.conveyorService = conveyorService;

	}

	/**
	 * This method is to be implemented by each specific transfer subclass. This
	 * is wrapped in the <code>call()</code> method of this abstract superclass,
	 * so that this superclass can wrap the call with appropriate
	 * setup/teardown, as well as error handling.
	 * 
	 * @param tcb
	 *            {@link TransferControlBlock} that contains transfer options
	 *            and other information shared with the underlying transfer
	 * @param irodsAccount
	 *            {@link IRODSAccount} that has been resolved for this operation
	 * 
	 * @throws ConveyorExecutionException
	 *             for errors in conveyor processing
	 * @throws JargonException
	 *             for errors in actual jargon-irods processing
	 */
	abstract void processCallForThisTransfer(final TransferControlBlock tcb,
			final IRODSAccount irodsAccount) throws ConveyorExecutionException,
			JargonException;

	/**
	 * Call method will invoke the <code>processCall</code> method to be
	 * implemented in each subclass. This will contain the specific code for
	 * each type of operation, and wrap it in error checking and other common
	 * processing.
	 * <p/>
	 * Note that any type of error is trapped in the catch, and conveyor will
	 * attempt to log these errors as part of the transfer attempt, so that the
	 * database reflects any issues at all with how the transfers are managed.
	 * It is possible that conveyor itself will have errors and be unable to
	 * flag these in the database, in which case it tries to log these and throw
	 * an exception, but in this case something is really wrong.
	 * <p/>
	 * The actual transfers are done in the callable, and if any sort of
	 * 'normal' iRODS or Jargon errors occur, these are not thrown from Jargon,
	 * because there is a registered callback listener. If the callback listener
	 * is present, Jargon will not throw an error from iRODS work, instead it
	 * puts the error in the callback, and normally, conveyor sees these
	 * callbacks and logs the errors in the conveyor database.
	 * 
	 * @throws <code>ConveyorExecutionException</code> if something really bad
	 *         happens. Mostly, any errors should be trapped and stuck in the
	 *         database for the transfer. Errors should only be thrown if
	 *         something goes wrong with that processing, and in that case
	 *         something is pretty much messed up anyhow. In other words if you
	 *         get an exception to throw here, something is broken with the
	 *         framework itself, and the framework is giving up.
	 * 
	 */
	@Override
	public final ConveyorExecutionFuture call()
			throws ConveyorExecutionException {

		IRODSAccount irodsAccount = null;
		try {
			synchronized (this) {
				irodsAccount = getConveyorService().getGridAccountService()
						.irodsAccountForGridAccount(
								transferAttempt.getTransfer().getGridAccount());
				setTransferControlBlock(buildDefaultTransferControlBlock());
			}

			/*
			 * Note that end of transfer success/failure processing will be
			 * triggered by the overall status callback from Jargon. That
			 * overall status callback will cause the queue to be released and
			 * final statuses to be updated.
			 * 
			 * The exception handling below is meant to trap 'out of band' or
			 * unanticipated exceptions, and signals that the conveyor service
			 * itself is fubar. That should be an unlikely occasion, and in that
			 * case it just shoots a flare and it's up to the client of the
			 * framework to handle things.
			 * 
			 * Exceptions that occur in the transfer process (irods errors,
			 * security errors, client errors, network errors) should be
			 * handled, not by thrown exceptions, but by error callbacks from
			 * the jargon transfer processes, and are not caught below.
			 */

			retrieveCandidateFlowSpecs();
			/*
			 * initialize the flow co processor. This is done here to avoid
			 * leaking a this ref from the constructor
			 */
			flowCoProcessor = new FlowCoProcessor(this);
			processCallForThisTransfer(transferControlBlock, irodsAccount);
			return new ConveyorExecutionFuture();
		} catch (JargonException je) {

			if (getTransferControlBlock().isCancelled()) {
				log.info("cancelled, return future and proceed");
				return new ConveyorExecutionFuture();
			}

			log.info(
					"jargon exception processing transfer, mark the transfer as an error and release the queue",
					je);

			/*
			 * The following will call doCompletionSequence() in the finally to
			 * release the queue
			 */
			markTransferAsAnExceptionWhenProcessingCall(je);
			return new ConveyorExecutionFuture();

		} catch (JargonRuntimeException je) {

			if (getTransferControlBlock().isCancelled()) {
				log.info("cancelled, return future and proceed");
				return new ConveyorExecutionFuture();
			}

			log.info(
					"jargon runtime exception processing transfer, mark the transfer as an error and release the queue",
					je);

			/*
			 * The following will call doCompletionSequence() in the finally to
			 * release the queue
			 */
			markTransferAsAnExceptionWhenProcessingCall(je);
			return new ConveyorExecutionFuture();

		} catch (Exception ex) {

			if (getTransferControlBlock().isCancelled()) {
				log.info("cancelled, return future and proceed");
				return new ConveyorExecutionFuture();
			}

			log.error(
					"*********** unanticipated exception occurred  *************",
					ex);
			reportConveyerExceptionDuringProcessing(ex);
			throw new ConveyorExecutionException(
					"unhandled exception during transfer process", ex);
		}
	}

	/**
	 * When an exception occurs setting up the transfer in the call, mark the
	 * transfer as an exception and set error statuses. If the transfer cannot
	 * be updated, bubble the exception back up to the callback listener.
	 * 
	 * @param je
	 *            {@link Exception} that should be handled in the transfer
	 */
	private void markTransferAsAnExceptionWhenProcessingCall(final Exception je) {
		log.info("markTransferAsAnExceptionWhenProcessingCall");
		try {
			getConveyorService().getTransferAccountingManagementService()
					.updateTransferAttemptWithConveyorException(
							transferAttempt, je);
		} catch (ConveyorExecutionException e) {
			log.error(
					"*********** unanticipated exception occurred  *************",
					e);
			getConveyorService().getConveyorCallbackListener()
					.signalUnhandledConveyorException(e);

		} catch (Exception e) {
			log.error(
					"*********** unanticipated exception occurred  *************",
					e);
			getConveyorService().getConveyorCallbackListener()
					.signalUnhandledConveyorException(e);

		} finally {
			doCompletionSequence();
		}

	}

	/**
	 * @return the transferAttempt
	 */
	public TransferAttempt getTransferAttempt() {
		return transferAttempt;
	}

	/**
	 * @return the conveyorService
	 */
	public ConveyorService getConveyorService() {
		return conveyorService;
	}

	/**
	 * Get the <code>TransferControlBlock</code> that will control this
	 * transfer, based on configuration
	 * 
	 * @return {@link TransferControlBlock}
	 * @throws ConveyorExecutionException
	 */
	protected TransferControlBlock buildDefaultTransferControlBlock()
			throws ConveyorExecutionException {
		return conveyorService.getConfigurationService()
				.buildDefaultTransferControlBlockBasedOnConfiguration(
						transferAttempt.getLastSuccessfulPath(),
						getIrodsAccessObjectFactory());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.transfer.TransferStatusCallbackListener#statusCallback
	 * (org.irods.jargon.core.transfer.TransferStatus)
	 */
	@Override
	public FileStatusCallbackResponse statusCallback(
			final TransferStatus transferStatus) throws JargonException {
		log.info("status callback:{}", transferStatus);

		FileStatusCallbackResponse response = FileStatusCallbackResponse.CONTINUE;

		try {
			if (transferStatus.getTransferState() == TransferState.SUCCESS
					|| transferStatus.getTransferState() == TransferStatus.TransferState.IN_PROGRESS_COMPLETE_FILE) {

				if (selectedFlowSpec != null) {
					log.info("processing post-file flow");
					ExecResult execResult = flowCoProcessor
							.executePostFileChain(selectedFlowSpec,
									transferStatus);

					if (execResult == ExecResult.ABORT_AND_TRIGGER_ANY_ERROR_HANDLER) {
						flowCoProcessor
								.executeAnyFailureMicroservice(selectedFlowSpec);
					} else if (execResult == ExecResult.CANCEL_OPERATION) {
						// cancel is set in coprocessor code
						log.info("cancelling...");

					} else if (execResult != ExecResult.CONTINUE) {
						log.error(
								"unsupported response for a pre-file operation:{}",
								execResult);
						throw new ConveyorExecutionException(
								"unsupported response for a pre-file operation");
					}
				}

				// FIXME: how is status updated by a skip, etc? should hti sonly
				// be for a continue or skip rest of flow?
				updateTransferStateOnFileCompletion(transferStatus);

			} else if (transferStatus.getTransferState() == TransferState.IN_PROGRESS_START_FILE) {
				log.info("file initiation, this is just passed on by conveyor");

				if (selectedFlowSpec != null) {
					log.info("processing pre-file flow");
					ExecResult execResult = flowCoProcessor
							.executePreFileChain(selectedFlowSpec,
									transferStatus);

					if (execResult == ExecResult.ABORT_AND_TRIGGER_ANY_ERROR_HANDLER) {
						flowCoProcessor
								.executeAnyFailureMicroservice(selectedFlowSpec);
					} else if (execResult == ExecResult.CANCEL_OPERATION) {
						// cancel is set in coprocessor code
						log.info("cancelling...");

					} else if (execResult == ExecResult.SKIP_THIS_FILE) {
						// FIXME: skip file processing?
						updateTransferStateOnRestartFile(transferStatus);

						// need to decide whether to just skip out and update
						// here, or whether to let jargon core do it and call
						// back again?
						response = FileStatusCallbackResponse.SKIP;
					} else if (execResult != ExecResult.CONTINUE) {
						log.error(
								"unsupported response for a pre-file operation:{}",
								execResult);
						throw new ConveyorExecutionException(
								"unsupported response for a pre-file operation");
					}
				}

			} else if (transferStatus.getTransferState() == TransferState.RESTARTING) {
				updateTransferStateOnRestartFile(transferStatus);

			} else if (transferStatus.getTransferState() == TransferState.FAILURE) {
				/*
				 * create failure item get exception from callback and add to
				 * item
				 */
				/*
				 * Treat as a warning for now, an error will be signaled when
				 * more than the threshold number of file errors occurs
				 */
				getConveyorService().getConveyorExecutorService()
						.setErrorStatus(ErrorStatus.WARNING);
				getConveyorService().getTransferAccountingManagementService()
						.updateTransferAfterFailedFileTransfer(transferStatus,
								getTransferAttempt(),
								transferControlBlock.getErrorCount());
			}

		} catch (ConveyorExecutionException ex) {
			getConveyorService().getConveyorExecutorService().setErrorStatus(
					ErrorStatus.ERROR);
			getConveyorService().getConveyorCallbackListener()
					.signalUnhandledConveyorException(ex);
			throw new JargonException(ex.getMessage(), ex.getCause());
		}

		if (conveyorService.getConveyorCallbackListener() != null) {
			conveyorService.getConveyorCallbackListener().statusCallback(
					transferStatus);
		}

		return response;

	}

	/**
	 * A restart file has been encountered. Do the proper updates to the
	 * transfer attempt and optionally log the restart of this file
	 * 
	 * @param transferStatus
	 * @throws ConveyorExecutionException
	 */
	private void updateTransferStateOnRestartFile(
			final TransferStatus transferStatus)
			throws ConveyorExecutionException {
		getConveyorService().getTransferAccountingManagementService()
				.updateTransferAfterRestartFileSkipped(transferStatus,
						getTransferAttempt());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.TransferStatusCallbackListener#
	 * overallStatusCallback(org.irods.jargon.core.transfer.TransferStatus)
	 */
	@Override
	public void overallStatusCallback(final TransferStatus transferStatus)
			throws JargonException {
		log.info("overall status callback signalling completion:{}",
				transferStatus);
		boolean doComplete = false;
		try {
			if (transferStatus.getTransferState() == TransferStatus.TransferState.OVERALL_INITIATION) {
				// potentially handle any pre proc for operation flow specs
				log.info("handle overall initiation");
				handleOverallInitiation(transferStatus);
			} else if (transferStatus.getTransferState() == TransferStatus.TransferState.OVERALL_COMPLETION) {
				log.info("overall completion...updating status of transfer...");
				doComplete = true;
				processOverallCompletionOfTransfer(transferStatus);
			} else if (transferStatus.getTransferState() == TransferStatus.TransferState.SYNCH_COMPLETION) {
				log.info("overall completion of synch...updating status of transfer...");
				doComplete = true;
				processOverallCompletionOfTransfer(transferStatus);
			} else if (transferStatus.getTransferState() == TransferStatus.TransferState.FAILURE) {
				log.error("failure to transfer in status");
				doComplete = true;
				processOverallCompletionOfTransferWithFailure(transferStatus);
			} else if (transferStatus.getTransferState() == TransferState.CANCELLED) {
				doComplete = true;
				log.error("transfer cancelled, this will be handled by the conveyor execution service, and the callback here will be ignored");
				processOverallCompletionOfTransferWithCancel(transferStatus);
			} else {
				log.info("unhandled transfer status of:{}", transferStatus);
			}
		} catch (ConveyorExecutionException ex) {
			log.error(
					"conveyorExecutionException during handling of overall status callback:{}",
					transferStatus, ex);
			doComplete = true;
			throw new JargonException(ex.getMessage(), ex.getCause());
		} finally {
			if (conveyorService.getConveyorCallbackListener() != null) {
				log.info("sending overall status callback to client");
				conveyorService.getConveyorCallbackListener()
						.overallStatusCallback(transferStatus);
			}

			/*
			 * signal a completion if it's not a callback that is informational
			 * or indicates startup
			 */
			if (doComplete) {
				doCompletionSequence();
			}

		}

	}

	/**
	 * Handle an overall initiation of a transfer operation, signaling any
	 * flows, etc
	 * 
	 * @param transferStatus
	 * @throws ConveyorExecutionException
	 */
	private void handleOverallInitiation(final TransferStatus transferStatus)
			throws ConveyorExecutionException {
		log.info("handleOverallInitiation()");
		if (getCandidateFlowSpecs().isEmpty()) {
			log.info("No flows to inspect");
			return;
		}

		// need to loop thru and run a condition on candidate flows

		for (FlowSpec flowSpec : candidateFlowSpecs) {
			boolean runFlow = flowCoProcessor.evaluateCondition(flowSpec,
					transferStatus);
			if (runFlow) {
				log.info("found a flow:{}", runFlow);
				selectedFlowSpec = flowSpec;
			}
		}

		/*
		 * If I have a flow spec, run the pre op chain, which will be normal,
		 * will cause a cancellation, or may abort with an error
		 */
		if (selectedFlowSpec != null) {
			log.info("have a flow spec, run any pre flow chain");
			ExecResult overallResult = flowCoProcessor
					.executePreOperationChain(selectedFlowSpec, transferStatus);
			log.info("overall result of pre-flow chain is:{}", overallResult);
		}

	}

	@Override
	public CallbackResponse transferAsksWhetherToForceOperation(
			final String irodsAbsolutePath, final boolean isCollection) {
		log.info("transferAsksWhetherToForceOperation");
		return CallbackResponse.YES_FOR_ALL;
	}

	/**
	 * Called by callable methods when an unexpected exception occurs in
	 * conveyor processing of the transfer, rather than an error occurring while
	 * the transfer operation is in progress. In other words, when the callable
	 * is setting up the transfer, updating the queue manager, trying to launch
	 * the transfer process, any error that is not signaled by a callback from
	 * Jargon is treated as a conveyor processing error, and the transfer is
	 * marked as an error with a global message.
	 * <p/>
	 * Note that calling this method will unlock the conveyor execution queue
	 * 
	 * @param ex
	 *            <code>Exception</code> that was caught while trying to process
	 *            the transfer
	 * @throws ConveyorExecutionException
	 */
	void reportConveyerExceptionDuringProcessing(final Exception ex)
			throws ConveyorExecutionException {
		log.warn("reportConveyerExceptionDuringProcessing() is called");
		Exception myException = null;

		/*
		 * Overkill check just to narrow the kind of errors that I'm processing
		 */

		if (ex == null) {
			myException = new ConveyorExecutionException(
					"warning!  An exception was reported but null was provided to the reportConveyerExceptionDuringProcessing() method");
		} else {
			log.info("reported exception:", ex);
			myException = ex;
		}

		log.info("updating transfer attempt with an exception");
		try {
			getConveyorService().getTransferAccountingManagementService()
					.updateTransferAttemptWithConveyorException(
							transferAttempt, myException);

		} catch (Exception e) {
			/*
			 * I've got an exception but cannot update the database with it. As
			 * a last step, log it, then try to signal the callback listener
			 * that some error has occurred
			 */
			log.error("*************  exception occurred in conveyor framework,unable to update conveyor database*****  will signal the callback listener");
			getConveyorService().getConveyorCallbackListener()
					.signalUnhandledConveyorException(e);
			getConveyorService().getConveyorExecutorService().setErrorStatus(
					ErrorStatus.ERROR);
			throw new ConveyorRuntimeException(
					"unprocessable exception in conveyor, not updated in database",
					e);
		} finally {
			log.info("aftar all possible error handling and notification, I am releasing the queue");
			doCompletionSequence();
		}
	}

	/**
	 * Whether in an error state, or in a successfully completed state, release
	 * the execution queue and attempt to trigger any subsequent operations.
	 * <p/>
	 * Note that any error in dequeuing the next transfer is logged, and the
	 * callback listener will be notified.
	 */
	private void doCompletionSequence() {
		log.info("doCompletionSequence()");
		getConveyorService().getConveyorExecutorService()
				.setOperationCompleted();
		log.info("signaling completion so queue manager can dequeue next");

		try {
			log.info("calling dequeue of next");
			getConveyorService().getQueueManagerService()
					.dequeueNextOperation();
		} catch (ConveyorExecutionException e) {
			log.error(
					"unable to dequeue, will send an exception back to listener",
					e);
			conveyorService.getConveyorCallbackListener()
					.signalUnhandledConveyorException(e);
			conveyorService.getConveyorExecutorService().setErrorStatus(
					ErrorStatus.ERROR);
		}
	}

	/**
	 * Given a transfer status, update the transfer state on completion of
	 * processing a file
	 * 
	 * @param transferStatus
	 * @throws ConveyorExecutionException
	 */
	private void updateTransferStateOnFileCompletion(
			final TransferStatus transferStatus)
			throws ConveyorExecutionException {
		getConveyorService().getTransferAccountingManagementService()
				.updateTransferAfterSuccessfulFileTransfer(transferStatus,
						getTransferAttempt());
	}

	/**
	 * A complete with success callback for an entire transfer operation, make
	 * the necessary updates
	 * 
	 * @param transferStatus
	 * @throws ConveyorExecutionException
	 */
	protected void processOverallCompletionOfTransfer(
			final TransferStatus transferStatus)
			throws ConveyorExecutionException {
		log.info("processOverallCompletionOfTransfer default version in AbstractConveyorCallable");

		log.info("evaluating transfer status by inspecting items for any file level errors");
		TransferStatusEnum evaluatedStatus = evaluateTransferErrorsInItemsToSetOverallStatus(transferAttempt);

		log.info("status was:{}", evaluatedStatus);

		if (evaluatedStatus == TransferStatusEnum.OK) {
			log.info("evaluated status is OK");
			if (getTransferControlBlock().getTotalFilesTransferredSoFar() == 0) {
				conveyorService.getConveyorExecutorService().setErrorStatus(
						ErrorStatus.WARNING);
				getConveyorService().getTransferAccountingManagementService()
						.updateTransferAfterOverallWarningNoFilesTransferred(
								transferStatus, transferAttempt);
			} else {
				conveyorService.getConveyorExecutorService().setErrorStatus(
						ErrorStatus.OK);
				getConveyorService().getTransferAccountingManagementService()
						.updateTransferAfterOverallSuccess(transferStatus,
								getTransferAttempt());
			}
		} else if (evaluatedStatus == TransferStatusEnum.WARNING) {
			conveyorService.getConveyorExecutorService().setErrorStatus(
					ErrorStatus.WARNING);
			getConveyorService().getTransferAccountingManagementService()
					.updateTransferAfterOverallWarningByFileErrorThreshold(
							transferStatus, getTransferAttempt());
		} else if (evaluatedStatus == TransferStatusEnum.ERROR) {
			conveyorService.getConveyorExecutorService().setErrorStatus(
					ErrorStatus.ERROR);
			getConveyorService().getTransferAccountingManagementService()
					.updateTransferAfterOverallFailureByFileErrorThreshold(
							transferStatus, getTransferAttempt());

		}
	}

	/**
	 * Handle a cancel from the overall status
	 * 
	 * @param transferStatus
	 * @throws ConveyorExecutionException
	 */
	private void processOverallCompletionOfTransferWithCancel(
			final TransferStatus transferStatus)
			throws ConveyorExecutionException {
		log.info("processOverallCompletionOfTransferWithFailure");
		conveyorService.getConveyorExecutorService().setErrorStatus(
				ErrorStatus.OK);
		getConveyorService().getTransferAccountingManagementService()
				.updateTransferAfterCancellation(transferAttempt);
	}

	/**
	 * Look at all of the items in the given transfer, and decide whether it's
	 * OK, or an ERROR or WARNING based on any file by file errors, and whether
	 * those errors are above or below the error threshold
	 * 
	 * @param transferAttempt
	 * @return
	 */
	protected TransferStatusEnum evaluateTransferErrorsInItemsToSetOverallStatus(
			final TransferAttempt transferAttempt) {

		assert transferAttempt != null;

		TransferStatusEnum status;
		if (transferControlBlock.getErrorCount() > 0
				&& transferControlBlock.getTotalFilesTransferredSoFar() == 0) {
			log.info("transfer had errors and no files were successful, so mark as an error");
			status = TransferStatusEnum.ERROR;
		} else if (transferControlBlock.getErrorCount() > 0
				&& transferControlBlock.getErrorCount() < transferControlBlock
						.getMaximumErrorsBeforeCanceling()) {
			log.info("transfer had errors but below max");
			status = TransferStatusEnum.WARNING;
		} else if (transferControlBlock.getErrorCount() > 0) {
			log.info("transfer had errors but below max");
			status = TransferStatusEnum.ERROR;
		} else {
			log.info("transfer had no errors");
			status = TransferStatusEnum.OK;
		}

		return status;
	}

	/**
	 * When a transfer is done, and an overall status callback of failure has
	 * occurred, update the transfer attempt to reflect this error status
	 * 
	 * @param transferStatus
	 * @throws ConveyorExecutionException
	 */
	protected void processOverallCompletionOfTransferWithFailure(
			final TransferStatus transferStatus)
			throws ConveyorExecutionException {
		log.info("processOverallCompletionOfTransferWithFailure");
		conveyorService.getConveyorExecutorService().setErrorStatus(
				ErrorStatus.ERROR);
		getConveyorService().getTransferAccountingManagementService()
				.updateTransferAfterOverallFailure(transferStatus,
						transferAttempt);

	}

	/**
	 * @return the transferControlBlock, note that this may be <code>null</code>
	 *         if it has not been initialized yet. The initialization is done in
	 *         the <code>call()</code> method.
	 */
	public synchronized TransferControlBlock getTransferControlBlock() {
		return transferControlBlock;
	}

	/**
	 * @param transferControlBlock
	 *            the transferControlBlock to set
	 */
	public synchronized void setTransferControlBlock(
			final TransferControlBlock transferControlBlock) {
		this.transferControlBlock = transferControlBlock;
	}

	/**
	 * Convenience method to get the <code>Transfer</code> associated with this
	 * callable
	 * 
	 * @return {@link Transfer}
	 */
	protected Transfer getTransfer() {
		return getTransferAttempt().getTransfer();
	}

	/**
	 * On initiation of a transfer, get any candidate flow specs, selected based
	 * on properties of the transfer. These will later be evaluated based on
	 * conditions within the <code>FlowSpec</code>
	 * 
	 * @return <code>List</code> of {@link FlowSpec} that are candidates for
	 *         this transfer attempt. This reflects the flow specs that are
	 *         cached for this instance
	 * 
	 * @throws ConveyorExecutionException
	 */
	private List<FlowSpec> retrieveCandidateFlowSpecs()
			throws ConveyorExecutionException {
		log.info("retrieveCandidateFlowSpecs()");

		FlowManagerService flowManagerService = conveyorService
				.getFlowManagerService();

		candidateFlowSpecs = flowManagerService
				.retrieveCandidateFlowSpecs(getTransferAttempt());

		return candidateFlowSpecs;

	}

	/**
	 * @return the candidateFlowSpecs that are cached, and this will cache them
	 *         if they have not been obtained already
	 * @throws ConveyorExecutionException
	 */
	protected synchronized List<FlowSpec> getCandidateFlowSpecs()
			throws ConveyorExecutionException {
		if (candidateFlowSpecs == null) {
			retrieveCandidateFlowSpecs();
		}
		return candidateFlowSpecs;
	}

	/**
	 * @param candidateFlowSpecs
	 *            the candidateFlowSpecs to set
	 */
	protected synchronized void setCandidateFlowSpecs(
			final List<FlowSpec> candidateFlowSpecs) {
		this.candidateFlowSpecs = candidateFlowSpecs;
	}

	/**
	 * @return the selectedFlowSpec
	 */
	protected FlowSpec getSelectedFlowSpec() {
		return selectedFlowSpec;
	}

	/**
	 * @return the flowCoProcessor
	 */
	FlowCoProcessor getFlowCoProcessor() {
		return flowCoProcessor;
	}

}
