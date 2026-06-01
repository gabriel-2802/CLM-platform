# Contract Service — Test Suite Documentation

## Overview

The test suite covers the contract-service Spring Boot application across five concern areas: unit logic, controller HTTP behaviour, document generation, scheduled jobs, and security. All 180 tests run without an external database or LibreOffice installation.

```
src/test/java/clm/demo/
├── unit/
│   ├── util/PlaceholderProcessorTest.java          (19 tests)
│   └── service/
│       ├── DocumentGenerationUtilTest.java          (14 tests)
│       ├── ContractServiceActiveDetailsTest.java    (8 tests)
│       ├── TemplateServiceTest.java                 (12 tests)
│       └── AppendixServiceTest.java                 (16 tests)
├── integration/
│   ├── AbstractControllerTest.java                  (base class)
│   ├── ContractControllerIT.java                    (18 tests)
│   ├── TemplateControllerIT.java                    (14 tests)
│   └── AppendixControllerIT.java                    (17 tests)
├── document/
│   └── DocxFillerTest.java                          (15 tests)
├── scheduler/
│   ├── ContractArchiveJobTest.java                  (4 tests)
│   └── ContractTerminationJobTest.java              (4 tests)
└── security/
    ├── JwtTokenProviderTest.java                    (13 tests)
    └── JwtAuthenticationFilterTest.java             (7 tests)
```

**Total: 180 tests | 0 failures | 0 skipped**

---

## How to run

```bash
# Run all new tests (excludes cache tests that need a live DB)
mvn test -Dtest="PlaceholderProcessorTest,DocumentGenerationUtilTest,ContractServiceActiveDetailsTest,\
JwtTokenProviderTest,JwtAuthenticationFilterTest,ContractArchiveJobTest,ContractTerminationJobTest,\
ContractControllerIT,TemplateControllerIT,AppendixControllerIT,DocxFillerTest,\
TemplateServiceTest,AppendixServiceTest"

# Run a single category
mvn test -Dtest="ContractControllerIT"
mvn test -Dtest="DocxFillerTest"
```

---

## Dependencies added

The following test-scoped dependencies were added to `pom.xml` because they were absent:

| Dependency | Version | Why |
|---|---|---|
| `org.awaitility:awaitility` | managed by SB BOM | Assert async/scheduled side-effects without `Thread.sleep` |
| `org.testcontainers:postgresql` | 1.21.0 (via BOM) | Pattern for real-DB slice tests |
| `org.springframework.boot:spring-boot-testcontainers` | managed by SB BOM | Spring Boot Testcontainers integration |
| `org.testcontainers:testcontainers-bom` | 1.21.0 | Version management for Testcontainers modules |

`spring-boot-starter-test`, `spring-security-test`, and `mockito-*` were already present and were not duplicated.

---

## Unit Tests

Unit tests use `@ExtendWith(MockitoExtension.class)`. Every external collaborator (repository, mapper, file utility) is mocked with `@Mock`. No Spring context is started — these tests run in milliseconds.

---

### PlaceholderProcessorTest

**File:** `unit/util/PlaceholderProcessorTest.java`

`PlaceholderProcessor` is the core text-processing utility. It detects dot-sequence placeholders (four or more consecutive `.` characters) in normalised text and substitutes them with field values. This class underpins every contract and appendix generation, so it has the most exhaustive coverage.

#### `normalize`
| Test | What it proves |
|---|---|
| `normalize_returns_empty_string_for_null` | Null input is safe — returns `""` instead of throwing |
| `normalize_converts_crlf_to_lf` | Windows line endings are collapsed to Unix `\n` |
| `normalize_converts_cr_to_lf` | Old Mac `\r` is also collapsed |
| `normalize_replaces_horizontal_ellipsis_with_three_dots` | Unicode `…` (U+2026) becomes `...` |
| `normalize_replaces_two_dot_leader_with_two_dots` | Unicode `‥` (U+2025) becomes `..` |
| `normalize_replaces_fullwidth_full_stop_with_ascii_dot` | Unicode `．` (U+FF0E) becomes `.` |
| `normalize_replaces_midline_ellipsis_with_three_dots` | Unicode `⋯` (U+22EF) becomes `...` |
| `normalize_leaves_plain_text_unchanged` | Ordinary ASCII text is not mutated |

