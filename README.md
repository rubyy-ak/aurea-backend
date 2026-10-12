# Auréa – Backend

**Auréa** ist eine Web-App für die eigene Parfümsammlung. Man trägt seine Flakons ein, führt eine Wunschliste und bekommt jeden Tag einen Duftvorschlag, der zum aktuellen Wetter in Berlin passt.

Tech-Stack: **Spring Boot 4 (Java 25)** · **Vue 3 + Vite** · **PostgreSQL** · Deployment auf **Render** · CI mit **GitHub Actions**

**Team:** Ruby Aktar, _Partner/in eintragen_
**Frontend-Repository:** https://github.com/rubyy-ak/aurea-frontend

**Live-App:** https://aurea-frontend-yytz.onrender.com
**Backend-API:** https://aurea-backend-5vbu.onrender.com/api/examples

## Use-Cases

| # | Use-Case | Wo in der App |
|---|----------|---------------|
| 1 | Konto erstellen & anmelden (Passwort wird als BCrypt-Hash gespeichert) | Login-Seite |
| 2 | Parfüm hinzufügen (Duftpyramide, Größe, Preis, Bewertung, Favorit) | „+ Parfüm“ |
| 3 | Sammlung ansehen, durchsuchen, filtern (auch „Nur Favoriten“) & sortieren | „Sammlung“ |
| 4 | Parfüm bearbeiten | Detailseite → „Bearbeiten“ |
| 5 | Parfüm löschen (mit Bestätigungsdialog) | Detailseite → „Löschen“ |
| 6 | Wunschliste führen & gekaufte Düfte in die Sammlung übernehmen | „Wunschliste“ → „Gekauft ✓“ |
| 7 | Duft für heute passend zum Wetter, Favoriten bevorzugt (externe API: Open-Meteo) | Startseite |

### Zusatzpunkte
- **Authentisierung & Multi-User:** Token-Login. Jeder Nutzer sieht nur seine eigenen Parfüms (wird in den Backend-Tests geprüft). Im Backend liegt nur der SHA-256-Hash des Tokens.
- **Externe API:** [Open-Meteo](https://open-meteo.com) liefert das aktuelle Wetter, ohne API-Key. Ist der Dienst nicht erreichbar, entscheidet die Jahreszeit.
- **Input-Validierung:** im Frontend (sofortiges Feedback) **und** im Backend (Bean Validation, deutsche Fehlermeldungen pro Feld).
- **Keine Credentials im Repository:** Alle Zugangsdaten kommen auf Render aus Umgebungsvariablen.

## Aufbau

```
src/main/java/de/htwberlin/aurea/
  controller/  REST-Endpunkte (Auth, Perfume, Health)
  service/     Geschäftslogik (Auth, Perfume, Suggestion)
  repository/  Spring Data JPA
  entity/      JPA-Entitäten (AppUser, AuthToken, Perfume) + Enums
  dto/         Request-/Response-Objekte mit Validierung
  security/    Token-Filter
  weather/     Open-Meteo-Client (externe API)
  exception/   Einheitliche Fehlerantworten
```

## REST-API

| Methode | Pfad | Beschreibung |
|---------|------|--------------|
| POST | `/api/auth/register` | Konto anlegen → Token |
| POST | `/api/auth/login` | Anmelden → Token |
| POST | `/api/auth/logout` | Token ungültig machen |
| GET | `/api/auth/me` | Aktueller Nutzer |
| GET | `/api/perfumes?status=&family=&q=&favorite=&sort=` | Eigene Parfüms, gefiltert |
| GET | `/api/perfumes/{id}` | Ein Parfüm |
| POST | `/api/perfumes` | Parfüm anlegen |
| PUT | `/api/perfumes/{id}` | Parfüm ändern |
| DELETE | `/api/perfumes/{id}` | Parfüm löschen |
| POST | `/api/perfumes/{id}/purchase` | Wunschliste → Sammlung |
| GET | `/api/perfumes/suggestion` | Duft des Tages (Wetter) |
| GET | `/api/examples` | Liste von Beispiel-Parfüms (öffentlich, ohne Login) |
| GET | `/api/health` | Healthcheck (öffentlich) |

Alle Endpunkte außer Registrieren, Login, Beispiele und Health erwarten den Header `Authorization: Bearer <token>`.

## Lokal starten

Voraussetzung: Java 25. Lokal wird eine H2-Datei-Datenbank in `data/` verwendet, in Produktion PostgreSQL.

```bash
./gradlew bootRun     # http://localhost:8080
./gradlew test        # 23 Tests
```

## Deployment auf Render

1. Auf Render **New → Blueprint** wählen und dieses Repository auswählen. Render liest `render.yaml` und legt die PostgreSQL-Datenbank und das Backend an.
2. Bei `CORS_ALLOWED_ORIGINS` die URL des Frontends eintragen, hier `https://aurea-frontend-yytz.onrender.com`.
3. Die Datenbank-Zugangsdaten setzt Render automatisch als Umgebungsvariablen. Im Repository stehen keine Credentials.

GitHub Actions führt bei jedem Push die Tests aus. Render deployt erst, wenn sie grün sind.
