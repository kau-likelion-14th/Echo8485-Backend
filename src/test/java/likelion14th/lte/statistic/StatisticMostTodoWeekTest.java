package likelion14th.lte.statistic;

import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.statistic.entity.StatWeek;
import likelion14th.lte.statistic.entity.Statistic;
import likelion14th.lte.todo.entity.WeekEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Statistic.getMostTodoWeek() 의 최댓값 선택과 동률 처리 규칙을 검증한다.
 * count 는 갱신 로직이 없는 필드이므로 테스트에서만 리플렉션으로 값을 채운다.
 */
class StatisticMostTodoWeekTest {

    /** 요일별 count 를 주입한 Statistic 생성 **/
    private Statistic statisticWithCounts(Map<WeekEnum, Integer> counts) {
        Statistic statistic = Statistic.create();
        for (StatWeek statWeek : statistic.getStatWeeks()) {
            ReflectionTestUtils.setField(statWeek, "count", counts.getOrDefault(statWeek.getWeek(), 0));
        }
        return statistic;
    }

    @Test
    @DisplayName("최다 완료 요일이 하나면 해당 요일을 반환한다")
    void returnsWeekWithHighestCount() {
        Statistic statistic = statisticWithCounts(Map.of(
                WeekEnum.MON, 3,
                WeekEnum.FRI, 9,
                WeekEnum.SUN, 5
        ));

        assertThat(statistic.getMostTodoWeek()).isEqualTo(WeekEnum.FRI);
    }

    @Test
    @DisplayName("최대 count 가 동률이면 WeekEnum 선언 순서가 앞선 요일을 반환한다")
    void returnsEarlierWeekOnTie() {
        Statistic statistic = statisticWithCounts(Map.of(
                WeekEnum.MON, 7,
                WeekEnum.TUE, 7
        ));

        assertThat(statistic.getMostTodoWeek()).isEqualTo(WeekEnum.MON);
    }

    @Test
    @DisplayName("뒤쪽 요일끼리 동률이어도 선언 순서가 앞선 요일을 반환한다")
    void returnsEarlierWeekOnTieAmongLaterWeeks() {
        Statistic statistic = statisticWithCounts(Map.of(
                WeekEnum.SAT, 4,
                WeekEnum.SUN, 4,
                WeekEnum.WED, 2
        ));

        assertThat(statistic.getMostTodoWeek()).isEqualTo(WeekEnum.SAT);
    }

    @Test
    @DisplayName("모든 count 가 0인 초기 상태에서는 MON 을 반환한다")
    void returnsMondayWhenAllCountsAreZero() {
        Statistic statistic = Statistic.create();

        assertThat(statistic.getMostTodoWeek()).isEqualTo(WeekEnum.MON);
    }

    @Test
    @DisplayName("statWeeks 가 비어 있는 비정상 상태에서는 null 대신 예외가 발생한다")
    void throwsWhenStatWeeksAreEmpty() {
        Statistic statistic = Statistic.create();
        ReflectionTestUtils.setField(statistic, "statWeeks", new ArrayList<StatWeek>());

        assertThatThrownBy(statistic::getMostTodoWeek)
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getCode())
                .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
