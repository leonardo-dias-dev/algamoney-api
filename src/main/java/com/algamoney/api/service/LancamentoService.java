package com.algamoney.api.service;

import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.algamoney.api.dto.LancamentoEstatisticaCategoriaDto;
import com.algamoney.api.dto.LancamentoEstatisticaDiaDto;
import com.algamoney.api.dto.LancamentoEstatisticaPessoa;
import com.algamoney.api.mail.Mailer;
import com.algamoney.api.model.Lancamento;
import com.algamoney.api.model.Lancamento_;
import com.algamoney.api.model.Pessoa;
import com.algamoney.api.model.Usuario;
import com.algamoney.api.repository.LancamentoRepository;
import com.algamoney.api.repository.PessoaRepository;
import com.algamoney.api.repository.UsuarioRepository;
import com.algamoney.api.repository.filter.LancamentoFilter;
import com.algamoney.api.repository.projection.ResumoLancamento;
import com.algamoney.api.service.exception.PessoaInexistenteOuInativaException;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class LancamentoService {

	private static final String DESTINATARIOS = "ROLE_PESQUISAR_LANCAMENTO";

	private static final Logger log = LoggerFactory.getLogger(LancamentoService.class);

	@Autowired
	private LancamentoRepository lancamentoRepository;

	@Autowired
	private PessoaRepository pessoaRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private Mailer mailer;

	public Page<Lancamento> pesquisar(LancamentoFilter lancamentoFilter, Pageable pageable) {
		return lancamentoRepository.filtrar(lancamentoFilter, pageable);
	}

	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable) {
		return lancamentoRepository.resumir(lancamentoFilter, pageable);
	}

	public Lancamento buscarPeloCodigo(Long codigo) {
		Optional<Lancamento> optionalLancamento = lancamentoRepository.findById(codigo);

		return optionalLancamento.orElseThrow(() -> new EmptyResultDataAccessException(1));
	}

	public Lancamento incluir(Lancamento lancamento) {
		Optional<Pessoa> optionalPessoa = pessoaRepository.findById(lancamento.getPessoa().getCodigo());
		
		if (!optionalPessoa.isPresent() || optionalPessoa.get().isInativo()) {
			throw new PessoaInexistenteOuInativaException();
		}
		
		if (StringUtils.hasText(lancamento.getAnexo())) {
			// s3.salvar(lancamento.getAnexo());
		}

		return lancamentoRepository.save(lancamento);
	}

	public void remover(Long codigo) {
		lancamentoRepository.deleteById(codigo);
	}

	public Lancamento atualizar(Long codigo, Lancamento lancamento) {
		Lancamento lancamentoSalvo = buscarLancamentoExistente(codigo);

		validarPessoa(lancamento, lancamentoSalvo);
		
		if (StringUtils.isEmpty(lancamento.getAnexo()) && StringUtils.hasText(lancamento.getAnexo())) {
			// s3.remover(lancamento.getAnexo());
		} else if (StringUtils.hasLength(lancamento.getAnexo()) && !lancamento.getAnexo().equals(lancamentoSalvo.getAnexo())) {
			// s3.substituir(lancamentoSalvo.getAnexo(), lancamento.getAnexo());
		}

		BeanUtils.copyProperties(lancamento, lancamentoSalvo, Lancamento_.CODIGO);

		return lancamentoRepository.save(lancamentoSalvo);
	}

	public List<LancamentoEstatisticaCategoriaDto> porCategoria() {
		return this.lancamentoRepository.porCategoria(LocalDate.now());
	}

	public List<LancamentoEstatisticaDiaDto> porDia() {
		return this.lancamentoRepository.porDia(LocalDate.now());
	}

	public byte[] relatorioPorPessoa(LocalDate inicio, LocalDate fim) throws Exception {
		List<LancamentoEstatisticaPessoa> dados = lancamentoRepository.porPessoa(inicio, fim);

		Map<String, Object> parametros = new HashMap<>();
		parametros.put("DT_INICIO", Date.valueOf(inicio));
		parametros.put("DT_FIM", Date.valueOf(fim));
		parametros.put("REPORT_LOCALE", new Locale("pt", "BR"));

		InputStream inputStream = this.getClass().getResourceAsStream("/relatorios/lancamentos-por-pessoa.jasper");
		JasperPrint jasperPrint = JasperFillManager.fillReport(inputStream, parametros, new JRBeanCollectionDataSource(dados));

		return JasperExportManager.exportReportToPdf(jasperPrint);
	}

	@Scheduled(cron = "0 0 6 * * *")
	public void avisarSobreLancamentoVencidos() {

		if (log.isDebugEnabled()) {
			log.debug("Preparando envio de e-mails de aviso de lanãmentos vencidos.");
		}

		List<Lancamento> lancamentos = lancamentoRepository.findByDataVencimentoLessThanEqualAndDataPagamentoIsNull(LocalDate.now());

		if (lancamentos.isEmpty()) {
			log.debug("Sem lançamentos vencidos para aviso");
			return;
		}

		log.debug("Existem {} lancamentos vencidos.", lancamentos.size());

		List<Usuario> usuarios = usuarioRepository.findByPermissoesDescricao(DESTINATARIOS);

		if (usuarios.isEmpty()) {
			log.warn("Existem lançamentos vencidos, mas o sistema não encontrou destinatários.");
			return;
		}

		mailer.avisarSobreLancamentosVencidos(lancamentos, usuarios);

		log.info("Envio de e-mail de aviso concluído.");
	}

	private Lancamento buscarLancamentoExistente(Long codigo) {
		Optional<Lancamento> optionalLancamento = lancamentoRepository.findById(codigo);

		return optionalLancamento.orElseThrow(() -> new IllegalArgumentException());
	}

	private void validarPessoa(Lancamento lancamento, Lancamento lancamentoSalvo) {
		if (lancamento.getPessoa().equals(lancamentoSalvo.getPessoa())) {
			return;
		}

		Optional<Pessoa> optional = pessoaRepository.findById(lancamento.getPessoa().getCodigo());

		if (!optional.isPresent() || optional.get().isInativo()) {
			throw new PessoaInexistenteOuInativaException();
		}
	}

}