package clm.client.demo.dtos.response;

import clm.client.demo.models.ClientHistory;
import clm.client.demo.models.enums.YesNoNa;

import java.math.BigDecimal;

public record HistoryResponse(
    Long id,
    Long clientId,
    Integer anul,
    BigDecimal cifraAfaceri,
    Boolean inventar,
    YesNoNa bilantSemIun,
    YesNoNa bilantAnual
) {
    public static HistoryResponse from(ClientHistory history) {
        return new HistoryResponse(
                history.getId(),
                history.getClient().getId(),
                history.getYear(),
                history.getTurnover(),
                history.isInventory(),
                history.getJuneSemesterBalance(),
                history.getAnnualBalance()
        );
    }
}
