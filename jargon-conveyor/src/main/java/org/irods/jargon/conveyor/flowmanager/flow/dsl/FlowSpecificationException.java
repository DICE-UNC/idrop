/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow.dsl;

/**
 * Exception (runtime) that occurs when defining a specification
 * 
 * @author Mike Conway - DICE
 * 
 */
public class FlowSpecificationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5151193882349061547L;

	/**
	 * 
	 */
	public FlowSpecificationException() {
	}

	/**
	 * @param arg0
	 */
	public FlowSpecificationException(final String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public FlowSpecificationException(final Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public FlowSpecificationException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

}
