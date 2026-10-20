package com.flowcart.commerce.catalog.application;

import com.flowcart.commerce.catalog.application.port.CategoryRepository;
import com.flowcart.commerce.catalog.domain.Category;
import com.flowcart.commerce.shared.domain.ConflictException;
import com.flowcart.commerce.shared.domain.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;

	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	@Transactional
	public Category create(String name, String description) {
		Category category = Category.create(name, description);
		if (categoryRepository.existsBySlug(category.slug())) {
			throw new ConflictException("Category slug already exists: " + category.slug());
		}
		return categoryRepository.save(category);
	}

	@Transactional(readOnly = true)
	public Category getById(UUID id) {
		return categoryRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
	}

	@Transactional(readOnly = true)
	public List<Category> list() {
		return categoryRepository.findAll();
	}
}
