/**
 * 
 */
package org.irods.jargon.conveyor.core;

import java.io.File;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.conveyor.unittest.utils.TransferTestRunningUtilities;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.transfer.dao.domain.ConfigurationProperty;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferStateEnum;
import org.irods.jargon.transfer.dao.domain.TransferStatusEnum;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

/**
 * Functional tests for conveyor service
 * 
 * @author Mike Conway - DICE (www.irods.org) see
 *         https://code.renci.org/gf/project/jargon/
 * 
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:transfer-dao-beans.xml",
		"classpath:transfer-dao-hibernate-spring.cfg.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
public class ConveyorExecutorServiceImplFunctionalTest {
	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static org.irods.jargon.testutils.filemanip.ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "ConveyorExecutorServiceImplFunctionalTest";
	private static org.irods.jargon.testutils.IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem = null;
	private static int TRANSFER_TIMEOUT = -1;

	@Autowired
	private ConveyorService conveyorService;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		irodsFileSystem = IRODSFileSystem.instance();
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new org.irods.jargon.testutils.filemanip.ScratchFileUtils(
				testingProperties);
		scratchFileUtils
				.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new org.irods.jargon.testutils.IRODSTestSetupUtilities();
		irodsTestSetupUtilities.clearIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities
				.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
	}

	@Before
	public void setUp() throws Exception {
		conveyorService.setIrodsAccessObjectFactory(irodsFileSystem
				.getIRODSAccessObjectFactory());
		conveyorService.getQueueManagerService().purgeAllFromQueue();
		conveyorService.getConveyorExecutorService().setOperationCompleted();
		conveyorService.getGridAccountService().resetPassPhraseAndAccounts();

	}

	@After
	public void tearDown() throws Exception {
		conveyorService.getQueueManagerService().purgeAllFromQueue();
	}

	/**
	 * Do a put, cancel partway through
	 * 
	 * @throws Exception
	 */
	@Ignore
	// FIXME: check this one out later, hibernate lock error? Might be timing...
	public void testPutWithCancellation() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		conveyorService.validatePassPhrase(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PASSWORD_KEY));
		conveyorService.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);
		ConfigurationProperty logSuccessful = new ConfigurationProperty();
		logSuccessful
				.setPropertyKey(ConfigurationPropertyConstants.LOG_SUCCESSFUL_FILES_KEY);
		logSuccessful.setPropertyValue("true");

		conveyorService.getConfigurationService().addConfigurationProperty(
				logSuccessful);

		String rootCollection = "testEnqueuePutTransferOperationAndWaitUntilDone";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testEnqueuePutTransferOperationAndWaitUntilDone", 2,
						5, 2, "testFile", ".txt", 10, 5, 100, 2000);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		File localFile = new File(localCollectionAbsolutePath);

		Transfer transfer = new Transfer();
		transfer.setIrodsAbsolutePath(destFile.getAbsolutePath());
		transfer.setLocalAbsolutePath(localFile.getAbsolutePath());
		transfer.setTransferType(TransferType.PUT);

		conveyorService.getQueueManagerService().enqueueTransferOperation(
				transfer, irodsAccount);

		TransferTestRunningUtilities.waitForTransferToTransferNFiles(
				conveyorService, TRANSFER_TIMEOUT, 15);
		conveyorService.getConveyorExecutorService().requestCancel(
				conveyorService.getConveyorExecutorService()
						.getCurrentTransferAttempt());

		List<Transfer> transfers = conveyorService.getQueueManagerService()
				.listAllTransfersInQueue();
		Assert.assertFalse("no transfers in queue", transfers.isEmpty());
		Assert.assertEquals("should be 1 transfer..maybe test cleanup is bad",
				1, transfers.size());
		transfer = transfers.get(0);

		transfer = conveyorService.getQueueManagerService()
				.initializeGivenTransferByLoadingChildren(transfer);

		Assert.assertFalse("did not create a transfer attempt", transfer
				.getTransferAttempts().isEmpty());

		Assert.assertEquals("did not get complete status",
				TransferStateEnum.CANCELLED, transfer.getTransferState());

		TransferAttempt attempts[] = new TransferAttempt[transfer
				.getTransferAttempts().size()];
		attempts = transfer.getTransferAttempts().toArray(attempts);
		Assert.assertEquals("should be 1 attempt", 1, attempts.length);

		TransferAttempt attempt = attempts[0];
		Assert.assertNotNull("transfer attempt not persisted", attempt.getId());
		Assert.assertEquals("did not set cancelled message",
				TransferAccountingManagementService.WARNING_CANCELLED_MESSAGE,
				attempt.getErrorMessage());
	}

	/**
	 * do a get of a nested collection that should be 'normal'
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetNestedCollection() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		conveyorService.validatePassPhrase(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PASSWORD_KEY));
		conveyorService.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);
		ConfigurationProperty logSuccessful = new ConfigurationProperty();
		logSuccessful
				.setPropertyKey(ConfigurationPropertyConstants.LOG_SUCCESSFUL_FILES_KEY);
		logSuccessful.setPropertyValue("true");

		conveyorService.getConfigurationService().addConfigurationProperty(
				logSuccessful);

		String rootCollection = "testGetWithCancellation";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String localCollectionReturnAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection + "return");

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testEnqueuePutTransferOperationAndWaitUntilDone", 2,
						5, 2, "testFile", ".txt", 10, 5, 100, 2000);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		File localFile = new File(localCollectionAbsolutePath);
		File localReturnFile = new File(localCollectionReturnAbsolutePath);

		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dto.putOperation(localFile, destFile, null, null);

		Transfer transfer = new Transfer();
		transfer.setIrodsAbsolutePath(destFile.getAbsolutePath());
		transfer.setLocalAbsolutePath(localReturnFile.getAbsolutePath());
		transfer.setTransferType(TransferType.GET);

		conveyorService.getQueueManagerService().enqueueTransferOperation(
				transfer, irodsAccount);

		TransferTestRunningUtilities.waitForTransferToRunOrTimeout(
				conveyorService, TRANSFER_TIMEOUT);

		List<Transfer> transfers = conveyorService.getQueueManagerService()
				.listAllTransfersInQueue();
		Assert.assertFalse("no transfers in queue", transfers.isEmpty());
		Assert.assertEquals("should be 1 transfer..maybe test cleanup is bad",
				1, transfers.size());
		transfer = transfers.get(0);

		transfer = conveyorService.getQueueManagerService()
				.initializeGivenTransferByLoadingChildren(transfer);

		Assert.assertFalse("did not create a transfer attempt", transfer
				.getTransferAttempts().isEmpty());

		Assert.assertEquals("did not get complete status",
				TransferStateEnum.COMPLETE, transfer.getTransferState());

		TransferAttempt attempts[] = new TransferAttempt[transfer
				.getTransferAttempts().size()];
		attempts = transfer.getTransferAttempts().toArray(attempts);
		Assert.assertEquals("should be 1 attempt", 1, attempts.length);

		TransferAttempt attempt = attempts[0];
		Assert.assertNotNull("transfer attempt not persisted", attempt.getId());

	}

	@Test
	public void testGetSingleFile() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		conveyorService.validatePassPhrase(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PASSWORD_KEY));
		conveyorService.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);
		ConfigurationProperty logSuccessful = new ConfigurationProperty();
		logSuccessful
				.setPropertyKey(ConfigurationPropertyConstants.LOG_SUCCESSFUL_FILES_KEY);
		logSuccessful.setPropertyValue("true");

		conveyorService.getConfigurationService().addConfigurationProperty(
				logSuccessful);

		String testFileName = "test.file";
		String rootCollection = "testGetSingleFile";
		String localCollectionReturnAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection + "return");

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);

		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				3);

		File localFile = new File(absPath, testFileName);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		File localReturnFile = new File(localCollectionReturnAbsolutePath);

		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dto.putOperation(localFile, destFile, null, null);

		destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath + "/"
						+ testFileName);

		Transfer transfer = new Transfer();
		transfer.setIrodsAbsolutePath(destFile.getAbsolutePath());
		transfer.setLocalAbsolutePath(localReturnFile.getAbsolutePath());
		transfer.setTransferType(TransferType.GET);

		conveyorService.getQueueManagerService().enqueueTransferOperation(
				transfer, irodsAccount);

		TransferTestRunningUtilities.waitForTransferToRunOrTimeout(
				conveyorService, TRANSFER_TIMEOUT);

		List<Transfer> transfers = conveyorService.getQueueManagerService()
				.listAllTransfersInQueue();
		Assert.assertFalse("no transfers in queue", transfers.isEmpty());
		Assert.assertEquals("should be 1 transfer..maybe test cleanup is bad",
				1, transfers.size());
		transfer = transfers.get(0);

		transfer = conveyorService.getQueueManagerService()
				.initializeGivenTransferByLoadingChildren(transfer);

		Assert.assertFalse("did not create a transfer attempt", transfer
				.getTransferAttempts().isEmpty());

		Assert.assertEquals("did not get complete status",
				TransferStateEnum.COMPLETE, transfer.getTransferState());

		TransferAttempt attempts[] = new TransferAttempt[transfer
				.getTransferAttempts().size()];
		attempts = transfer.getTransferAttempts().toArray(attempts);
		Assert.assertEquals("should be 1 attempt", 1, attempts.length);

		TransferAttempt attempt = attempts[0];
		Assert.assertNotNull("transfer attempt not persisted", attempt.getId());

	}

	/**
	 * do a get of a nested collection that should be 'normal'
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCopyNestedCollection() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		conveyorService.validatePassPhrase(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PASSWORD_KEY));
		conveyorService.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);
		ConfigurationProperty logSuccessful = new ConfigurationProperty();
		logSuccessful
				.setPropertyKey(ConfigurationPropertyConstants.LOG_SUCCESSFUL_FILES_KEY);
		logSuccessful.setPropertyValue("true");

		conveyorService.getConfigurationService().addConfigurationProperty(
				logSuccessful);

		String rootCollection = "testCopyNestedCollection";
		String targetCollection = "testCopyNestedCollectionTarget";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String irodsCopySourceAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ rootCollection);

		String irodsCopyTargetAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ targetCollection);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testEnqueuePutTransferOperationAndWaitUntilDone", 2,
						5, 2, "testFile", ".txt", 10, 5, 100, 2000);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		File localFile = new File(localCollectionAbsolutePath);

		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dto.putOperation(localFile, destFile, null, null);

		Transfer transfer = new Transfer();
		transfer.setIrodsAbsolutePath(irodsCopySourceAbsolutePath);
		transfer.setLocalAbsolutePath(irodsCopyTargetAbsolutePath);
		transfer.setTransferType(TransferType.COPY);

		conveyorService.getQueueManagerService().enqueueTransferOperation(
				transfer, irodsAccount);

		TransferTestRunningUtilities.waitForTransferToRunOrTimeout(
				conveyorService, TRANSFER_TIMEOUT);

		List<Transfer> transfers = conveyorService.getQueueManagerService()
				.listAllTransfersInQueue();
		Assert.assertFalse("no transfers in queue", transfers.isEmpty());
		Assert.assertEquals("should be 1 transfer..maybe test cleanup is bad",
				1, transfers.size());
		transfer = transfers.get(0);

		transfer = conveyorService.getQueueManagerService()
				.initializeGivenTransferByLoadingChildren(transfer);

		Assert.assertFalse("did not create a transfer attempt", transfer
				.getTransferAttempts().isEmpty());

		Assert.assertEquals("did not get complete status",
				TransferStateEnum.COMPLETE, transfer.getTransferState());

		Assert.assertEquals("did not get OK status", TransferStatusEnum.OK,
				transfer.getLastTransferStatus());

		TransferAttempt attempts[] = new TransferAttempt[transfer
				.getTransferAttempts().size()];
		attempts = transfer.getTransferAttempts().toArray(attempts);
		Assert.assertEquals("should be 1 attempt", 1, attempts.length);

		TransferAttempt attempt = attempts[0];
		Assert.assertNotNull("transfer attempt not persisted", attempt.getId());

	}

	/**
	 * do a get of a nested collection that should be 'normal'
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReplicateNestedCollection() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		conveyorService.validatePassPhrase(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PASSWORD_KEY));
		conveyorService.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);
		ConfigurationProperty logSuccessful = new ConfigurationProperty();
		logSuccessful
				.setPropertyKey(ConfigurationPropertyConstants.LOG_SUCCESSFUL_FILES_KEY);
		logSuccessful.setPropertyValue("true");

		conveyorService.getConfigurationService().addConfigurationProperty(
				logSuccessful);

		String rootCollection = "testReplicateNestedCollection";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH
						+ '/' + rootCollection);

		String irodsCollectionRootAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		String irodsSourceAbsolutePath = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH + "/"
								+ rootCollection);

		FileGenerator
				.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
						localCollectionAbsolutePath,
						"testEnqueuePutTransferOperationAndWaitUntilDone", 2,
						5, 2, "testFile", ".txt", 10, 5, 100, 2000);

		IRODSFileFactory irodsFileFactory = irodsFileSystem
				.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory
				.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		File localFile = new File(localCollectionAbsolutePath);

		DataTransferOperations dto = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataTransferOperations(
						irodsAccount);

		dto.putOperation(localFile, destFile, null, null);

		Transfer transfer = new Transfer();
		transfer.setIrodsAbsolutePath(irodsSourceAbsolutePath);
		transfer.setResourceName(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_SECONDARY_RESOURCE_KEY));
		transfer.setTransferType(TransferType.REPLICATE);

		conveyorService.getQueueManagerService().enqueueTransferOperation(
				transfer, irodsAccount);

		TransferTestRunningUtilities.waitForTransferToRunOrTimeout(
				conveyorService, TRANSFER_TIMEOUT);

		List<Transfer> transfers = conveyorService.getQueueManagerService()
				.listAllTransfersInQueue();
		Assert.assertFalse("no transfers in queue", transfers.isEmpty());
		Assert.assertEquals("should be 1 transfer..maybe test cleanup is bad",
				1, transfers.size());
		transfer = transfers.get(0);

		transfer = conveyorService.getQueueManagerService()
				.initializeGivenTransferByLoadingChildren(transfer);

		Assert.assertFalse("did not create a transfer attempt", transfer
				.getTransferAttempts().isEmpty());

		Assert.assertEquals("did not get complete status",
				TransferStateEnum.COMPLETE, transfer.getTransferState());

		Assert.assertEquals("did not get OK status", TransferStatusEnum.OK,
				transfer.getLastTransferStatus());

		TransferAttempt attempts[] = new TransferAttempt[transfer
				.getTransferAttempts().size()];
		attempts = transfer.getTransferAttempts().toArray(attempts);
		Assert.assertEquals("should be 1 attempt", 1, attempts.length);

		TransferAttempt attempt = attempts[0];
		Assert.assertNotNull("transfer attempt not persisted", attempt.getId());

	}

	/**
	 * do a get of a nested collection that should be 'normal'
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTransferWithUnknownType() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		conveyorService.validatePassPhrase(testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_PASSWORD_KEY));
		conveyorService.getGridAccountService()
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);
		ConfigurationProperty logSuccessful = new ConfigurationProperty();
		logSuccessful
				.setPropertyKey(ConfigurationPropertyConstants.LOG_SUCCESSFUL_FILES_KEY);
		logSuccessful.setPropertyValue("true");

		conveyorService.getConfigurationService().addConfigurationProperty(
				logSuccessful);

		Transfer transfer = new Transfer();
		transfer.setIrodsAbsolutePath("blah");
		transfer.setTransferType(TransferType.UNKNOWN);

		conveyorService.getQueueManagerService().enqueueTransferOperation(
				transfer, irodsAccount);

		TransferTestRunningUtilities.waitForTransferToRunOrTimeout(
				conveyorService, TRANSFER_TIMEOUT);

		List<Transfer> transfers = conveyorService.getQueueManagerService()
				.listAllTransfersInQueue();
		Assert.assertFalse("no transfers in queue", transfers.isEmpty());
		Assert.assertEquals("should be 1 transfer..maybe test cleanup is bad",
				1, transfers.size());
		transfer = transfers.get(0);

		transfer = conveyorService.getQueueManagerService()
				.initializeGivenTransferByLoadingChildren(transfer);

		Assert.assertFalse("did not create a transfer attempt", transfer
				.getTransferAttempts().isEmpty());

		Assert.assertEquals("did not get complete status",
				TransferStateEnum.COMPLETE, transfer.getTransferState());

		Assert.assertEquals("did not get errir status",
				TransferStatusEnum.ERROR, transfer.getLastTransferStatus());

		TransferAttempt attempts[] = new TransferAttempt[transfer
				.getTransferAttempts().size()];
		attempts = transfer.getTransferAttempts().toArray(attempts);
		Assert.assertEquals("should be 1 attempt", 1, attempts.length);

		TransferAttempt attempt = attempts[0];
		Assert.assertNotNull("transfer attempt not persisted", attempt.getId());

	}

}
