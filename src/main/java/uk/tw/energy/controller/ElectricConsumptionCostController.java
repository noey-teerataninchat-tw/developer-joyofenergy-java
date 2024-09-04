package uk.tw.energy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.service.AccountService;
import uk.tw.energy.service.MeterReadingService;
import uk.tw.energy.service.PricePlanService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/electric/cost")
public class ElectricConsumptionCostController {

    private final AccountService accountService;
    private final PricePlanService pricePlanService;
    private final MeterReadingService meterReadingService;

    public ElectricConsumptionCostController(AccountService accountService, PricePlanService pricePlanService, MeterReadingService meterReadingService) {
        this.accountService = accountService;
        this.pricePlanService = pricePlanService;
        this.meterReadingService = meterReadingService;
    }

    @GetMapping("/smartMeter/{smartMeterId}/lastWeek")
    public ResponseEntity<BigDecimal> getCostForLastSevenDays(@PathVariable String smartMeterId) {
        String pricePlanId = accountService.getPricePlanIdForSmartMeterId(smartMeterId);

        if (StringUtils.isEmpty(pricePlanId)) {
            return ResponseEntity.notFound().build();
        }

        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        Optional<List<ElectricityReading>> electricityReadings = meterReadingService.getReadings(smartMeterId);
        List<ElectricityReading> lastSevenDaysReadings = electricityReadings.get().stream()
                .filter(x -> x.time().isAfter(sevenDaysAgo))
                .collect(Collectors.toList());

        BigDecimal consumptionCost = pricePlanService.getConsumptionCostOfElectricityReadingsByPricePlan(pricePlanId, lastSevenDaysReadings);

        return ResponseEntity.ok(consumptionCost);
    }
}
