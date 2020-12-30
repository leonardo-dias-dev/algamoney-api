package com.algamoney.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.algamoney.api.model.Categoria;
import com.algamoney.api.repository.CategoriaRepository;

@Service
public class CategoriaService {
	
	@Autowired
	private CategoriaRepository categoriaRepository;
	
	public List<Categoria> buscarTodos() {
		return categoriaRepository.findAll();
	}
	
	public Categoria buscarPeloCodigo(Long codigo) {
		Optional<Categoria> optionalCategoria = categoriaRepository.findById(codigo);
		
		return optionalCategoria.orElseThrow(() -> new EmptyResultDataAccessException(1));
	}
	
	public Categoria incluir(Categoria categoria) {
		return categoriaRepository.save(categoria);
	}

}
