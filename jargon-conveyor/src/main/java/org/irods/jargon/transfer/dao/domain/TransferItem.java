package org.irods.jargon.transfer.dao.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;

/**
 * For a <code>Transfer</code>, this is an individual operation within the
 * transfer. This item would be a directory or file that was moved during the
 * transfer.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Entity
@Table(name = "transfer_item")
public class TransferItem implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id()
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "sequence_number", nullable = false)
	private long sequenceNumber;

	@ManyToOne(targetEntity = TransferAttempt.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "transfer_attempt_id", nullable = false)
	private TransferAttempt transferAttempt;

	@Column(name = "source_file_absolute_path", length = 32672)
	private String sourceFileAbsolutePath;

	@Column(name = "target_file_absolute_path", length = 32672)
	private String targetFileAbsolutePath;

	@Column(name = "transfer_type")
	@Enumerated(EnumType.STRING)
	private TransferType transferType;

	@Column(name = "is_file")
	private boolean file;

	@Column(name = "is_skipped")
	private boolean skipped = false;

	@Column(name = "is_error")
	private boolean error;

	@Column(name = "length_in_bytes")
	private long lengthInBytes = 0L;

	@Column(name = "error_message")
	private String errorMessage;

	@Column(name = "error_stack_trace", length = 32672)
	private String errorStackTrace;

	@Column(name = "transferred_at")
	@Temporal(javax.persistence.TemporalType.TIMESTAMP)
	private Date transferredAt;

	public TransferItem() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public String getSourceFileAbsolutePath() {
		return sourceFileAbsolutePath;
	}

	public void setSourceFileAbsolutePath(final String sourceFileAbsolutePath) {
		this.sourceFileAbsolutePath = sourceFileAbsolutePath;
	}

	public String getTargetFileAbsolutePath() {
		return targetFileAbsolutePath;
	}

	public void setTargetFileAbsolutePath(final String targetFileAbsolutePath) {
		this.targetFileAbsolutePath = targetFileAbsolutePath;
	}

	public boolean isFile() {
		return file;
	}

	public void setFile(final boolean file) {
		this.file = file;
	}

	public boolean isError() {
		return error;
	}

	public void setError(final boolean error) {
		this.error = error;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Date getTransferredAt() {
		return transferredAt;
	}

	public void setTransferredAt(final Date transferredAt) {
		this.transferredAt = transferredAt;
	}

	public TransferAttempt getTransferAttempt() {
		return transferAttempt;
	}

	public void setTransferAttempt(final TransferAttempt transferAttempt) {
		this.transferAttempt = transferAttempt;
	}

	public long getLengthInBytes() {
		return lengthInBytes;
	}

	public void setLengthInBytes(final long lengthInBytes) {
		this.lengthInBytes = lengthInBytes;
	}

	public String getErrorStackTrace() {
		return errorStackTrace;
	}

	public void setErrorStackTrace(final String errorStackTrace) {
		this.errorStackTrace = errorStackTrace;
	}

	/**
	 * @param transferType
	 *            the transferType to set
	 */
	public void setTransferType(final TransferType transferType) {
		this.transferType = transferType;
	}

	/**
	 * @return the transferType
	 */
	public TransferType getTransferType() {
		return transferType;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TransferItem:");
		sb.append("\n   id:");
		sb.append(id);
		sb.append("\n   transferType:");
		sb.append(transferType);
		sb.append("\n   sourceFileAbsolutePath:");
		sb.append(sourceFileAbsolutePath);
		sb.append("\n   targetFileAbsolutePath:");
		sb.append(targetFileAbsolutePath);
		sb.append("\n   isFile:");
		sb.append(file);
		sb.append("\n   isSkippedFile:");
		sb.append(skipped);
		sb.append("\n   lengthInBytes:");
		sb.append(lengthInBytes);
		sb.append("\n   isError:");
		sb.append(error);
		sb.append("\n   errorMessage:");
		sb.append(errorMessage);
		sb.append("\n   transferredAt:");
		sb.append(transferredAt);
		return sb.toString();
	}

	/**
	 * @return the skipped
	 */
	public boolean isSkipped() {
		return skipped;
	}

	/**
	 * @param skipped
	 *            the skipped to set
	 */
	public void setSkipped(final boolean skipped) {
		this.skipped = skipped;
	}

	/**
	 * @return the sequenceNumber
	 */
	public long getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * @param sequenceNumber
	 *            the sequenceNumber to set
	 */
	public void setSequenceNumber(final long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

}
