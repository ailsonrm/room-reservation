package br.org.ciee.room_reservation.repository;

import br.org.ciee.room_reservation.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findBySalaId(String salaId);
}
