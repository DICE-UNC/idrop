/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.irods.jargon.core.utils.LocalFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to load flow specifications as DSL script files from a set of
 * locations, and find appropriate flow specs given a specification
 * <p/>
 * Since flow specs are groovy DSLs in a text file, this will run a groovy shell
 * on each discovered specification
 * 
 * @author Mike Conway - DICE
 * 
 */
public class FlowSpecCacheService {

	private List<String> flowSourceLocalAbsolutePaths = new ArrayList<String>();
	private List<FlowSpec> flowSpecs = new ArrayList<FlowSpec>();
	/**
	 * Dependency on delimiter, defaults to a , (comma) if not set explicitly
	 */
	private final String delimiter = ",";

	private static final Logger log = LoggerFactory
			.getLogger(FlowSpecCacheService.class);

	/**
	 * 
	 */
	public FlowSpecCacheService() {
	}

	/**
	 * Scan the library of dsls and add a flowspec for each .groovy file
	 * 
	 * @throws FlowSpecConfigurationException
	 * @throws FlowManagerException
	 */
	public synchronized void init() throws FlowSpecConfigurationException,
			FlowManagerException {

		log.info("init()...scan for dsl files");

		for (String flowPath : flowSourceLocalAbsolutePaths) {
			findFlowsInPath(flowPath);
		}

		log.info("flows processed");

		if (flowSpecs.isEmpty()) {
			log.warn("No flows configured");
		}

		flowSpecs = Collections.unmodifiableList(flowSpecs);

	}

	/**
	 * Scan the file and load up any flows you find. This does not recurse, so
	 * that one may control the load order by file sort order.
	 * 
	 * @param flowPath
	 * @throws FlowSpecConfigurationException
	 * @throws FlowManagerException
	 */
	private void findFlowsInPath(final String flowPath)
			throws FlowSpecConfigurationException, FlowManagerException {

		log.info("looking for flows in path:{}", flowPath);

		/*
		 * Ignore missing dirs and just plow ahead. Mabye later add a validate
		 * step?
		 */

		File flowPathFile = new File(LocalFileUtils.normalizePath(flowPath));
		if (!flowPathFile.exists()) {
			log.warn("flow path does not exist:{}", flowPath);
			return;
		}

		if (!flowPathFile.isDirectory()) {
			log.warn("flow path not directory:{}", flowPath);
			return;
		}

		log.info("list chilren...");

		for (File child : flowPathFile.listFiles()) {
			log.info("child:{}", child);

			if (child.isFile()) {
				if (LocalFileUtils.getFileExtension(child.getName()).equals(
						".groovy")) {
					log.info("have groovy file");
					addGroovyDslToFlowSpecs(child);
				} else {
					continue;
				}
			} else {
				// this is a dir, if I want to recurse to it here, for now it's
				// flat to have more reasonable load orders
				continue;
			}
		}
	}

	private void addGroovyDslToFlowSpecs(final File child)
			throws FlowManagerException {

		log.info("parsing groovy sript for flow:{}", child);

		/*
		 * Note here that the specified roots are scanned for any code changes,
		 * allowing recompile in place if they change
		 */
		String[] roots = flowSourceLocalAbsolutePaths
				.toArray(new String[flowSourceLocalAbsolutePaths.size()]);
		GroovyScriptEngine gse;
		try {
			gse = new GroovyScriptEngine(roots);
			Binding binding = new Binding();
			log.info("running...{}", child.getName());
			Object result = gse.run(child.getName(), binding);

			if (result == null) {
				log.warn("null result, script discarded for:{}", child);
			} else if (result instanceof FlowSpec) {
				log.info("adding flow spec for child");
				flowSpecs.add((FlowSpec) result);
			} else {
				log.warn("result not flow spec, discarding:{}", child);
			}
		} catch (IOException e) {
			log.error(
					"groovy error - io exception on startup of GroovyScriptEngine",
					e);
			throw new FlowManagerException("io exception starting groovy", e);
		} catch (ResourceException e) {
			log.error(
					"groovy error - resource exception on startup of GroovyScriptEngine",
					e);
			throw new FlowManagerException(
					"resource exception starting groovy", e);
		} catch (ScriptException e) {
			log.error(
					"groovy error - script exception on startup of GroovyScriptEngine",
					e);
			throw new FlowManagerException("script exception starting groovy",
					e);
		}

	}

	/**
	 * Alternative setter for dependency on a list of paths, allowing a
	 * delimited string that represents the list of paths, separated by the
	 * given delimiter value;
	 * 
	 * @param paths
	 * @param delimiter
	 */
	public synchronized void setFlowSourceLocalAbsolutePathsAsDelimitedString(
			final String paths) {
		log.info("setFLowSourceLocalAbsolutePathsAsDelimitedString()");

		if (paths == null || paths.isEmpty()) {
			throw new IllegalArgumentException("null or empty paths");
		}

		String[] splitPaths = paths.split(delimiter);
		List<String> pathsList = new ArrayList<String>();
		for (String path : splitPaths) {
			pathsList.add(path);
		}

		setFlowSourceLocalAbsolutePaths(pathsList);

	}

	/**
	 * Setter for injecting the list of local absolute paths to scan for .groovy
	 * files that are flow specs.
	 * <p/>
	 * This is saved internally as an immutable list
	 * 
	 * @param flowSourceLocalAbsolutePaths
	 */
	public synchronized void setFlowSourceLocalAbsolutePaths(
			final List<String> flowSourceLocalAbsolutePaths) {

		if (flowSourceLocalAbsolutePaths == null) {
			throw new IllegalArgumentException(
					"null flowSourceLocalAbsolutePaths");
		}

		this.flowSourceLocalAbsolutePaths = Collections
				.unmodifiableList(flowSourceLocalAbsolutePaths);
	}

	/**
	 * Get the current set of flow specs. Note that this is an unmodifiable list
	 * of clones, so they are thread safe once obtained.
	 * 
	 * @return
	 */
	public synchronized List<FlowSpec> getFlowSpecs() {
		List<FlowSpec> clonedFlowSpecs = new ArrayList<FlowSpec>();

		for (FlowSpec flowSpec : flowSpecs) {
			clonedFlowSpecs.add(flowSpec.clone());
		}

		return Collections.unmodifiableList(clonedFlowSpecs);
	}

	/**
	 * @return the delimiter
	 */
	public synchronized String getDelimiter() {
		return delimiter;
	}

}
