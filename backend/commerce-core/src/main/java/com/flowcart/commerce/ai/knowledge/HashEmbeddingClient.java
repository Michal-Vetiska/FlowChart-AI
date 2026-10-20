package com.flowcart.commerce.ai.knowledge;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Deterministic local embeddings for CI / demos without an LLM.
 * Replace with Ollama/OpenAI adapter in production-like demos.
 */
@Component
@ConditionalOnProperty(name = "flowcart.ai.embedding-mode", havingValue = "hash", matchIfMissing = true)
public class HashEmbeddingClient implements EmbeddingClient {

	private static final int DIMS = 384;

	@Override
	public float[] embed(String text) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(text.toLowerCase().getBytes(StandardCharsets.UTF_8));
			float[] vector = new float[DIMS];
			for (int i = 0; i < DIMS; i++) {
				vector[i] = ((hash[i % hash.length] & 0xff) / 255f) - 0.5f;
			}
			normalize(vector);
			return vector;
		}
		catch (Exception ex) {
			float[] zeros = new float[DIMS];
			Arrays.fill(zeros, 0.01f);
			return zeros;
		}
	}

	@Override
	public String modelVersion() {
		return "hash-sha256-v1";
	}

	@Override
	public int dimensions() {
		return DIMS;
	}

	private static void normalize(float[] vector) {
		double sum = 0;
		for (float v : vector) {
			sum += v * v;
		}
		double norm = Math.sqrt(sum);
		if (norm == 0) {
			return;
		}
		for (int i = 0; i < vector.length; i++) {
			vector[i] = (float) (vector[i] / norm);
		}
	}
}
