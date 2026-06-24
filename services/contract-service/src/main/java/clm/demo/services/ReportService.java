package clm.demo.services;

import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.mappers.GeneratedContractMapper;
import clm.demo.models.Contract;
import clm.demo.models.enums.ContractStatus;
import clm.demo.repositories.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ContractRepository contractRepository;
    private final GeneratedContractMapper contractMapper;

    @Transactional(readOnly = true)
    public List<ContractResponseDTO> getExpiringContracts(int days) {
        LocalDate today    = LocalDate.now();
        LocalDate deadline = today.plusDays(days);

        log.info("Fetching ACTIVE contracts expiring between {} and {}", today, deadline);

        List<Contract> contracts = contractRepository.findExpiringContracts(
                ContractStatus.ACTIVE, today, deadline);

        return contracts.stream().map(c -> contractMapper.toResponseDTO(c, ContractService.getCurentlyActiveContractDetails(c.getContractDetailsList()))).toList();
    }

    @Transactional(readOnly = true)
    public List<ContractResponseDTO> getInactiveClientContracts(int months) {
        // generatedAt and createdAt are LocalDateTime columns; Hibernate 6 requires
        // an exact type match — coerce to start-of-day before passing to the query.
        LocalDateTime cutoffDate = LocalDate.now().minusMonths(months).atStartOfDay();

        log.info("Fetching ACTIVE contracts with no renegotiation since {} ({} months ago)", cutoffDate, months);

        List<Contract> contracts = contractRepository.findInactiveClientContracts(
                ContractStatus.ACTIVE, cutoffDate);

        return contracts.stream().map(c -> contractMapper.toResponseDTO(c, ContractService.getCurentlyActiveContractDetails(c.getContractDetailsList()))).toList();
    }
}
