# Quiz App

Aplicatie full-stack pentru gestionarea si sustinerea de sesiuni de quiz in timp real.

## Tehnologii

- **Backend**: Spring Boot 3, Spring Security, Spring Data JPA, WebSockets (STOMP)
- **Frontend**: React 18, Vite, React Router, Axios, STOMP client
- **Baza de date**: MySQL 8
- **Containerizare**: Docker & Docker Compose

## Functionalitati

- Autentificare si autorizare bazata pe JWT (roluri: `PROFESSOR`, `STUDENT`)
- CRUD intrebari pentru profesori
- Creare, activare si inchidere sesiuni de quiz
- Raspuns la intrebari de catre studenti
- Afisare live a distributiei raspunsurilor prin WebSockets
- Vizualizare scor individual dupa finalizarea quiz-ului

## Structura proiect

```
quiz.app/
  backend/        # aplicatia Spring Boot
  frontend/       # aplicatia React
  docker-compose.yml
```

## Rulare cu Docker Compose

1. Asigura-te ca ai instalat Docker si Docker Compose.
2. Din directorul radacina (`quiz.app/`) ruleaza:

```powershell
docker compose up --build
```

3. Aplicatia va fi disponibila la:
   - Frontend: http://localhost:5173
   - Backend: http://localhost:8080
   - MySQL: localhost:3306 (user `root`, parola `root`)

## Rulare locala fara Docker

### Backend

```powershell
cd backend
mvn spring-boot:run
```

Configureaza variabilele de mediu pentru conexiunea la baza de date daca este necesar:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/quiz_app"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="root"
```

### Frontend

```powershell
cd frontend
npm install
npm run dev
```

Aplicatia React ruleaza implicit pe http://localhost:5173 si face proxy catre backend-ul de la http://localhost:8080.

## Endpoints principale

- `POST /api/auth/register` – inregistrare (profesor / student)
- `POST /api/auth/login` – autentificare
- `GET /api/questions` – lista intrebarilor profesorului curent
- `POST /api/questions` – creare intrebare
- `POST /api/professor/sessions` – creare sesiune quiz
- `POST /api/professor/sessions/{id}/activate` – activare sesiune
- `POST /api/student/sessions/{code}/answers` – trimitere raspunsuri student
- `GET /api/student/sessions/{code}/score` – scor individual

## Note suplimentare

- In fisierul `backend/src/main/resources/application.yml` poti ajusta secretul JWT, durata token-ului si credentialele bazei de date.
- WebSocket endpoint-ul este expus la `/ws`, iar mesajele live sunt transmise pe `/topic/sessions/{sessionId}/questions/{questionId}`.
- Pentru producties, inlocuieste valorile implicite (de exemplu secretul JWT) cu unele sigure.
