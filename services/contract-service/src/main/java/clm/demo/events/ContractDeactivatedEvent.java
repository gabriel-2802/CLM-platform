package clm.demo.events;

import java.util.List;

public record ContractDeactivatedEvent(List<Long> contractIds) {}
