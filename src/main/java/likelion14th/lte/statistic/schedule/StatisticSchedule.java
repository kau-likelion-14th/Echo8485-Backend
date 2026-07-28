package likelion14th.lte.statistic.schedule;

import likelion14th.lte.statistic.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatisticSchedule {

    private final StatisticService statisticService;

    /** 매일 00시 10분에 전날 기준 통계를 갱신 */
    @Scheduled(cron = "0 10 0 * * *")
    public void updateStatistics() {
        statisticService.updateAllStatistics();
    }
}