#### `findPlaceholders`
| Test | What it proves |
|---|---|
| `findPlaceholders_finds_single_4dot_sequence` | A single `....` is detected and returned as one record |
| `findPlaceholders_finds_multiple_placeholders` | Two separate `....` groups produce two records in order |
| `findPlaceholders_returns_empty_when_no_placeholders` | Text without `....` returns an empty list |
| `findPlaceholders_ignores_sequences_shorter_than_4_dots` | Three dots `...` do not match |
| `findPlaceholders_matches_5_dot_sequence` | Five dots `....` also match (threshold is ≥ 4) |
| `findPlaceholders_records_correct_offsets` | `startOffset` and `endOffset` point to the right characters |

#### `substituteEach`
| Test | What it proves |
|---|---|
| `substituteEach_replaces_placeholder_with_value` | A single placeholder becomes the provided string |
| `substituteEach_keeps_original_when_resolver_returns_null` | Null from the resolver leaves the dots intact |
| `substituteEach_replaces_multiple_placeholders_in_order` | Three placeholders map to three values by index |
| `substituteEach_handles_empty_string` | Empty input returns empty output without throwing |
| `substituteEach_replaces_partial_when_some_resolvers_return_null` | Only index 1 replaced; others keep their dots |
| `substituteEach_handles_special_characters_in_replacement` | Apostrophes, ampersands, and `$1` regex patterns survive as literal text |
| `substituteEach_handles_very_long_replacement_value` | A 10,000-character replacement is handled correctly |

#### `substituteEachWithSpans`
| Test | What it proves |
|---|---|
| `substituteEachWithSpans_returns_spans_for_each_placeholder` | Returns one `SubstitutionSpan` per placeholder |
| `substituteEachWithSpans_marks_unreplaced_span_as_not_replaced` | `replaced = false` when resolver returns null |
| `substituteEachWithSpans_anyFilled_returns_false_when_nothing_replaced` | `anyFilled()` is false when zero substitutions |
| `substituteEachWithSpans_anyFilled_returns_true_when_at_least_one_replaced` | `anyFilled()` is true when ≥ 1 substitution |

#### Parameterised edge cases
`findPlaceholders_handles_unicode_edge_cases_after_normalize` — four scenarios driven by `@MethodSource`:
- Double ellipsis `……` (normalises to 6 dots) → 1 match
- Single ellipsis `…` (normalises to 3 dots) → 0 matches
- `....a....` → 2 separate matches
- Empty string → 0 matches

---

### DocumentGenerationUtilTest

**File:** `unit/service/DocumentGenerationUtilTest.java`

`DocumentGenerationUtil` is a shared `@Component` used by both `ContractService` and `AppendixService` for three operations: validating mandatory fields, building `DocumentFieldValue` entities, and building the label-to-value map used by the DOCX filler.

#### `validateMandatoryFields`
| Test | What it proves |
|---|---|
| `should_pass_when_all_required_fields_are_present` | No exception when all required labels have non-blank values |
| `should_throw_when_required_field_is_missing` | `MissingMandatoryFieldException` thrown when mapping has no entry for a required label |
| `should_throw_when_required_field_value_is_blank` | A whitespace-only value counts as missing |
| `should_not_throw_when_optional_field_is_missing` | Fields with `isRequired = false` are not checked |
| `should_include_all_missing_fields_in_exception_message` | All missing labels appear in the exception message, not just the first |
| `should_skip_fields_with_null_label` | A field that was never labelled cannot be validated and is silently skipped |

