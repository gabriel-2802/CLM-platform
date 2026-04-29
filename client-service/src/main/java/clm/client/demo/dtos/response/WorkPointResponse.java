package clm.client.demo.dtos.response;

import clm.client.demo.models.WorkPoint;
import clm.client.demo.models.enums.Administration;

import java.time.LocalDateTime;

public record WorkPointResponse(
    Long id,
    Long clientId,
    String denumire,
    LocalDateTime deLa,
    LocalDateTime panaLa,
    Administration administratie,
    Boolean registruUC,
    Integer salariati,
    String cui,
    Boolean casaDeMarcat
) {
    public static WorkPointResponse from(WorkPoint workPoint) {
        return new WorkPointResponse(
                workPoint.getId(),
                workPoint.getClient().getId(),
                workPoint.getName(),
                workPoint.getValidFrom(),
                workPoint.getValidTo(),
                workPoint.getAdministration(),
                workPoint.isUcRegistry(),
                workPoint.getEmployeeCount(),
                workPoint.getTaxId(),
                workPoint.isCashRegister()
        );
    }
}
