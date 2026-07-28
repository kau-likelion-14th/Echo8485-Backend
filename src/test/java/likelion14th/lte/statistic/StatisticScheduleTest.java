package likelion14th.lte.statistic;

import likelion14th.lte.statistic.schedule.StatisticSchedule;
import likelion14th.lte.statistic.service.StatisticService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class StatisticScheduleTest {

    @Mock
    private StatisticService statisticService;

    @InjectMocks
    private StatisticSchedule statisticSchedule;

    @Test
    @DisplayName("스케줄러는 updateAllStatistics() 만 호출한다")
    void callsUpdateAllStatisticsOnly() {
        statisticSchedule.updateStatistics();

        verify(statisticService).updateAllStatistics();
        verifyNoMoreInteractions(statisticService);
    }

    @Test
    @DisplayName("cron 표현식이 매일 00시 10분으로 설정되어 있다")
    void cronRunsAtTenPastMidnight() throws Exception {
        Method method = StatisticSchedule.class.getMethod("updateStatistics");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        assertThat(scheduled).isNotNull();
        assertThat(scheduled.cron()).isEqualTo("0 10 0 * * *");
    }
}
