package clm.demo.services;


import clm.demo.dto.requests.GenContractRequest;
import clm.demo.dto.responses.ContractResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractService {
    public ContractResponseDTO generateContract(GenContractRequest request) {
        return null;
    }
}