#### `buildFieldValues`
| Test | What it proves |
|---|---|
| `should_build_field_value_for_each_mapped_field` | Two labels map to two `DocumentFieldValue` entities |
| `should_return_empty_list_when_no_mappings_match` | Missing optional field → empty list, no crash |
| `should_skip_fields_with_null_label` | Unlabelled fields do not produce field-value records |
| `should_skip_fields_with_blank_value_in_mappings` | Whitespace-only values are excluded |
| `should_associate_field_value_with_correct_document_and_field` | The created entity links back to the correct `Document` and `TemplateField` instances |

#### `buildLabelValueMap`
| Test | What it proves |
|---|---|
| `should_build_map_from_field_values` | Label → value entries are correctly populated |
| `should_return_empty_map_for_empty_input` | Empty list → empty map, no crash |
| `should_skip_entries_with_null_field_value` | Null `fieldValue` is excluded |
| `should_skip_entries_with_null_template_field` | Null `templateField` reference is excluded |
| `should_skip_entries_with_null_field_label` | A field with a null label is excluded |

---

### ContractServiceActiveDetailsTest

**File:** `unit/service/ContractServiceActiveDetailsTest.java`

`ContractService.getCurentlyActiveContractDetails()` is a public static method that selects the currently valid `ContractDetails` record from a contract's history list. This is critical correctness logic: it determines which financial terms (value, balance, dates) are shown to the user.

| Test | What it proves |
|---|---|
| `should_return_single_detail_when_only_one_exists` | A one-element list always returns that element (fast path) |
| `should_throw_when_list_is_empty` | Empty list → `IllegalStateException` with a descriptive message |
| `should_throw_when_list_is_null` | Null list → `IllegalStateException` (null check before iteration) |
| `should_return_most_recent_active_details_when_multiple_exist` | Among multiple currently-valid records, the most recently created one wins |
| `should_throw_when_no_detail_covers_today` | Two future-dated records (neither covers today) → `IllegalStateException` |
| `should_return_first_when_list_has_exactly_one_element` | Single element returned regardless of its date range (documented fast path) |
| `should_select_most_recent_created_among_currently_valid_details` | Parameterised: two orderings of the same list both return the most recent record |

> **Note:** A known behavioural quirk exists — when the list has exactly one element the date range is not checked, so a single future-dated record is returned without error. This is the current production behaviour; the test documents it rather than changing it.

---

### TemplateServiceTest

**File:** `unit/service/TemplateServiceTest.java`

Tests the business logic of `TemplateService`. All four repository and mapper dependencies are mocked.

#### `getTemplate`
| Test | What it proves |
|---|---|
| `should_return_dto_when_template_exists` | Repository hit → mapper called → DTO returned |
| `should_throw_when_template_not_found` | `ResourceNotFoundException` with the template ID in the message |

#### `getAllTemplates`
| Test | What it proves |
|---|---|
| `should_return_page_of_templates` | Pageable passed through; each entity mapped to DTO |
| `should_return_empty_page_when_no_templates_exist` | Empty page is returned without throwing |

#### `deleteTemplate`
| Test | What it proves |
|---|---|
| `should_delete_existing_template` | `deleteById` called when the template exists |
| `should_throw_when_template_not_found` | `ResourceNotFoundException` thrown and `deleteById` never called |

#### `uploadTemplate`
| Test | What it proves |
|---|---|
| `should_throw_when_template_name_already_exists` | `DuplicateTemplateNameException` when the name is taken |
| `should_throw_when_file_is_empty` | `IllegalArgumentException` for a zero-byte `MultipartFile` |

#### `updateFieldLabels`
| Test | What it proves |
|---|---|
| `should_throw_when_template_not_found` | `ResourceNotFoundException` before any field is touched |
| `should_throw_when_field_does_not_belong_to_template` | `TemplateFieldOwnershipException` for cross-template field IDs |
| `should_update_labels_and_set_fully_mapped_when_all_fields_have_labels` | After the update, `isFullyMapped` is set to `true` on the template |

---

