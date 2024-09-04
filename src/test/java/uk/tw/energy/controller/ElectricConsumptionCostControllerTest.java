package uk.tw.energy.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.PricePlan;
import uk.tw.energy.service.AccountService;
import uk.tw.energy.service.MeterReadingService;
import uk.tw.energy.service.PricePlanService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ElectricConsumptionCostControllerTest {

    private static final String SMART_METER_ID = "smart-meter-id";
    private static final String PRICE_PLAN_ID = "test-supplier";
    private ElectricConsumptionCostController controller;
    private AccountService accountService;
    private MeterReadingService meterReadingService;

    @BeforeEach
    public void setUp() {
        PricePlan pricePlan = new PricePlan(PRICE_PLAN_ID, null, BigDecimal.ONE, null);
        meterReadingService = new MeterReadingService(new HashMap<>());


        ElectricityReading electricityReading = new ElectricityReading(Instant.now().minusSeconds(3600), BigDecimal.valueOf(15.0));
        ElectricityReading otherReading = new ElectricityReading(Instant.now(), BigDecimal.valueOf(5.0));
        ElectricityReading eightDayBeforeReading = new ElectricityReading(Instant.now().minus(8, ChronoUnit.DAYS), BigDecimal.valueOf(15.0));
        meterReadingService.storeReadings(SMART_METER_ID, Arrays.asList(electricityReading, otherReading ,eightDayBeforeReading));


        List<PricePlan> pricePlans = Arrays.asList(pricePlan);

        PricePlanService pricePlansService = new PricePlanService(pricePlans, meterReadingService);


        Map<String, String> meterToTariffs = new HashMap<>();
        meterToTariffs.put(SMART_METER_ID, PRICE_PLAN_ID);
        accountService = new AccountService(meterToTariffs);

        controller = new ElectricConsumptionCostController(accountService, pricePlansService, meterReadingService);

    }

    @Test
    public void givenPricePlanAttachedMeterIdShouldReturned7DaysUsage()
    {
        assertThat(controller.getCostForLastSevenDays(SMART_METER_ID).getBody()).isEqualTo(BigDecimal.valueOf(10.0));
    }

    @Test
    public void givenNoMatchingMeterIdShouldReturnNotFound()
    {
        assertThat(controller.getCostForLastSevenDays("not-found").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
