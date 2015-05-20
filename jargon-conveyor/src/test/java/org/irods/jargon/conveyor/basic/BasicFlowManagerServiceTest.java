package org.irods.jargon.conveyor.basic;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;
import org.irods.jargon.conveyor.flowmanager.flow.FlowSpecCacheService;
import org.irods.jargon.conveyor.flowmanager.flow.Selector.FlowActionEnum;
import org.irods.jargon.conveyor.flowmanager.flow.dsl.Flow;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.junit.Test;
import org.mockito.Mockito;

public class BasicFlowManagerServiceTest {

	@Test
	public void testRetrieveCandidateFlowSpecs()
			throws ConveyorExecutionException {
		String host = "test";
		String zone = "zone";
		FlowActionEnum action = FlowActionEnum.GET;

		String fqcn = Microservice.class.getName();

		FlowSpec flow = Flow.define().forAction(action).forHost(host)
				.forZone(zone).onAllConditions().endPreOperationChain()
				.endPreFileChain().addPostFileMicroservice(fqcn)
				.endPostFileChain().endPostOperationChain()
				.endFlowWithoutErrorHandler();

		List<FlowSpec> candidates = new ArrayList<FlowSpec>();
		candidates.add(flow);

		FlowSpecCacheService flowSpecCacheService = Mockito
				.mock(FlowSpecCacheService.class);
		Mockito.when(flowSpecCacheService.getFlowSpecs())
				.thenReturn(candidates);

		ConveyorService conveyorService = Mockito.mock(ConveyorService.class);
		BasicFlowManagerService flowManagerService = new BasicFlowManagerService();
		flowManagerService.setFlowSpecCacheService(flowSpecCacheService);
		flowManagerService.setConveyorService(conveyorService);

		Transfer transfer = new Transfer();
		GridAccount gridAccount = new GridAccount();
		gridAccount.setHost(host);
		gridAccount.setZone(zone);
		transfer.setTransferType(TransferType.GET);
		transfer.setGridAccount(gridAccount);
		TransferAttempt transferAttempt = new TransferAttempt();
		transferAttempt.setTransfer(transfer);

		List<FlowSpec> flowSpecs = flowManagerService
				.retrieveCandidateFlowSpecs(transferAttempt);
		Assert.assertFalse("did not get the flowSpec", flowSpecs.isEmpty());

	}

	@Test
	public void testRetrieveCandidateFlowSpecsWrongHost()
			throws ConveyorExecutionException {
		String host = "test";
		String zone = "zone";
		FlowActionEnum action = FlowActionEnum.GET;

		String fqcn = Microservice.class.getName();

		FlowSpec flow = Flow.define().forAction(action).forHost(host)
				.forZone(zone).onAllConditions().endPreOperationChain()
				.endPreFileChain().addPostFileMicroservice(fqcn)
				.endPostFileChain().endPostOperationChain()
				.endFlowWithoutErrorHandler();

		List<FlowSpec> candidates = new ArrayList<FlowSpec>();
		candidates.add(flow);

		FlowSpecCacheService flowSpecCacheService = Mockito
				.mock(FlowSpecCacheService.class);
		Mockito.when(flowSpecCacheService.getFlowSpecs())
				.thenReturn(candidates);

		ConveyorService conveyorService = Mockito.mock(ConveyorService.class);
		BasicFlowManagerService flowManagerService = new BasicFlowManagerService();
		flowManagerService.setFlowSpecCacheService(flowSpecCacheService);
		flowManagerService.setConveyorService(conveyorService);

		Transfer transfer = new Transfer();
		GridAccount gridAccount = new GridAccount();
		gridAccount.setHost("xxxx");
		gridAccount.setZone(zone);
		transfer.setTransferType(TransferType.GET);
		transfer.setGridAccount(gridAccount);
		TransferAttempt transferAttempt = new TransferAttempt();
		transferAttempt.setTransfer(transfer);

		List<FlowSpec> flowSpecs = flowManagerService
				.retrieveCandidateFlowSpecs(transferAttempt);
		Assert.assertTrue("should not be matching flow specs",
				flowSpecs.isEmpty());

	}
}