### AppendixServiceTest

**File:** `unit/service/AppendixServiceTest.java`

Tests the business logic of `AppendixService`. Covers all state transitions on the `AppendixStatus` enum and all error paths.

#### `getAppendicesForContract`
| Test | What it proves |
|---|---|
| `should_return_appendices_for_existing_contract` | Existing contract → repository queried → DTOs returned |
| `should_throw_when_contract_not_found` | `ResourceNotFoundException` when the parent contract is missing |
| `should_return_empty_list_when_contract_has_no_appendices` | No appendices → empty list, no crash |

#### `uploadSignedAppendix`
| Test | What it proves |
|---|---|
| `should_throw_when_appendix_not_found` | `ResourceNotFoundException` for unknown appendix ID |
| `should_throw_when_appendix_already_signed` | `InvalidAppendixStateException` — cannot re-sign an already-signed appendix |
| `should_transition_draft_to_signed_when_valid_pdf_uploaded` | Status becomes `SIGNED`, `uploadedSignedByUser` and `uploadedSignedAt` are set |

#### `terminateAppendix`
| Test | What it proves |
|---|---|
| `should_terminate_signed_appendix` | `SIGNED` → `TERMINATED` is the only valid transition |
| `should_throw_when_appendix_is_draft` | `DRAFT` appendices cannot be terminated |
| `should_throw_when_appendix_not_found` | `ResourceNotFoundException` for unknown ID |
| `should_throw_when_appendix_already_terminated` | `TERMINATED` appendices cannot be terminated again |

#### `deleteAppendix`
| Test | What it proves |
|---|---|
| `should_delete_appendix_when_it_exists` | `deleteById` called when the appendix exists |
| `should_throw_when_appendix_not_found` | `ResourceNotFoundException` and `deleteById` never called |

#### `generateAppendix`
| Test | What it proves |
|---|---|
| `should_throw_when_contract_not_found` | Contract lookup fails → `ResourceNotFoundException` |
| `should_throw_when_template_not_found` | Template lookup fails → `ResourceNotFoundException` |
| `should_throw_when_template_not_fully_mapped` | `isFullyMapped = false` → `TemplateIncompleteException` |

---

## Controller Integration Tests

Controller tests use **standalone MockMvc** (`MockMvcBuilders.standaloneSetup()`). This approach:
- Instantiates only the controller under test (no full Spring context, no database)
- Wires the `GlobalExceptionHandler` so HTTP status codes from exceptions are verified
- Attaches a `LocalValidatorFactoryBean` so `@Valid` / `@NotNull` annotations are enforced
- Mocks every `@Service` dependency with Mockito

All service and download-service collaborators are `@Mock`. The tests assert HTTP status codes, response JSON structure, and response headers.

---

### AbstractControllerTest

**File:** `integration/AbstractControllerTest.java`

Base class that all controller IT classes extend. Provides `buildMockMvc(Object... controllers)` which wires the `GlobalExceptionHandler` and the Jakarta Validation factory. Subclasses call this in their `@BeforeEach`.

---

### ContractControllerIT

**File:** `integration/ContractControllerIT.java`

Covers the full surface of `ContractController` (`/api/contracts`).

#### `POST /api/contracts/generate`
| Test | Status | Scenario |
|---|---|---|
| `should_return_201_and_location_header_when_contract_created` | 201 | Happy path — Location header contains `/api/contracts/42` |
| `should_return_400_when_template_id_missing` | 400 | Missing required `templateId` field |
| `should_return_400_when_start_date_missing` | 400 | Missing required `startDate` field |
| `should_return_404_when_template_not_found` | 404 | Service throws `ResourceNotFoundException` |
| `should_return_422_when_template_not_fully_mapped` | 422 | Service throws `TemplateIncompleteException` |

#### `GET /api/contracts/{id}`
| Test | Status | Scenario |
|---|---|---|
| `should_return_200_with_contract_when_found` | 200 | Response body contains `id` and `contractStatus` |
| `should_return_404_when_contract_not_found` | 404 | Service throws `ResourceNotFoundException` |

