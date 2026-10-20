package com.flowcart.commerce.ai.knowledge;

import com.flowcart.commerce.shared.domain.DomainException;
import com.flowcart.commerce.shared.domain.ResourceNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.tika.Tika;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class KnowledgeService {

	private final KnowledgeDocumentRepository documentRepository;
	private final EmbeddingClient embeddingClient;
	private final JdbcTemplate jdbcTemplate;
	private final Tika tika = new Tika();

	public KnowledgeService(
			KnowledgeDocumentRepository documentRepository,
			EmbeddingClient embeddingClient,
			JdbcTemplate jdbcTemplate
	) {
		this.documentRepository = documentRepository;
		this.embeddingClient = embeddingClient;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional
	public KnowledgeDocument upload(MultipartFile file, String title) {
		validateFile(file);
		try {
			String text = tika.parseToString(file.getInputStream());
			return ingestText(
					title == null || title.isBlank() ? file.getOriginalFilename() : title,
					file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename(),
					file.getContentType() == null ? "application/octet-stream" : file.getContentType(),
					text
			);
		}
		catch (DomainException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new DomainException("Failed to ingest document: " + ex.getMessage(), ex);
		}
	}

	@Transactional
	public KnowledgeDocument ingestText(String title, String sourceName, String contentType, String text) {
		try {
			KnowledgeDocument document = KnowledgeDocument.create(title, sourceName, contentType, text);
			document.markProcessing();
			documentRepository.save(document);
			indexDocument(document);
			document.markIndexed();
			return documentRepository.save(document);
		}
		catch (Exception ex) {
			throw new DomainException("Failed to ingest document: " + ex.getMessage(), ex);
		}
	}

	@Transactional(readOnly = true)
	public List<KnowledgeDocument> list() {
		return documentRepository.findAll();
	}

	@Transactional(readOnly = true)
	public KnowledgeDocument get(UUID id) {
		return documentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));
	}

	@Transactional
	public void delete(UUID id) {
		jdbcTemplate.update("delete from knowledge_chunks where document_id = ?", id);
		documentRepository.deleteById(id);
	}

	@Transactional(readOnly = true)
	public GroundedAnswer query(String question, int topK) {
		List<RetrievedChunk> vectorHits = vectorSearch(question, topK);
		List<RetrievedChunk> ftsHits = fullTextSearch(question, topK);
		List<RetrievedChunk> fused = reciprocalRankFusion(vectorHits, ftsHits, topK);

		if (fused.isEmpty()) {
			return new GroundedAnswer(
					"I do not have enough indexed evidence to answer that question.",
					"low",
					List.of(),
					true
			);
		}

		StringBuilder evidence = new StringBuilder();
		List<Citation> citations = new ArrayList<>();
		int i = 1;
		for (RetrievedChunk chunk : fused) {
			evidence.append("[").append(i).append("] ").append(chunk.content()).append("\n\n");
			citations.add(new Citation(chunk.documentId(), chunk.documentTitle(), chunk.pageNumber(), i));
			i++;
		}

		String answer = "Based on retrieved policy/runbook evidence:\n"
				+ fused.getFirst().content().strip()
				+ "\n\nCitations: "
				+ citations.stream().map(c -> "[" + c.ref() + "] " + c.documentTitle()).reduce((a, b) -> a + "; " + b).orElse("");

		return new GroundedAnswer(answer, "medium", citations, false);
	}

	private void indexDocument(KnowledgeDocument document) {
		List<String> chunks = chunk(document.rawText(), 500, 80);
		int index = 0;
		for (String chunk : chunks) {
			float[] embedding = embeddingClient.embed(chunk);
			String vectorLiteral = toVectorLiteral(embedding);
			jdbcTemplate.update("""
					insert into knowledge_chunks
					(id, document_id, chunk_index, content, section, page_number, embedding, tsv, embedding_model, created_at)
					values (?, ?, ?, ?, ?, ?, cast(? as vector), to_tsvector('english', ?), ?, ?)
					""",
					UUID.randomUUID(),
					document.id(),
					index++,
					chunk,
					null,
					null,
					vectorLiteral,
					chunk,
					embeddingClient.modelVersion(),
					Instant.now()
			);
		}
	}

	private List<RetrievedChunk> vectorSearch(String question, int topK) {
		String vectorLiteral = toVectorLiteral(embeddingClient.embed(question));
		return jdbcTemplate.query("""
				select c.id, c.document_id, d.title, c.content, c.page_number,
				       (c.embedding <=> cast(? as vector)) as distance
				from knowledge_chunks c
				join knowledge_documents d on d.id = c.document_id
				where d.status = 'INDEXED'
				order by c.embedding <=> cast(? as vector)
				limit ?
				""",
				(rs, rowNum) -> new RetrievedChunk(
						UUID.fromString(rs.getString("id")),
						UUID.fromString(rs.getString("document_id")),
						rs.getString("title"),
						rs.getString("content"),
						(Integer) rs.getObject("page_number"),
						1.0 / (1.0 + rs.getDouble("distance"))
				),
				vectorLiteral, vectorLiteral, topK
		);
	}

	private List<RetrievedChunk> fullTextSearch(String question, int topK) {
		return jdbcTemplate.query("""
				select c.id, c.document_id, d.title, c.content, c.page_number,
				       ts_rank(c.tsv, plainto_tsquery('english', ?)) as rank
				from knowledge_chunks c
				join knowledge_documents d on d.id = c.document_id
				where d.status = 'INDEXED' and c.tsv @@ plainto_tsquery('english', ?)
				order by rank desc
				limit ?
				""",
				(rs, rowNum) -> new RetrievedChunk(
						UUID.fromString(rs.getString("id")),
						UUID.fromString(rs.getString("document_id")),
						rs.getString("title"),
						rs.getString("content"),
						(Integer) rs.getObject("page_number"),
						rs.getDouble("rank")
				),
				question, question, topK
		);
	}

	private List<RetrievedChunk> reciprocalRankFusion(
			List<RetrievedChunk> a,
			List<RetrievedChunk> b,
			int topK
	) {
		Map<UUID, Double> scores = new LinkedHashMap<>();
		Map<UUID, RetrievedChunk> byId = new LinkedHashMap<>();
		addRanks(a, scores, byId);
		addRanks(b, scores, byId);
		return scores.entrySet().stream()
				.sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
				.limit(topK)
				.map(e -> byId.get(e.getKey()))
				.toList();
	}

	private void addRanks(List<RetrievedChunk> hits, Map<UUID, Double> scores, Map<UUID, RetrievedChunk> byId) {
		int rank = 1;
		for (RetrievedChunk hit : hits) {
			byId.put(hit.chunkId(), hit);
			scores.merge(hit.chunkId(), 1.0 / (60 + rank), Double::sum);
			rank++;
		}
	}

	static List<String> chunk(String text, int size, int overlap) {
		List<String> chunks = new ArrayList<>();
		if (text == null || text.isBlank()) {
			return chunks;
		}
		String normalized = text.replaceAll("\\s+", " ").trim();
		int start = 0;
		while (start < normalized.length()) {
			int end = Math.min(normalized.length(), start + size);
			chunks.add(normalized.substring(start, end));
			if (end == normalized.length()) {
				break;
			}
			start = Math.max(end - overlap, start + 1);
		}
		return chunks;
	}

	private static String toVectorLiteral(float[] embedding) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < embedding.length; i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(embedding[i]);
		}
		sb.append(']');
		return sb.toString();
	}

	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new DomainException("File is required");
		}
		if (file.getSize() > 5_000_000) {
			throw new DomainException("File exceeds 5MB limit");
		}
		String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
		if (!(name.endsWith(".txt") || name.endsWith(".md") || name.endsWith(".pdf") || name.endsWith(".docx"))) {
			throw new DomainException("Unsupported file type");
		}
	}

	public record RetrievedChunk(
			UUID chunkId,
			UUID documentId,
			String documentTitle,
			String content,
			Integer pageNumber,
			double score
	) {
	}

	public record Citation(UUID documentId, String documentTitle, Integer pageNumber, int ref) {
	}

	public record GroundedAnswer(String answer, String confidence, List<Citation> citations, boolean insufficientEvidence) {
	}
}
