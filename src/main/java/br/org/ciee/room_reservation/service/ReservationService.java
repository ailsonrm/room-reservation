package br.org.ciee.room_reservation.service;

import br.org.ciee.room_reservation.exception.ValidacaoReservaException;
import br.org.ciee.room_reservation.model.Reserva;
import br.org.ciee.room_reservation.repository.ReservaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservaRepository repository;

    public ReservationService(ReservaRepository repository) {
        this.repository = repository;
    }

    public boolean reservar(Reserva reserva) {
        validar(reserva);

        if (!estaDisponivel(reserva.getSalaId(), reserva.getInicio(), reserva.getFim())) {
            return false;
        }

        repository.save(reserva);
        return true;
    }

    public boolean estaDisponivel(String salaId, LocalDateTime inicio, LocalDateTime fim) {
        validarJanelaConsulta(salaId, inicio, fim);

        List<Reserva> reservasNaSala = repository.findBySalaId(salaId);

        return reservasNaSala.stream().noneMatch(existente ->
                inicio.isBefore(existente.getFim()) && fim.isAfter(existente.getInicio())
        );
    }

    public List<Reserva> listarReservasPorSala(String salaId) {
        if (salaId == null || salaId.isBlank()) {
            throw new ValidacaoReservaException("O identificador da sala é obrigatório.");
        }
        return repository.findBySalaId(salaId);
    }

    public List<Reserva> listarTodasReservas() {
        return repository.findAll();
    }

    /**
     * Remove a reserva pelo identificador numérico persistido.
     *
     * @return {@code true} se existia e foi removida; {@code false} se não há reserva com esse id
     */
    public boolean excluirReservaPorId(Long id) {
        if (id == null || id < 1) {
            throw new ValidacaoReservaException("O identificador da reserva (id) deve ser um número positivo.");
        }
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    private void validar(Reserva reserva) {
        if (reserva.getSalaId() == null || reserva.getSalaId().isBlank()) {
            throw new ValidacaoReservaException("O identificador da sala (salaId) é obrigatório.");
        }
        if (reserva.getUsuarioId() == null || reserva.getUsuarioId().isBlank()) {
            throw new ValidacaoReservaException("O identificador do usuário (usuarioId) é obrigatório.");
        }
        if (reserva.getInicio() == null || reserva.getFim() == null) {
            throw new ValidacaoReservaException("Início e fim devem ser informados.");
        }
        if (!reserva.getInicio().isBefore(reserva.getFim())) {
            throw new ValidacaoReservaException("O início deve ser anterior ao fim.");
        }
        if (!reserva.getInicio().isAfter(LocalDateTime.now())) {
            throw new ValidacaoReservaException("A reserva deve ter início no futuro.");
        }
    }

    private void validarJanelaConsulta(String salaId, LocalDateTime inicio, LocalDateTime fim) {
        if (salaId == null || salaId.isBlank()) {
            throw new ValidacaoReservaException("O identificador da sala é obrigatório.");
        }
        if (inicio == null || fim == null) {
            throw new ValidacaoReservaException("Início e fim devem ser informados.");
        }
        if (!inicio.isBefore(fim)) {
            throw new ValidacaoReservaException("O início deve ser anterior ao fim.");
        }
    }
}
