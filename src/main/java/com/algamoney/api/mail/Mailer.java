package com.algamoney.api.mail;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.algamoney.api.model.Lancamento;
import com.algamoney.api.model.Usuario;

@Component
public class Mailer {

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private TemplateEngine templateEngine;

	public void enviarEmail(String remetente, List<String> destinatarios, String assunto, String mensagem) {
		try {
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");

			mimeMessageHelper.setFrom(remetente);
			mimeMessageHelper.setTo(destinatarios.toArray(new String[destinatarios.size()]));
			mimeMessageHelper.setSubject(assunto);
			mimeMessageHelper.setText(mensagem, true);

			javaMailSender.send(mimeMessage);
		} catch (MessagingException e) {
			throw new RuntimeException("Problemas com o envio de e-mail.", e);
		}
	}

	public void enviarEmail(String remetente, List<String> destinatarios, String assunto, String template,
			Map<String, Object> variaveis) {
		Context context = new Context(new Locale("pt", "BR"));

		variaveis.entrySet().forEach(e -> context.setVariable(e.getKey(), e.getValue()));

		String mensagem = templateEngine.process(template, context);

		enviarEmail(remetente, destinatarios, assunto, mensagem);
	}

	public void avisarSobreLancamentosVencidos(List<Lancamento> lancamentos, List<Usuario> usuarios) {
		Map<String, Object> variaveis = new HashMap<>();
		variaveis.put("lancamentos", lancamentos);

		List<String> emails = usuarios.stream().map(u -> u.getEmail()).collect(Collectors.toList());
		
		enviarEmail("ld773871@gmail.com", emails, "Lançamentos vencidos", "mail/aviso-lancamentos-vencidos", variaveis);
	}

//	@EventListener
//	public void teste(ApplicationReadyEvent applicationReadyEvent) {
//		String template = "mail/aviso-lancamentos-vencidos";
//		List<Lancamento> lancamentos = lancamentoRepository.findAll();
//
//		Map<String, Object> variaveis = new HashMap<>();
//		variaveis.put("lancamentos", lancamentos);
//
//		enviarEmail("ld773871@gmail.com", Arrays.asList("leonardosd.ti@gmail.com"), "E-mail curso Angular FullStack",
//				template, variaveis);
//
//		System.out.println("Terminado o envio de e-mail.");
//	}

}
