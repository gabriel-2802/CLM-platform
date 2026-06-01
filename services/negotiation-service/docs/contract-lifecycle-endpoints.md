# Endpoint-uri lifecycle contract — negotiation-service

## Context

Microserviciul de negocieri (`negotiation-service`) nu știa când un contract era semnat sau când expira/era încetat. Din cauza asta apăreau două probleme:

1. Un contract nou semnat nu apărea deloc în istoricul negocierilor — nu exista nicio negociere "de start" care să marcheze momentul semnării.
2. Job-ul care trimite email-uri de inactivitate (`NegotiationNotificationJob`) includea și clienți ai căror contracte expiraseră sau fuseseră încetate, deși nu mai era relevant să fie contactați.

Soluția: `contract-service` (Gabriel) apelează două endpoint-uri noi din `negotiation-service` în momentele cheie din ciclul de viață al unui contract.

---

## Fișiere adăugate / modificate

### 1. `V5__terminated_contracts.sql` — migrare Flyway

Creează tabelul `negotiations.terminated_contract`:

```sql
CREATE TABLE negotiations.terminated_contract (
    id             BIGSERIAL   PRIMARY KEY,
    contract_id    BIGINT      NOT NULL UNIQUE,
    terminated_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);
```

Acest tabel stochează ID-urile contractelor care au expirat sau au fost încetate. Este folosit ulterior pentru a exclude clienții respectivi din email-urile de inactivitate.

---

### 2. `TerminatedContract.java` — entitate JPA

Mapează tabelul `terminated_contract`. Câmpurile sunt:
- `contractId` — ID-ul contractului terminat (unic)
- `terminatedAt` — momentul la care a fost înregistrat ca terminat (setat automat prin `@PrePersist`)

---

### 3. `TerminatedContractRepository.java` — repository

Expune o singură metodă relevantă:
```java
boolean existsByContractId(Long contractId);
```
Folosită în service pentru a evita duplicate în tabel.

---

### 4. `ContractActivatedRequest.java` — DTO pentru endpoint-ul `/activated`

```java
public record ContractActivatedRequest(
    Long contractId,      // obligatoriu
    Integer clientId,     // obligatoriu
    LocalDate startDate,  // obligatoriu — devine createdAt al negocierii
    LocalDate endDate,    // opțional — devine proposedEndDate
    BigDecimal contractValue // opțional — devine proposedValue
)
```

`endDate` și `contractValue` sunt opționale pentru că nu orice contract are ambele definite la momentul semnării. Dacă `endDate` lipsește, se folosește `startDate` ca fallback (necesar pentru a satisface constraint-ul din DB care cere cel puțin una dintre `proposed_value` sau `proposed_end_date` să fie nenulă).

---

### 5. `ContractDeactivatedRequest.java` — DTO pentru endpoint-ul `/deactivated`

```java
public record ContractDeactivatedRequest(
    List<Long> contractIds  // obligatoriu, cel puțin un element
)
```

Acceptă o listă pentru că pot expira mai multe contracte simultan (ex. un job batch care procesează contracte expirate).

---

### 6. `ContractLifecycleService.java` — logică business

Conține două metode:

#### `onContractActivated(ContractActivatedRequest)`

Creează o negociere inițială cu:
- `status = ACCEPTED` — reprezintă înțelegerea deja acceptată (contractul semnat)
- `createdAt = startDate.atStartOfDay()` — apare corect în ordinea cronologică a istoricului, la data semnării contractului, nu la data apelului API
- `proposedEndDate = endDate` dacă există, altfel `startDate`
- `proposedValue = contractValue` dacă există, altfel `null`
- `createdByUserId = 0` — convenție pentru negocieri create automat de sistem
- `notes = "Negociere inițială — contract semnat"`

#### `onContractsDeactivated(ContractDeactivatedRequest)`

Iterează lista de `contractIds` și pentru fiecare:
- Verifică dacă există deja în `terminated_contract` (idempotent — poate fi apelat de mai multe ori pentru același contract fără efecte secundare)
- Dacă nu există, îl salvează

---

### 7. `ContractLifecycleController.java` — controller REST

Expune două endpoint-uri sub `/api/negotiations/contracts`:

#### `POST /api/negotiations/contracts/activated`

Apelat de `contract-service` după ce un contract este încărcat semnat. Răspunde cu negocierea inițială creată (200 OK + body JSON).

Request body:
```json
{
  "contractId": 10,
  "clientId": 42,
  "startDate": "2026-01-15",
  "endDate": "2027-01-15",
  "contractValue": 5000.00
}
```

#### `POST /api/negotiations/contracts/deactivated`

Apelat de `contract-service` când contracte expiră sau sunt încetate. Răspunde cu 200 OK fără body.

Request body:
```json
{
  "contractIds": [1, 2, 3]
}
```

Ambele endpoint-uri necesită autentificare JWT (token de serviciu generat de `contract-service`, același mecanism folosit deja de `negotiation-service` când apelează `contract-service`).

---

### 8. `NegotiationRepository.java` — query modificat

Query-ul `findClientsInactiveSince` a fost modificat să excludă negocierile asociate contractelor terminate:

```java
// ÎNAINTE
SELECT n.clientId, MAX(n.createdAt)
FROM Negotiation n
GROUP BY n.clientId
HAVING MAX(n.createdAt) < :cutoff

// DUPĂ
SELECT n.clientId, MAX(n.createdAt)
FROM Negotiation n
WHERE n.contractId NOT IN (
    SELECT tc.contractId FROM TerminatedContract tc
)
GROUP BY n.clientId
HAVING MAX(n.createdAt) < :cutoff
```

Efectul: clienții ai căror contracte sunt în `terminated_contract` nu mai apar în raportul de inactivitate și nu mai primesc email-uri de reamintire.

---

## Flux complet

```
contract-service (Gabriel)
    │
    ├─► POST /api/negotiations/contracts/activated
    │       └─► se creează Negotiation (ACCEPTED, createdAt=startDate)
    │
    └─► POST /api/negotiations/contracts/deactivated
            └─► se salvează contractId în terminated_contract
                    └─► findClientsInactiveSince îl exclude din email-uri
```