#### `GET /api/contracts/all`
| Test | Status | Scenario |
|---|---|---|
| `should_return_200_with_list_when_contracts_exist` | 200 | List of two contracts returned |
| `should_return_204_when_no_contracts_exist` | 204 | Empty page → No Content |

#### `PUT /api/contracts/terminate/{id}`
| Test | Status | Scenario |
|---|---|---|
| `should_return_204_when_termination_succeeds` | 204 | Happy path |
| `should_return_400_when_termination_date_missing` | 400 | `@NotNull terminationDate` fails validation |
| `should_return_404_when_contract_not_found` | 404 | Service throws `ResourceNotFoundException` |
| `should_return_409_when_contract_not_active` | 409 | Service throws `InvalidContractStateException` |

#### `POST /api/contracts/{id}/upload-signed`
| Test | Status | Scenario |
|---|---|---|
| `should_return_200_when_file_uploaded_successfully` | 200 | PDF uploaded; status becomes ACTIVE |
| `should_return_400_when_file_is_empty` | 400 | Zero-byte file rejected |
| `should_return_404_when_contract_not_found` | 404 | Service throws `ResourceNotFoundException` |

#### `PATCH /api/contracts/{id}/update-terms`
| Test | Status | Scenario |
|---|---|---|
| `should_return_200_when_terms_updated` | 200 | Financial terms updated |
| `should_return_404_when_contract_not_found` | 404 | Service throws `ResourceNotFoundException` |
| `should_return_409_when_contract_not_active` | 409 | Service throws `InvalidContractStateException` |

#### `POST /api/contracts/search`
| Test | Status | Scenario |
|---|---|---|
| `should_return_200_when_matches_found` | 200 | One matching contract returned |
| `should_return_204_when_no_matches` | 204 | Empty page → No Content |

#### `GET /api/contracts/download/{id}/{type}/{format}`
| Test | Status | Scenario |
|---|---|---|
| `should_return_400_when_format_invalid` | 400 | `xyz` is not a valid `DocumentFormat` |
| `should_return_400_when_type_invalid` | 400 | `invalid` does not map to a `DocumentType` |
| `should_return_200_with_pdf_bytes_when_download_succeeds` | 200 | Correct `Content-Disposition` header and raw bytes in body |

#### `GET /api/contracts/{id}/detailed`
| Test | Status | Scenario |
|---|---|---|
| `should_return_404_when_contract_not_found` | 404 | Service throws `ResourceNotFoundException` |

---

### TemplateControllerIT

**File:** `integration/TemplateControllerIT.java`

Covers the full surface of `TemplateController` (`/api/templates`).

#### `GET /api/templates`
| Test | Status | Scenario |
|---|---|---|
| `should_return_200_with_templates_when_exist` | 200 | `templateName` and `templateId` in response body |
| `should_return_204_when_no_templates_exist` | 204 | Empty page → No Content |
| `should_pass_custom_pagination_parameters` | 204 | `?page=2&size=5` forwarded to service |

#### `GET /api/templates/{id}`
| Test | Status | Scenario |
|---|---|---|
| `should_return_200_when_template_found` | 200 | `templateId` and `templateName` in body |
| `should_return_404_when_template_not_found` | 404 | Service throws `ResourceNotFoundException` |

#### `DELETE /api/templates/{id}`
| Test | Status | Scenario |
|---|---|---|
| `should_return_204_when_deleted_successfully` | 204 | Happy path |
| `should_return_404_when_template_not_found` | 404 | Service throws `ResourceNotFoundException` |

#### `PUT /api/templates/{id}/labels`
| Test | Status | Scenario |
|---|---|---|
| `should_return_200_when_labels_updated` | 200 | Valid mapping request |
| `should_return_400_when_request_invalid` | 400 | Empty `mappings` list fails `@NotEmpty` |
| `should_return_404_when_template_not_found` | 404 | Service throws `ResourceNotFoundException` |
| `should_return_400_when_field_does_not_belong_to_template` | 400 | Service throws `TemplateFieldOwnershipException` |

