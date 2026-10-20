package com.flowcart.commerce.ai.knowledge;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class KnowledgeChunkingTest {

	@Test
	void chunksOverlapAndCoverFullText() {
		String text = "a".repeat(1200);
		List<String> chunks = KnowledgeService.chunk(text, 500, 80);
		assertThat(chunks).hasSizeGreaterThan(1);
		assertThat(chunks.getFirst()).hasSize(500);
		assertThat(chunks.get(1)).startsWith(chunks.getFirst().substring(420));
		assertThat(chunks.getLast().length()).isLessThanOrEqualTo(500);
		int covered = chunks.stream().mapToInt(String::length).sum() - 80 * (chunks.size() - 1);
		assertThat(covered).isEqualTo(1200);
	}

	@Test
	void blankTextYieldsNoChunks() {
		assertThat(KnowledgeService.chunk("   ", 100, 10)).isEmpty();
		assertThat(KnowledgeService.chunk(null, 100, 10)).isEmpty();
	}

	@Test
	void hashEmbeddingsAreDeterministicAndNormalized() {
		HashEmbeddingClient client = new HashEmbeddingClient();
		float[] a = client.embed("payment failure runbook");
		float[] b = client.embed("payment failure runbook");
		assertThat(a).hasSize(384);
		assertThat(a).containsExactly(b);
		double norm = 0;
		for (float v : a) {
			norm += v * v;
		}
		assertThat(Math.sqrt(norm)).isBetween(0.99, 1.01);
	}
}
