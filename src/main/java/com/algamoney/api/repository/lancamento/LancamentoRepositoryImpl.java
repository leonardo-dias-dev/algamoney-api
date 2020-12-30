package com.algamoney.api.repository.lancamento;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.algamoney.api.dto.LancamentoEstatisticaCategoriaDto;
import com.algamoney.api.dto.LancamentoEstatisticaDiaDto;
import com.algamoney.api.dto.LancamentoEstatisticaPessoa;
import com.algamoney.api.model.Categoria_;
import com.algamoney.api.model.Lancamento;
import com.algamoney.api.model.Lancamento_;
import com.algamoney.api.model.Pessoa_;
import com.algamoney.api.repository.filter.LancamentoFilter;
import com.algamoney.api.repository.projection.ResumoLancamento;

public class LancamentoRepositoryImpl implements LancamentoRepositoryQuery {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Page<Lancamento> filtrar(LancamentoFilter lancamentoFilter, Pageable pageable) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Lancamento> criteriaQuery = criteriaBuilder.createQuery(Lancamento.class);
		Root<Lancamento> root = criteriaQuery.from(Lancamento.class);

		Predicate[] predicates = criarRestricoes(lancamentoFilter, criteriaBuilder, root);
		criteriaQuery.where(predicates);

		TypedQuery<Lancamento> typedQuery = entityManager.createQuery(criteriaQuery);

		adicionarRestricoesDePaginacao(typedQuery, pageable);