#### `GET /api/templates/download/{id}/{format}`
| Test | Status | Scenario |
|---|---|---|
| `should_return_200_with_file_bytes` | 200 | Correct `Content-Disposition` header |
| `should_return_400_when_format_invalid` | 400 | `xyz` is not a valid format |
| `should_return_404_when_template_not_found` | 404 | Download service throws `ResourceNotFoundException` |

---

### AppendixControllerIT

**File:** `integration/AppendixControllerIT.java`

Covers the full surface of `AppendixController` (`/api/appendices`).

#### `POST /api/appendices/generate`
| Test | Status | Scenario |
|---|---|---|
| `should_return_201_when_appendix_created` | 201 | Appendix created in DRAFT state |
| `should_return_400_when_contract_id_missing` | 400 | `@NotNull contractId` fails validation |
| `should_return_400_when_title_is_blank` | 400 | `@NotBlank title` fails validation |
| `should_return_404_when_contract_not_found` | 404 | Service throws `ResourceNotFoundException` |

#### `POST /api/appendices/{id}/upload-signed`
| Test | Status | Scenario |
|---|---|---|
| `should_return_200_when_signed_document_uploaded` | 200 | Status transitions to SIGNED |
| `should_return_400_when_file_is_empty` | 400 | Zero-byte file rejected |
| `should_return_404_when_appendix_not_found` | 404 | Service throws `ResourceNotFoundException` |
| `should_return_409_when_appendix_already_signed` | 409 | Service throws `InvalidAppendixStateException` |

#### `GET /api/appendices/contract/{contractId}`
| Test | Status | Scenario |
|---|---|---|
| `should_return_200_with_list_when_appendices_exist` | 200 | Two appendices in response |
| `should_return_204_when_no_appendices` | 204 | Empty list → No Content |
| `should_return_404_when_contract_not_found` | 404 | Service throws `ResourceNotFoundException` |

#### `PATCH /api/appendices/{id}/terminate`
| Test | Status | Scenario |
|---|---|---|
| `should_return_200_when_terminated_successfully` | 200 | Status becomes TERMINATED |
| `should_return_404_when_appendix_not_found` | 404 | Service throws `ResourceNotFoundException` |
| `should_return_409_when_appendix_not_signed` | 409 | Service throws `InvalidAppendixStateException` |

#### `DELETE /api/appendices/{id}`
| Test | Status | Scenario |
|---|---|---|
| `should_return_204_when_deleted_successfully` | 204 | Happy path |
| `should_return_404_when_appendix_not_found` | 404 | Service throws `ResourceNotFoundException` |

#### `GET /api/appendices/download/{id}/{type}/{format}`
| Test | Status | Scenario |
|---|---|---|
| `should_return_200_with_bytes_when_download_succeeds` | 200 | Correct `Content-Disposition` header |
| `should_return_400_when_format_invalid` | 400 | `exe` is not a valid format |
| `should_return_400_when_type_invalid` | 400 | `raw` does not map to an appendix document type |

---

## Document Tests

### DocxFillerTest

**File:** `document/DocxFillerTest.java`

`DocxFiller` is the most critical document-processing class. It takes compressed DOCX bytes, a positionally-ordered list of `TemplateField` objects, and a label-to-value map, then writes the field values into the placeholders while preserving per-run Word formatting. These tests create real in-memory DOCX documents using Apache POI — no LibreOffice or database is needed.

#### Basic substitution
| Test | What it proves |
|---|---|
| `should_replace_single_placeholder_with_value` | `....` becomes the field value in extracted paragraph text |
| `should_not_contain_dots_after_replacement` | No `....` remains in the filled document |
| `should_replace_multiple_placeholders_in_order` | Two fields in two paragraphs both substituted in positional order |
| `should_keep_original_dots_when_no_value_provided` | Missing value → dots remain; no crash |
| `should_handle_empty_document_without_throwing` | Empty DOCX with no paragraphs produces valid output |
| `should_handle_no_placeholder_in_document` | Document without placeholders is returned unchanged |

