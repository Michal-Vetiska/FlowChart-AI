package com.flowcart.commerce.ai.knowledge;

import com.flowcart.commerce.ai.knowledge.KnowledgeService.GroundedAnswer;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {

	private final KnowledgeService knowledgeService;

	public KnowledgeController(KnowledgeService knowledgeService) {
		this.knowledgeService = knowledgeService;
	}

	public record DocumentResponse(
			UUID id,
			String title,
			String sourceName,
			String contentType,
			String status,
			String errorMessage
	) {
		static DocumentResponse from(KnowledgeDocument document) {
			return new DocumentResponse(
					document.id(),
					document.title(),
					document.sourceName(),
					document.contentType(),
					document.status().name(),
					document.errorMessage()
			);
		}
	}

	public record QueryRequest(String question, Integer topK) {
	}

	@PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public DocumentResponse upload(
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "title", required = false) String title
	) {
		return DocumentResponse.from(knowledgeService.upload(file, title));
	}

	@GetMapping("/documents")
	public List<DocumentResponse> list() {
		return knowledgeService.list().stream().map(DocumentResponse::from).toList();
	}

	@GetMapping("/documents/{id}")
	public DocumentResponse get(@PathVariable UUID id) {
		return DocumentResponse.from(knowledgeService.get(id));
	}

	@DeleteMapping("/documents/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		knowledgeService.delete(id);
	}

	@PostMapping("/query")
	public GroundedAnswer query(@RequestBody QueryRequest request) {
		int topK = request.topK() == null ? 5 : request.topK();
		return knowledgeService.query(request.question(), topK);
	}
}
