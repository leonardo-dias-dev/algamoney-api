package com.algamoney.api.dto;

import java.math.BigDecimal;

import com.algamoney.api.model.Categoria;

public class LancamentoEstatisticaCategoriaDto {
	
	private Categoria categoria;
	
	private BigDecimal total;

	public LancamentoEstatisticaCategoriaDto(Categoria categoria, BigDecimal total) {
		this.categoria = categoria;
		this.total = total;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public void setCategoria(Categoria categoria) {
		this.categoria = categoria;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}
	
}