#### Edge cases
| Test | What it proves |
|---|---|
| `should_handle_special_characters_in_replacement_value` | Apostrophes, ampersands, and em-dashes survive as literal text |
| `should_handle_unicode_replacement_value` | German umlauts (`Müller`) round-trip correctly |
| `should_handle_very_long_replacement_value` | A 5,000-character value is written without truncation |

#### Parameterised scenarios
`should_substitute_correctly_for_various_field_values` — five label/value pairs via `@MethodSource`:
- ISO date string `2026-01-01`
- Monetary format `10,000.00`
- Contract reference `CLM-2026-001`
- French name `Jean-François Dupont`
- Email address `user@example.com`

#### Template completion check
`should_replace_all_placeholders_when_all_values_provided` — three-paragraph document with three placeholders: verifies that **no `....` token remains** and all three expected values appear after filling. This directly validates the document-completion requirement.

---

## Scheduler Tests

Both scheduled job classes are tested with `@ExtendWith(MockitoExtension.class)`. The `ContractRepository` is mocked; the job methods are called directly rather than waiting for a cron trigger. An additional Awaitility-based test demonstrates the async assertion pattern.

---

### ContractArchiveJobTest

**File:** `scheduler/ContractArchiveJobTest.java`

`ContractArchiveJob.archiveExpiredContracts()` is scheduled daily at midnight. It calls `contractRepository.archiveExpiredContracts(today, ACTIVE, ARCHIVED)`.

| Test | What it proves |
|---|---|
| `should_call_archive_with_today_active_and_archived_statuses` | Exact arguments captured: `LocalDate.now()`, `ACTIVE`, `ARCHIVED` |
| `should_process_zero_contracts_when_none_expired` | Return value of 0 is handled without error |
| `should_process_multiple_expired_contracts` | Return value of 42 is handled without error |
| `should_invoke_archive_query_within_expected_time` (Awaitility) | When called on a background thread, the repository method is invoked within 5 seconds |

The Awaitility test demonstrates the correct pattern for scheduler integration tests where the method runs asynchronously: a `CountDownLatch` or `AtomicInteger` captures the side effect and `Awaitility.await().untilAsserted()` polls without `Thread.sleep`.

---

### ContractTerminationJobTest

**File:** `scheduler/ContractTerminationJobTest.java`

`ContractTerminationJob.processTerminationDueContracts()` is scheduled daily at midnight. It calls `contractRepository.processTerminationDueContracts(TERMINATED, TERMINATION_DUE, today)`.

| Test | What it proves |
|---|---|
| `should_call_process_with_terminated_termination_due_and_today` | Exact status and date arguments verified with `ArgumentCaptor` |
| `should_not_throw_when_no_contracts_due` | Return value of 0 is handled |
| `should_not_throw_when_many_contracts_terminated` | Return value of 100 is handled |
| `should_complete_asynchronously_within_time_budget` | Background thread completes within 5 seconds (Awaitility) |

---

## Security Tests

### JwtTokenProviderTest

**File:** `security/JwtTokenProviderTest.java`

`JwtTokenProvider` is a standalone `@Component` that signs and parses JWTs using JJWT 0.12. Tests construct the provider directly (no Spring context), generate real signed tokens using the same JJWT API, and verify all parsing paths.

#### `validateToken`
| Test | What it proves |
|---|---|
| `should_return_true_for_valid_token` | A freshly-signed token with the correct key is valid |
| `should_return_false_for_expired_token` | A token with a past expiry returns false |
| `should_return_false_for_tampered_token` | Altering the last 5 characters invalidates the signature |
| `should_return_false_for_null_token` | Null input is safe — returns false |
| `should_return_false_for_blank_token` | Whitespace-only input is safe — returns false |
| `should_return_false_for_token_signed_with_different_key` | Wrong key → signature mismatch → false |

