package com.flowcart.commerce.ai.knowledge;

import java.time.Instant;
import java.util.UUID;

public class KnowledgeDocument {

	private final UUID id;
	private String title;
	private final String sourceName;
	private final String contentType;
	private DocumentStatus status;
	private String errorMessage;
	private String rawText;
	private final Instant createdAt;
	private Instant updatedAt;

	private KnowledgeDocument(
			UUID id,
			String title,
			String sourceName,
			String contentType,
			DocumentStatus status,
			String errorMessage,
			String rawText,
			Instant createdAt,
			Instant updatedAt
	) {
		this.id = id;
		this.title = title;
		this.sourceName = sourceName;
		this.contentType = contentType;
		this.status = status;
		this.errorMessage = errorMessage;
		this.rawText = rawText;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static KnowledgeDocument create(String title, String sourceName, String contentType, String rawText) {
		Instant now = Instant.now();
		return new KnowledgeDocument(UUID.randomUUID(), title, sourceName, contentType, DocumentStatus.UPLOADED, null, rawText, now, now);
	}

	public static KnowledgeDocument rehydrate(
			UUID id,
			String title,
			String sourceName,
			String contentType,
			DocumentStatus status,
			String errorMessage,
			String rawText,
			Instant createdAt,
			Instant updatedAt
	) {
		return new KnowledgeDocument(id, title, sourceName, contentType, status, errorMessage, rawText, createdAt, updatedAt);
	}

	public void markProcessing() {
		status = DocumentStatus.PROCESSING;
		updatedAt = Instant.now();
	}

	public void markIndexed() {
		status = DocumentStatus.INDEXED;
		errorMessage = null;
		updatedAt = Instant.now();
	}

	public void markFailed(String error) {
		status = DocumentStatus.FAILED;
		errorMessage = error;
		updatedAt = Instant.now();
	}

	public UUID id() {
		return id;
	}

	public String title() {
		return title;
	}

	public String sourceName() {
		return sourceName;
	}

	public String contentType() {
		return contentType;
	}

	public DocumentStatus status() {
		return status;
	}

	public String errorMessage() {
		return errorMessage;
	}

	public String rawText() {
		return rawText;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}
