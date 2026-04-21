# CLM Notifications Microservice

Microserviciu Spring Boot care trimite lunar un email de digest către angajații firmei de contabilitate, conținând:
- contractele care expiră în următoarele 30 de zile
- clienții cu care nu s-a renegociat în ultimele 6 luni

Comunicarea se face exclusiv prin HTTP cu microserviciul de contracte (`contracts`). Nu accesează direct baza de date.

---

## Cerințe

- Java 25
- Maven
- Microserviciul `contracts` pornit și accesibil (implicit pe portul 8080)
- Un cont Gmail cu **App Password** activat (pentru trimiterea emailurilor)

---

## Configurare înainte de prima pornire

Creează fișierul `src/main/resources/application.properties` (nu este inclus în repo deoarece conține credențiale):

```properties
spring.application.name=notifications
server.port=8082

contracts.api.base-url=http://localhost:8080

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=adresa_ta@gmail.com
spring.mail.password=parola_app_16_caractere
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

notification.recipients=angajat1@firma.ro,angajat2@firma.ro
notification.from=adresa_ta@gmail.com
notification.cron=0 0 8 1 * *
notification.expiry-warning-days=30
notification.inactivity-months=6
```

### Cum generezi App Password pentru Gmail

1. Mergi la [myaccount.google.com](https://myaccount.google.com) → **Security**
2. Activează **2-Step Verification** dacă nu e deja activ
3. Caută **App passwords** → creează unul nou (ex: `CLM Notifications`)
4. Copiază parola de 16 caractere și pune-o la `spring.mail.password`

### Valori configurabile

| Proprietate | Default | Descriere |
|---|---|---|
| `server.port` | `8082` | Portul pe care rulează microserviciul |
| `contracts.api.base-url` | `http://localhost:8080` | Adresa microserviciului de contracte |
| `notification.recipients` | — | Emailurile care primesc raportul (separate prin virgulă) |
| `notification.from` | — | Adresa Gmail care trimite emailul |
| `notification.cron` | `0 0 8 1 * *` | Când se trimite emailul (format cron Spring: secunde minute ore zi lună zi-săptămână) |
| `notification.expiry-warning-days` | `30` | Cu câte zile înainte de expirare apare un contract în raport |
| `notification.inactivity-months` | `6` | Câte luni de inactivitate pentru a fi considerat client nerenegociat |

---

## Pornirea aplicației complete (toate serviciile)

### 1. Pornește baza de date PostgreSQL

```bash
docker-compose up -d
```

Verifică că e ready:
```bash
docker exec clm_postgres pg_isready -U clm_user -d clm_platform
```

### 2. Pornește microserviciul de contracte

```bash
cd contracts
JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64 mvn spring-boot:run
```

Rulează pe portul **8080**. Așteaptă mesajul `Started DemoApplication`.

### 3. Pornește microserviciul de notificări

```bash
cd notifications
JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64 mvn spring-boot:run
```

Rulează pe portul **8082**. Așteaptă mesajul `Started NotificationsApplication`.

### 4. Pornește frontend-ul

```bash
nvm use 20
cd general
npm run dev
```

Rulează pe portul **3000**. Accesează aplicația la [http://localhost:3000](http://localhost:3000).

---

## Endpoint-uri disponibile

| Endpoint | Metodă | Descriere |
|---|---|---|
| `/notifications/preview` | GET | Afișează datele care ar fi incluse în emailul următor, fără a trimite nimic |
| `/notifications/trigger` | POST | Declanșează trimiterea digestului imediat (util pentru testare) |

### Exemple

```bash
# Previzualizează datele fără să trimiți email
curl http://localhost:8082/notifications/preview

# Trimite emailul imediat
curl -X POST http://localhost:8082/notifications/trigger
```

---

## Cum funcționează

```
[Cron lunar / POST /trigger]
        ↓
ContractNotificationJob
        ↓
NotificationService
        ↓                              ↓
GET /api/contracts              GET /api/contracts
/report/expiring                /report/inactive-clients
(contracte ce expiră            (clienți nerenegociați
 în 30 zile)                     de 6 luni)
        ↓                              ↓
        └──────────────┬───────────────┘
                       ↓
              construiește email HTML
                       ↓
              JavaMailSender → SMTP → inbox
```
