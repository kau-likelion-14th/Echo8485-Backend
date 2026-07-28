package likelion14th.lte.statistic;

import likelion14th.lte.statistic.entity.StatWeek;
import likelion14th.lte.statistic.entity.Statistic;
import likelion14th.lte.todo.entity.WeekEnum;
import likelion14th.lte.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User 생성 시 Statistic 1개와 StatWeek 7개가 항상 함께 만들어지는지 검증한다.
 * DB 없이 객체 그래프만 확인하므로 Spring 컨텍스트를 띄우지 않는다.
 */
class StatisticCreationTest {

    private User createUser() {
        return User.builder()
                .username("테스트유저")
                .userTag("test_tag")
                .introduction("소개글")
                .build();
    }

    @Test
    @DisplayName("User 생성 직후 Statistic 이 함께 생성된다")
    void userHasStatisticOnCreation() {
        User user = createUser();

        assertThat(user.getStatistic()).isNotNull();
    }

    @Test
    @DisplayName("Statistic 의 streak 와 monthPercent 초기값은 0이다")
    void statisticInitialValuesAreZero() {
        Statistic statistic = createUser().getStatistic();

        assertThat(statistic.getStreak()).isZero();
        assertThat(statistic.getMonthPercent()).isZero();
    }

    @Test
    @DisplayName("StatWeek 은 WeekEnum 값 개수만큼 생성되고 모든 요일이 정확히 한 번씩 포함된다")
    void statWeeksCoverEveryWeekEnumExactlyOnce() {
        List<StatWeek> statWeeks = createUser().getStatistic().getStatWeeks();

        assertThat(statWeeks).hasSize(WeekEnum.values().length);
        assertThat(statWeeks).hasSize(7);
        assertThat(statWeeks)
                .extracting(StatWeek::getWeek)
                .containsExactlyInAnyOrder(WeekEnum.values());
    }

    @Test
    @DisplayName("모든 StatWeek 의 count 초기값은 0이다")
    void statWeekCountsAreZero() {
        List<StatWeek> statWeeks = createUser().getStatistic().getStatWeeks();

        assertThat(statWeeks).allSatisfy(statWeek -> assertThat(statWeek.getCount()).isZero());
    }

    @Test
    @DisplayName("모든 StatWeek 이 동일한 부모 Statistic 을 참조한다 (양방향 연관관계)")
    void statWeeksReferenceSameParentStatistic() {
        Statistic statistic = createUser().getStatistic();

        assertThat(statistic.getStatWeeks())
                .allSatisfy(statWeek -> assertThat(statWeek.getStatistic()).isSameAs(statistic));
    }

    @Test
    @DisplayName("User 마다 서로 다른 Statistic 이 생성된다")
    void eachUserHasItsOwnStatistic() {
        User first = createUser();
        User second = createUser();

        assertThat(first.getStatistic()).isNotSameAs(second.getStatistic());
    }
}
