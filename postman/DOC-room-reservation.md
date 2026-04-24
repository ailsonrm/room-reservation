# 🏢 Room Reservation API

> REST API para gerenciamento de reservas de salas de reunião, com validação de regras de negócio e prevenção de conflitos de agenda.

---

## 🚀 Sobre o projeto

Esta API foi desenvolvida utilizando **Java + Spring Boot**, com foco em:

- ✔️ Boas práticas de arquitetura (Controller → Service → Repository)
- ✔️ Validação robusta de regras de negócio
- ✔️ Prevenção de conflitos de horários
- ✔️ Código limpo e organizado
- ✔️ Facilidade de execução (H2 em memória)

---

## 🧠 Regras de negócio implementadas

- ⏰ O horário de início deve ser anterior ao horário de fim  
- 📅 Reservas devem ser feitas apenas para o futuro  
- 🚫 Não é permitido conflito de horários na mesma sala  

### 🔥 Regra de conflito

```java
inicio.isBefore(existente.getFim()) &&
fim.isAfter(existente.getInicio())
```

---

## 🛠️ Tecnologias utilizadas

- ☕ Java 17+
- 🚀 Spring Boot
- 🌐 Spring Web
- 🗄️ Spring Data JPA
- ⚡ H2 Database (em memória)
- 🔧 Lombok

---

## 📡 Endpoints da API

### ➕ Criar reserva

POST /reservas

#### 📥 Body

```json
{
  "salaId": "A1",
  "usuarioId": "user1",
  "inicio": "2026-04-25T10:00:00",
  "fim": "2026-04-25T11:00:00"
}
```

---

### 📄 Listar todas reservas

GET /reservas

---

### 📄 Listar por sala

GET /reservas/{salaId}

---

### ❌ Excluir reserva

DELETE /reservas/{id}

---

## ⚙️ Como executar o projeto

### 🔧 Pré-requisitos

- Java 17+
- Maven

---

### ▶️ Rodando a aplicação

```bash
mvn spring-boot:run
```

ou

```bash
./mvnw spring-boot:run
```

---

### 🌐 Acesso

http://localhost:8080

---

### 🗄️ Console H2

http://localhost:8080/h2-console

Configuração:

- JDBC URL: jdbc:h2:mem:testdb
- User: sa
- Password: (vazio)

---

## 🧱 Estrutura do projeto

src/main/java/br/org/ciee/room_reservation
 ├── controller
 ├── service
 ├── repository
 ├── model
 ├── exception
 └── RoomReservationApplication

---

## 🧪 Exemplos de cenários

✔️ Criar reserva válida → sucesso  
❌ Criar reserva no passado → erro  
❌ Criar reserva com conflito → rejeitado  
❌ Intervalo inválido (início > fim) → erro  

---

## 🧠 Decisões técnicas

- Uso do H2 para facilitar testes e execução rápida  
- Validações centralizadas no Service  
- API REST simples e objetiva  
- Separação clara de responsabilidades  

---

## 📌 Diferenciais

- ✔️ Tratamento de erros consistente  
- ✔️ Regra de conflito bem definida  
- ✔️ Código limpo e legível  
- ✔️ Estrutura pronta para evolução (microservices / cloud)

---

## 👨‍💻 Autor

**Ailson Ramos Moreira**

- 💼 Java Backend Developer / Tech Lead
- 🚀 Foco em arquitetura, APIs e sistemas escaláveis

---

## 📦 Código fonte (principais classes)

🚀 Application
```java
@SpringBootApplication
public class RoomReservationApplication {
    public static void main(String[] args) {
        SpringApplication.run(RoomReservationApplication.class, args);
    }
}
```

📦 Entity
```java
@Entity
@Table(name = "reservas")
@Getter
@Setter
@Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String salaId;
    private String usuarioId;
    private LocalDateTime inicio;
    private LocalDateTime fim;
}
```

⚠️ Exception
```java
public class ValidacaoReservaException extends RuntimeException {
    public ValidacaoReservaException(String mensagem) {
        super(mensagem);
    }
}
```

🗄️ Repository
```java
@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findBySalaId(String salaId);
}
```

🧠 Service
```java
@Service
public class ReservationService {

    public boolean reservar(Reserva reserva) {
        validar(reserva);

        if (!estaDisponivel(reserva.getSalaId(), reserva.getInicio(), reserva.getFim())) {
            return false;
        }

        repository.save(reserva);
        return true;
    }

    public boolean estaDisponivel(String salaId, LocalDateTime inicio, LocalDateTime fim) {
        return repository.findBySalaId(salaId)
            .stream()
            .noneMatch(r ->
                inicio.isBefore(r.getFim()) &&
                fim.isAfter(r.getInicio())
            );
    }
}
```

🌐 Controller

```java
@RestController
@RequestMapping("/reservas")
public class ReservationController {

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Reserva reserva) {
        boolean ok = service.reservar(reserva);

        if (!ok) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Horário indisponível");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
            .body("Reserva criada");
    }
}
```