package nttdata.personal.julius.api.infrastructure.client.dto;

import java.math.BigDecimal;
import java.util.List;

public record ExchangeRateResponse(
        List<Cotacao> cotacoes,
        String moeda,
        String data
) {
    public record Cotacao(
            BigDecimal cotacao_compra,
            BigDecimal cotacao_venda,
            String data_hora_cotacao,
            String tipo_boletim
    ) {}
}
