package br.org.ciee.room_reservation.exception;

/**
 * Violação das regras de validação da reserva (intervalo de datas, horário no futuro, campos obrigatórios).
 */
public class ValidacaoReservaException extends RuntimeException {

    public ValidacaoReservaException(String mensagem) {
        super(mensagem);
    }
}
