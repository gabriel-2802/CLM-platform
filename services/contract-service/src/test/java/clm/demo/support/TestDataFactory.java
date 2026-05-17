package clm.demo.support;

import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.models.Contract;
import clm.demo.models.enums.ContractStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class TestDataFactory {

    private TestDataFactory() {}

    public static Contract contract(Long id, ContractStatus status) {
        Contract c = Contract.builder()
                .clientId(42)
                .contractStatus(status)
                .contractValue(BigDecimal.valueOf(10_000))
                .contractBalance(BigDecimal.valueOf(5_000))
                .contractStartDate(LocalDate.of(2026, 1, 1))
                .contractEndDate(LocalDate.of(2027, 1, 1))
                .build();
        setId(c, id);
        return c;
    }

    public static ContractResponseDTO contractResponse(Long id, String status) {
        return ContractResponseDTO.builder()
                .id(id)
                .clientId(42)
                .contractStatus(status)
                .contractStartDate(LocalDate.of(2026, 1, 1))
                .contractEndDate(LocalDate.of(2027, 1, 1))
                .build();
    }

    public static void setId(Object entity, Long id) {
        Class<?> clazz = entity.getClass();
        while (clazz != null) {
            try {
                var f = clazz.getDeclaredField("id");
                f.setAccessible(true);
                f.set(entity, id);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException("could not set id on " + entity.getClass().getSimpleName(), e);
            }
        }
    }
}
