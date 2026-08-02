package likelion14th.lte.statistic;

import jakarta.persistence.EntityManager;
import likelion14th.lte.statistic.service.StatisticService;
import likelion14th.lte.todo.repository.TodoDateRepository;
import likelion14th.lte.user.domain.User;
import likelion14th.lte.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/** updateAllStatistics() 의 페이징·flush/clear 동작을 검증한다. **/
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StatisticBatchTest {

    private static final int PAGE_SIZE = 500;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TodoDateRepository todoDateRepository;

    @Mock
    private EntityManager entityManager;

    private StatisticService statisticService;

    @BeforeEach
    void setUp() {
        statisticService = new StatisticService(userRepository, todoDateRepository);
        ReflectionTestUtils.setField(statisticService, "entityManager", entityManager);
    }

    private User user(long id) {
        User user = User.builder()
                .username("유저" + id)
                .userTag("tag" + id)
                .introduction("소개")
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    @DisplayName("사용자가 0명이어도 오류 없이 종료한다")
    void runsWithoutUsers() {
        given(userRepository.findAll(PageRequest.of(0, PAGE_SIZE)))
                .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, PAGE_SIZE), 0));

        statisticService.updateAllStatistics();

        verify(userRepository).findAll(PageRequest.of(0, PAGE_SIZE));
        verify(entityManager).flush();
        verify(entityManager).clear();
        verify(todoDateRepository, never())
                .existsByTodo_User_IdAndDateAndCompleted(anyLong(), any(LocalDate.class), anyBoolean());
    }

    @Test
    @DisplayName("사용자가 500명을 넘으면 페이지 크기 500으로 hasNext 가 false 가 될 때까지 순회한다")
    void iteratesEveryPage() {
        long totalUsers = 600L;
        given(userRepository.findAll(PageRequest.of(0, PAGE_SIZE)))
                .willReturn(new PageImpl<>(List.of(user(1L)), PageRequest.of(0, PAGE_SIZE), totalUsers));
        given(userRepository.findAll(PageRequest.of(1, PAGE_SIZE)))
                .willReturn(new PageImpl<>(List.of(user(2L)), PageRequest.of(1, PAGE_SIZE), totalUsers));

        statisticService.updateAllStatistics();

        verify(userRepository).findAll(PageRequest.of(0, PAGE_SIZE));
        verify(userRepository).findAll(PageRequest.of(1, PAGE_SIZE));
        verify(userRepository, times(2)).findAll(any(PageRequest.class));

        // 페이지마다 flush 직후 clear 가 호출된다
        InOrder inOrder = inOrder(entityManager);
        inOrder.verify(entityManager).flush();
        inOrder.verify(entityManager).clear();
        inOrder.verify(entityManager).flush();
        inOrder.verify(entityManager).clear();
        verify(entityManager, times(2)).flush();
        verify(entityManager, times(2)).clear();
    }

    @Test
    @DisplayName("배치는 User 를 ID 로 다시 조회하지 않고 모든 페이지에서 같은 기준일을 사용한다")
    void reusesLoadedUsersAndSingleBaseDate() {
        long totalUsers = 600L;
        given(userRepository.findAll(PageRequest.of(0, PAGE_SIZE)))
                .willReturn(new PageImpl<>(List.of(user(1L)), PageRequest.of(0, PAGE_SIZE), totalUsers));
        given(userRepository.findAll(PageRequest.of(1, PAGE_SIZE)))
                .willReturn(new PageImpl<>(List.of(user(2L)), PageRequest.of(1, PAGE_SIZE), totalUsers));

        statisticService.updateAllStatistics();

        LocalDate yesterday = LocalDate.now().minusDays(1);
        verify(userRepository, never()).findById(anyLong());
        verify(todoDateRepository)
                .existsByTodo_User_IdAndDateAndCompleted(eq(1L), eq(yesterday), eq(true));
        verify(todoDateRepository)
                .existsByTodo_User_IdAndDateAndCompleted(eq(2L), eq(yesterday), eq(true));
    }
}
