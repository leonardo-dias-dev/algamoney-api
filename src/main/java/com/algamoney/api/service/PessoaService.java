package com.algamoney.api.service;

import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.algamoney.api.model.Pessoa;
import com.algamoney.api.repository.PessoaRepository;
import com.algamoney.api.repository.filter.PessoaFilter;

@Service
public class PessoaService {
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	public Page<Pessoa> pesquisar(PessoaFilter pessoaFilter, Pageable pageable) {
		return pessoaRepository.filtrar(pessoaFilter, pageable);
	}
	
	public Pessoa buscarPeloCodigo(Long codigo) {
		Optional<Pessoa> optionalPessoa = pessoaRepository.findById(codigo);
		
		return optionalPessoa.orElseThrow(() -> new EmptyResultDataAccessException(1));
	}
	
	public Pessoa incluir(Pessoa pessoa) {
		pessoa.getContatos().forEach(contato -> contato.setPessoa(pessoa));
		
		return pessoaRepository.save(pessoa);
	}
	
	public void remover(Long codigo) {
		pessoaRepository.deleteById(codigo);
	}
	
	public Pessoa atualizar(Pessoa pessoa, Long codigo) {
		Pessoa pessoaSalvo = buscarPeloCodigo(codigo);
		
		pessoaSalvo.getContatos().clear();
		pessoaSalvo.getContatos().addAll(pessoa.getContatos());
		pessoaSalvo.getContatos().forEach(contato -> contato.setPessoa(pessoaSalvo));
		
		BeanUtils.copyProperties(pessoa, pessoaSalvo, "codigo", "contatos");
		
		return pessoaRepository.save(pessoaSalvo);
	}
	
	public void atualizarPropriedadeAtivo(Long codigo, Boolean ativo) {
		Pessoa pessoa = buscarPeloCodigo(codigo);
		
		pessoa.setAtivo(ativo);
		
		pessoaRepository.save(pessoa);
	}

}
