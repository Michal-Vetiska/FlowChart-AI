package com.flowcart.commerce.ai.knowledge;

public interface EmbeddingClient {

	float[] embed(String text);

	String modelVersion();

	int dimensions();
}
