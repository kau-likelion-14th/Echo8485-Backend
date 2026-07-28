package likelion14th.lte.statistic;

import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.statistic.dto.response.StatisticResponse;
import likelion14th.lte.statistic.service.StatisticService;
import likelion14th.lte.todo.entity.WeekEnum;
import likelion14th.lte.todo.repository.TodoDateRepository;
import likelion14th.lte.user.domain.User;
import likelion14th.lte.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StatisticServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TodoDateRepository todoDateRepository;

    @InjectMocks
    private StatisticService statisticService;

    @Test
    @DisplayName("존재하는 User 의 통계를 조회하면 초기값이 담긴 StatisticResponse 를 반환한다")
    void getStatisticReturnsResponse() {
        User user = User.builder()
                .username("테스트유저")
                .userTag("test_tag")
                .introduction("소개글")
                .build();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        StatisticResponse response = statisticService.getStatistic(1L);

        assertThat(response.getStreak()).isZero();
        assertThat(response.getMonthPercent()).isZero();
        assertThat(response.getMostTodoWeek()).isEqualTo(WeekEnum.MON);
    }

    @Test
    @DisplayName("존재하지 않는 userId 이면 USER_4041 예외가 발생한다")
    void getStatisticThrowsWhenUserNotFound() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> statisticService.getStatistic(999L))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }
}
