/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.irods.jargon.idrop.desktop.systraygui.services;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.irods.jargon.transfer.TransferServiceFactoryImpl;
import org.irods.jargon.transfer.engine.ConfigurationService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author mikeconway
 */
public class IdropConfigurationServiceImplTest {

    private static IdropConfigurationService idropConfigurationService;
    private static ConfigurationService configurationService;
    private static Properties testingProperties = new Properties();
    private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
    private static ScratchFileUtils scratchFileUtils = null;
    private static final String TESTING_SUBDIR = "IdropConfigurationServiceImplTest";

    public IdropConfigurationServiceImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
        testingProperties = testingPropertiesLoader.getTestProperties();
        scratchFileUtils = new ScratchFileUtils(testingProperties);
        scratchFileUtils.clearAndReinitializeScratchDirectory(TESTING_SUBDIR);
        File f = new File(scratchFileUtils.createAndReturnAbsoluteScratchPath(TESTING_SUBDIR), "idrop.properties");
        idropConfigurationService = new IdropConfigurationServiceImpl(f);
        TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();
        configurationService = transferServiceFactory.instanceConfigurationService();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        scratchFileUtils.clearAndReinitializeScratchDirectory(TESTING_SUBDIR);
        configurationService.importProperties(new Properties());
    }

    @After
    public void tearDown() {
    }

   
    @Test
    public void testBootstrapConfigurationWhenPropsInDb() throws Exception {
        String testPropKey = "testBootstrapConfigurationWhenPropsInDb";
        String testPropVal = "thevalue";
        Properties testProps = new Properties();
        testProps.put(testPropKey, testPropVal);
        configurationService.importProperties(testProps);
        Properties myProps = idropConfigurationService.bootstrapConfiguration();
        Assert.assertNotNull("null props returned", myProps);
        Assert.assertEquals(1, myProps.size());
        Assert.assertEquals(testPropVal, myProps.get(testPropKey));
    }
    
    @Test
    public void testBootstrapConfigurationWhenPropsInClasspath() throws Exception {
        Properties testProps = new Properties();
        configurationService.importProperties(testProps);
        Properties myProps = idropConfigurationService.bootstrapConfiguration();
        Assert.assertNotNull("null props returned", myProps);
        Assert.assertFalse("props shold not be empty will be loaded from classpath", myProps.isEmpty());
         Assert.assertNotNull("should have loaded props from classpath", myProps.get("login.preset"));
    }

}
