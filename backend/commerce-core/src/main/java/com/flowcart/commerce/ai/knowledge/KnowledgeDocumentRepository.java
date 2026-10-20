package com.flowcart.commerce.ai.knowledge;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
interface SpringDataKnowledgeDocumentJpaRepository extends JpaRepository<KnowledgeDocumentJpaEntity, UUID> {
}

@Component
public class KnowledgeDocumentRepository {

	private final SpringDataKnowledgeDocumentJpaRepository jpa;

	public KnowledgeDocumentRepository(SpringDataKnowledgeDocumentJpaRepository jpa) {
		this.jpa = jpa;
	}

	public KnowledgeDocument save(KnowledgeDocument document) {
		KnowledgeDocumentJpaEntity entity = new KnowledgeDocumentJpaEntity();
		entity.setId(document.id());
		entity.setTitle(document.title());
		entity.setSourceName(document.sourceName());
		entity.setContentType(document.contentType());
		entity.setStatus(document.status());
		entity.setErrorMessage(document.errorMessage());
		entity.setRawText(document.rawText());
		entity.setCreatedAt(document.createdAt());
		entity.setUpdatedAt(document.updatedAt());
		KnowledgeDocumentJpaEntity saved = jpa.save(entity);
		return toDomain(saved);
	}

	public Optional<KnowledgeDocument> findById(UUID id) {
		return jpa.findById(id).map(this::toDomain);
	}

	public List<KnowledgeDocument> findAll() {
		return jpa.findAll().stream().map(this::toDomain).toList();
	}

	public void deleteById(UUID id) {
		jpa.deleteById(id);
	}

	private KnowledgeDocument toDomain(KnowledgeDocumentJpaEntity entity) {
		return KnowledgeDocument.rehydrate(
				entity.getId(),
				entity.getTitle(),
				entity.getSourceName(),
				entity.getContentType(),
				entity.getStatus(),
				entity.getErrorMessage(),
				entity.getRawText(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}
}
