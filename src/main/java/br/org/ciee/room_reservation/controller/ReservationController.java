package br.org.ciee.room_reservation.controller;

import br.org.ciee.room_reservation.exception.ValidacaoReservaException;
import br.org.ciee.room_reservation.model.Reserva;
import br.org.ciee.room_reservation.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservas")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Reserva reserva) {
        try {
            boolean ok = service.reservar(reserva);
            if (!ok) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Horário indisponível: já existe reserva sobreposta nesta sala.");
            }
            return ResponseEntity.status(HttpStatus.CREATED).body("Reserva criada com sucesso.");
        } catch (ValidacaoReservaException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Reserva>> listarTodas() {
        return ResponseEntity.ok(service.listarTodasReservas());
    }

    @GetMapping("/{salaId}")
    public ResponseEntity<?> listarPorSala(@PathVariable String salaId) {
        try {
            List<Reserva> lista = service.listarReservasPorSala(salaId);
            return ResponseEntity.ok(lista);
        } catch (ValidacaoReservaException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        try {
            boolean removida = service.excluirReservaPorId(id);
            if (!removida) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Não existe reserva com o id informado.");
            }
            return ResponseEntity.noContent().build();
        } catch (ValidacaoReservaException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
