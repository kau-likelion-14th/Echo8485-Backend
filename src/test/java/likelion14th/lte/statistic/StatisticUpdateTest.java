package likelion14th.lte.statistic;

import likelion14th.lte.statistic.entity.StatWeek;
import likelion14th.lte.statistic.entity.Statistic;
import likelion14th.lte.statistic.service.StatisticService;
import likelion14th.lte.todo.repository.TodoDateRepository;
import likelion14th.lte.user.domain.User;
import likelion14th.lte.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * updateStatistic(userId) 의 streak / 요일별 count / monthPercent 갱신 규칙을 검증한다.
 * 기준일은 구현과 동일하게 "어제"다.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StatisticUpdateTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TodoDateRepository todoDateRepository;

    private StatisticService statisticService;

    private User user;
    private LocalDate yesterday;

    @BeforeEach
    void setUp() {
        statisticService = new StatisticService(userRepository, todoDateRepository);

        user = User.builder()
                .username("테스트유저")
                .userTag("test_tag")
                .introduction("소개글")
                .build();
        ReflectionTestUtils.setField(user, "id", USER_ID);

        yesterday = LocalDate.now().minusDays(1);

        given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
    }

    /** 어제의 완료/미완료 투두 존재 여부를 지정 **/
    private void givenYesterday(boolean hasCompleted, boolean hasIncomplete) {
        given(todoDateRepository.existsByTodo_User_IdAndDateAndCompleted(USER_ID, yesterday, true))
                .willReturn(hasCompleted);
        given(todoDateRepository.existsByTodo_User_IdAndDateAndCompleted(USER_ID, yesterday, false))
                .willReturn(hasIncomplete);
    }

    /** 최근 30일 완료/미완료 개수를 지정 **/
    private void givenRecentCounts(long completed, long incomplete) {
        LocalDate start = yesterday.minusDays(30);
        given(todoDateRepository.countByTodo_User_IdAndDateBetweenAndCompleted(USER_ID, start, yesterday, true))
                .willReturn(completed);
        given(todoDateRepository.countByTodo_User_IdAndDateBetweenAndCompleted(USER_ID, start, yesterday, false))
                .willReturn(incomplete);
    }

    private Statistic statistic() {
        return user.getStatistic();
    }

    /** 어제 요일에 해당하는 StatWeek **/
    private StatWeek yesterdayStatWeek() {
        return statistic().getStatWeeks().stream()
                .filter(week -> week.getWeek().toDayOfWeek() == yesterday.getDayOfWeek())
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("완료 투두만 있으면 성공으로 보고 streak 가 1 증가한다")
    void streakIncreasesOnSuccess() {
        givenYesterday(true, false);

        statisticService.updateStatistic(USER_ID);

        assertThat(statistic().getStreak()).isEqualTo(1);
    }

    @Test
    @DisplayName("완료와 미완료 투두가 함께 있으면 streak 가 0으로 초기화된다")
    void streakResetsWhenIncompleteExists() {
        ReflectionTestUtils.setField(statistic(), "streak", 5);
        givenYesterday(true, true);

        statisticService.updateStatistic(USER_ID);

        assertThat(statistic().getStreak()).isZero();
    }

    @Test
    @DisplayName("미완료 투두만 있으면 streak 가 0으로 초기화된다")
    void streakResetsWhenOnlyIncompleteExists() {
        ReflectionTestUtils.setField(statistic(), "streak", 3);
        givenYesterday(false, true);

        statisticService.updateStatistic(USER_ID);

        assertThat(statistic().getStreak()).isZero();
    }

    @Test
    @DisplayName("TodoDate 가 하나도 없으면 streak 가 0으로 초기화된다")
    void streakResetsWhenNoTodoDate() {
        ReflectionTestUtils.setField(statistic(), "streak", 7);
        givenYesterday(false, false);

        statisticService.updateStatistic(USER_ID);

        assertThat(statistic().getStreak()).isZero();
    }

    @Test
    @DisplayName("성공한 날에는 어제 요일의 StatWeek count 만 1 증가한다")
    void countIncreasesOnlyForYesterdayWeek() {
        givenYesterday(true, false);

        statisticService.updateStatistic(USER_ID);

        assertThat(yesterdayStatWeek().getCount()).isEqualTo(1);
        assertThat(statistic().getStatWeeks())
                .filteredOn(week -> week.getWeek().toDayOfWeek() != yesterday.getDayOfWeek())
                .allSatisfy(week -> assertThat(week.getCount()).isZero());
    }

    @Test
    @DisplayName("실패한 날에는 모든 StatWeek count 가 유지된다")
    void countStaysOnFailure() {
        ReflectionTestUtils.setField(yesterdayStatWeek(), "count", 4);
        givenYesterday(true, true);

        statisticService.updateStatistic(USER_ID);

        assertThat(yesterdayStatWeek().getCount()).isEqualTo(4);
        assertThat(statistic().getStatWeeks())
                .filteredOn(week -> week.getWeek().toDayOfWeek() != yesterday.getDayOfWeek())
                .allSatisfy(week -> assertThat(week.getCount()).isZero());
    }

    @Test
    @DisplayName("집계 기간에 투두가 없으면 monthPercent 는 0이다")
    void monthPercentIsZeroWhenNoTodo() {
        ReflectionTestUtils.setField(statistic(), "monthPercent", 50);
        givenYesterday(false, false);
        givenRecentCounts(0, 0);

        statisticService.updateStatistic(USER_ID);

        assertThat(statistic().getMonthPercent()).isZero();
    }

    @Test
    @DisplayName("완료 3개 미완료 1개이면 monthPercent 는 75이다")
    void monthPercentIs75() {
        givenYesterday(true, false);
        givenRecentCounts(3, 1);

        statisticService.updateStatistic(USER_ID);

        assertThat(statistic().getMonthPercent()).isEqualTo(75);
    }

    @Test
    @DisplayName("완료 1개 미완료 2개이면 정수 몫으로 monthPercent 는 33이다")
    void monthPercentIs33() {
        givenYesterday(false, true);
        givenRecentCounts(1, 2);

        statisticService.updateStatistic(USER_ID);

        assertThat(statistic().getMonthPercent()).isEqualTo(33);
    }
}