		return new PageImpl<>(typedQuery.getResultList(), pageable, total(lancamentoFilter));
	}

	@Override
	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ResumoLancamento> criteriaQuery = criteriaBuilder.createQuery(ResumoLancamento.class);
		Root<Lancamento> root = criteriaQuery.from(Lancamento.class);

		criteriaQuery.select(criteriaBuilder.construct(ResumoLancamento.class, root.get(Lancamento_.CODIGO),
				root.get(Lancamento_.DESCRICAO), root.get(Lancamento_.DATA_VENCIMENTO),
				root.get(Lancamento_.DATA_PAGAMENTO), root.get(Lancamento_.VALOR), root.get(Lancamento_.TIPO),
				root.get(Lancamento_.CATEGORIA).get(Categoria_.NOME), root.get(Lancamento_.PESSOA).get(Pessoa_.NOME)));

		Predicate[] predicates = criarRestricoes(lancamentoFilter, criteriaBuilder, root);
		criteriaQuery.where(predicates);

		TypedQuery<ResumoLancamento> typedQuery = entityManager.createQuery(criteriaQuery);

		adicionarRestricoesDePaginacao(typedQuery, pageable);

		return new PageImpl<>(typedQuery.getResultList(), pageable, total(lancamentoFilter));
	}

	private Predicate[] criarRestricoes(LancamentoFilter lancamentoFilter, CriteriaBuilder criteriaBuilder,
			Root<Lancamento> root) {
		List<Predicate> predicates = new ArrayList<>();

		if (!StringUtils.isEmpty(lancamentoFilter.getDescricao())) {
			Expression<String> descricao = criteriaBuilder.lower(root.get(Lancamento_.DESCRICAO));
			Predicate predicate = criteriaBuilder.like(descricao,
					"%" + lancamentoFilter.getDescricao().toLowerCase() + "%");

			predicates.add(predicate);
		}

		if (lancamentoFilter.getDataVencimentoDe() != null) {
			Predicate predicate = criteriaBuilder.greaterThanOrEqualTo(root.get(Lancamento_.DATA_VENCIMENTO),
					lancamentoFilter.getDataVencimentoDe());

			predicates.add(predicate);
		}

		if (lancamentoFilter.getDataVencimentoAte() != null) {
			Predicate predicate = criteriaBuilder.lessThanOrEqualTo(root.get(Lancamento_.DATA_VENCIMENTO),
					lancamentoFilter.getDataVencimentoAte());

			predicates.add(predicate);
		}

		return predicates.toArray(new Predicate[predicates.size()]);
	}

	private void adicionarRestricoesDePaginacao(TypedQuery<?> typedQuery, Pageable pageable) {
		int paginaAtual = pageable.getPageNumber();
		int totalRegistrosPorPagina = pageable.getPageSize();
		int primeiroRegistroDaPagina = paginaAtual * totalRegistrosPorPagina;

		typedQuery.setFirstResult(primeiroRegistroDaPagina);
		typedQuery.setMaxResults(totalRegistrosPorPagina);
	}

	private Long total(LancamentoFilter lancamentoFilter) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<Lancamento> root = criteriaQuery.from(Lancamento.class);

		Predicate[] predicates = criarRestricoes(lancamentoFilter, criteriaBuilder, root);
		criteriaQuery.where(predicates);

		criteriaQuery.select(criteriaBuilder.count(root));

		return entityManager.createQuery(criteriaQuery).getSingleResult();
	}

	@Override
	public List<LancamentoEstatisticaCategoriaDto> porCategoria(LocalDate mesReferencial) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<LancamentoEstatisticaCategoriaDto> criteriaQuery = criteriaBuilder
				.createQuery(LancamentoEstatisticaCategoriaDto.class);
		Root<Lancamento> root = criteriaQuery.from(Lancamento.class);

		LocalDate primeiroDia = mesReferencial.withDayOfMonth(1);
		LocalDate ultimoDia = mesReferencial.withDayOfMonth(mesReferencial.lengthOfMonth());

		CompoundSelection<LancamentoEstatisticaCategoriaDto> construct = criteriaBuilder.construct(
				LancamentoEstatisticaCategoriaDto.class, root.get(Lancamento_.categoria),
				criteriaBuilder.sum(root.get(Lancamento_.valor)));

		criteriaQuery.select(construct)
				.where(criteriaBuilder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento), primeiroDia),
						criteriaBuilder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento), ultimoDia))
				.groupBy(root.get(Lancamento_.categoria));

		TypedQuery<LancamentoEstatisticaCategoriaDto> typedQuery = entityManager.createQuery(criteriaQuery);

		return typedQuery.getResultList();
	}

	@Override
	public List<LancamentoEstatisticaDiaDto> porDia(LocalDate mesReferencial) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<LancamentoEstatisticaDiaDto> criteriaQuery = criteriaBuilder
				.createQuery(LancamentoEstatisticaDiaDto.class);
		Root<Lancamento> root = criteriaQuery.from(Lancamento.class);

		LocalDate primeiroDia = mesReferencial.withDayOfMonth(1);
		LocalDate ultimoDia = mesReferencial.withDayOfMonth(mesReferencial.lengthOfMonth());

		CompoundSelection<LancamentoEstatisticaDiaDto> construct = criteriaBuilder.construct(
				LancamentoEstatisticaDiaDto.class,
				root.get(Lancamento_.tipo),
				root.get(Lancamento_.dataVencimento),
				criteriaBuilder.sum(root.get(Lancamento_.valor)));

		criteriaQuery.select(construct)
				.where(criteriaBuilder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento), primeiroDia),
						criteriaBuilder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento), ultimoDia))
				.groupBy(root.get(Lancamento_.tipo), root.get(Lancamento_.dataVencimento));

		TypedQuery<LancamentoEstatisticaDiaDto> typedQuery = entityManager.createQuery(criteriaQuery);

		return typedQuery.getResultList();
	}
	
	@Override
	public List<LancamentoEstatisticaPessoa> porPessoa(LocalDate inicio, LocalDate fim) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<LancamentoEstatisticaPessoa> criteriaQuery = criteriaBuilder
				.createQuery(LancamentoEstatisticaPessoa.class);
		Root<Lancamento> root = criteriaQuery.from(Lancamento.class);

		CompoundSelection<LancamentoEstatisticaPessoa> construct = criteriaBuilder.construct(
				LancamentoEstatisticaPessoa.class,
				root.get(Lancamento_.tipo),
				root.get(Lancamento_.pessoa),
				criteriaBuilder.sum(root.get(Lancamento_.valor)));

		criteriaQuery.select(construct)
				.where(criteriaBuilder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento), inicio),
						criteriaBuilder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento), fim))
				.groupBy(root.get(Lancamento_.tipo), root.get(Lancamento_.pessoa));

		TypedQuery<LancamentoEstatisticaPessoa> typedQuery = entityManager.createQuery(criteriaQuery);

		return typedQuery.getResultList();
	}
}
