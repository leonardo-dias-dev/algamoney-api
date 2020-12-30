package com.algamoney.api.repository.lancamento;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.algamoney.api.dto.LancamentoEstatisticaCategoriaDto;
import com.algamoney.api.dto.LancamentoEstatisticaDiaDto;
import com.algamoney.api.dto.LancamentoEstatisticaPessoa;
import com.algamoney.api.model.Lancamento;
import com.algamoney.api.repository.filter.LancamentoFilter;
import com.algamoney.api.repository.projection.ResumoLancamento;

public interface LancamentoRepositoryQuery {
	
	public Page<Lancamento> filtrar(LancamentoFilter lancamentoFilter, Pageable pageable);
	
	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable);
	
	public List<LancamentoEstatisticaCategoriaDto> porCategoria(LocalDate mesReferencial);
	
	public List<LancamentoEstatisticaDiaDto> porDia(LocalDate mesReferencial);

	List<LancamentoEstatisticaPessoa> porPessoa(LocalDate inicio, LocalDate fim);

}
 