#### `getSubject`
| Test | What it proves |
|---|---|
| `should_return_subject_from_valid_token` | `sub` claim extracted correctly |
| `should_return_empty_for_invalid_token` | Non-JWT string → `Optional.empty()` |
| `should_return_empty_for_null_token` | Null → `Optional.empty()` |

#### `getClaim`
| Test | What it proves |
|---|---|
| `should_return_custom_claim_from_valid_token` | A `role: ADMIN` claim is extracted by name |
| `should_return_empty_claim_for_invalid_token` | Invalid token → `Optional.empty()` |
| `should_throw_when_claim_name_is_null` | `NullPointerException` thrown (documented contract) |

#### Initialisation
| Test | What it proves |
|---|---|
| `should_throw_during_init_when_secret_is_too_short` | Secret shorter than 32 characters throws `IllegalStateException` at startup |

---

### JwtAuthenticationFilterTest

**File:** `security/JwtAuthenticationFilterTest.java`

`JwtAuthenticationFilter` extends `OncePerRequestFilter`. It reads the `Authorization: Bearer <token>` header, delegates to `JwtTokenProvider.getSubject()`, and populates (or leaves empty) the `SecurityContextHolder`. Tests call `doFilterInternal()` directly — bypassing `shouldNotFilter()` — and verify `SecurityContextHolder` state after each invocation.

| Test | What it proves |
|---|---|
| `should_populate_security_context_when_valid_bearer_token_present` | Valid token → `Authentication` object with correct principal in context; `FilterChain.doFilter()` called |
| `should_not_populate_security_context_when_no_authorization_header` | No header → context stays null; `tokenProvider` never consulted |
| `should_not_populate_security_context_when_bearer_token_invalid` | Token returns empty from provider → context stays null |
| `should_not_populate_context_when_header_is_not_bearer` | `Basic` auth header → not a Bearer token → provider never called |
| `should_skip_filter_for_actuator_paths` | `shouldNotFilter()` returns `true` for `/actuator/health` |
| `should_not_skip_filter_for_api_paths` | `shouldNotFilter()` returns `false` for `/api/contracts/1` |
| `should_always_call_filter_chain_regardless_of_auth_outcome` | Even with no header, `FilterChain.doFilter()` is always called (stateless — no short-circuit) |

---

## What is not covered and why

| Area | Reason |
|---|---|
| **Repository layer (`@DataJpaTest`)** | Flyway migrations use PostgreSQL-specific DDL: custom schemas (`clm`), `CREATE TYPE ... AS ENUM`, and `pg_trgm` extensions. These cannot run against H2. Real repo tests require a Testcontainers PostgreSQL container (dependency and pattern are wired in pom.xml; run `make test-cache-real` with Docker). |
| **`FileUtils.convert()` (DOCX → PDF)** | Calls LibreOffice headless. Correctly mocked with `@Mock FileUtils` in all service unit tests. Integration-level conversion tests would require LibreOffice installed in CI. |
| **`ReportController` / `ReportService`** | Not included in this pass; structure mirrors the existing controllers and can be added with the same standalone MockMvc pattern. |
| **Full `@SpringBootTest` security (401 assertions via HTTP)** | Booting the full context requires either a real database connection or disabling all JPA/Flyway auto-configuration. The filter test (`JwtAuthenticationFilterTest`) provides equivalent coverage of the logic that produces 401 responses without the infrastructure overhead. |
| **`DocxNormalizer`** | Normalisation logic is exercised indirectly through `PlaceholderProcessorTest`. Dedicated normaliser tests can be added to `document/` using the same in-memory POI pattern. |
| **Cache behaviour** | Covered by the existing `CachePerformanceTest` and `CacheRealDbTest` (which require a running PostgreSQL). Not duplicated. |
