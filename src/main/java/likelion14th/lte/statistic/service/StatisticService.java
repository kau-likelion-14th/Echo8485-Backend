package likelion14th.lte.statistic.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.statistic.dto.response.StatisticResponse;
import likelion14th.lte.statistic.entity.StatWeek;
import likelion14th.lte.statistic.entity.Statistic;
import likelion14th.lte.todo.repository.TodoDateRepository;
import likelion14th.lte.user.domain.User;
import likelion14th.lte.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StatisticService {

    /** 배치 페이지 크기 **/
    private static final int BATCH_PAGE_SIZE = 500;

    /** 완료율 집계 기간 **/
    private static final int MONTH_PERCENT_RANGE_DAYS = 30;

    private final UserRepository userRepository;
    private final TodoDateRepository todoDateRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 통계 조회
     * User → Statistic → StatWeek 이 모두 LAZY 이므로,
     * DTO 변환까지 트랜잭션 안에서 끝내야 지연 로딩 예외가 발생하지 않는다.
     */
    @Transactional(readOnly = true)
    public StatisticResponse getStatistic(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        Statistic statistic = user.getStatistic();

        return StatisticResponse.from(statistic);
    }

    /** 한 명의 사용자 통계 갱신 (기준일: 어제) **/
    @Transactional
    public void updateStatistic(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        LocalDate day = LocalDate.now().minusDays(1);

        updateStatistic(user, day);
    }

    /** 전체 사용자 통계 배치 갱신 **/
    @Transactional
    public void updateAllStatistics() {
        int pageNumber = 0;
        Page<User> userPage;

        // 모든 사용자가 같은 기준일을 쓰도록 반복문 밖에서 한 번만 계산
        LocalDate day = LocalDate.now().minusDays(1);

        do {
            userPage = userRepository.findAll(PageRequest.of(pageNumber, BATCH_PAGE_SIZE));

            for (User user : userPage.getContent()) {
                updateStatistic(user, day);
            }

            // 페이지 단위로 쓰기 지연 SQL 을 내보내고 영속성 컨텍스트를 비워 메모리를 관리한다.
            // (flush 는 commit 이 아니며, 트랜잭션은 메서드 종료 시 한 번에 커밋된다)
            entityManager.flush();
            entityManager.clear();

            pageNumber++;
        } while (userPage.hasNext());
    }

    /**
     * 실제 통계 계산
     * 영속 상태의 엔티티를 도메인 메서드로 변경하므로 별도의 save 호출 없이 변경 감지로 반영된다.
     */
    private void updateStatistic(User user, LocalDate day) {
        Statistic statistic = user.getStatistic();

        // 어제 성공 여부: 완료 투두가 1개 이상이고 미완료 투두가 하나도 없어야 성공
        boolean hasCompletedTodo = todoDateRepository
                .existsByTodo_User_IdAndDateAndCompleted(user.getId(), day, true);
        boolean hasIncompleteTodo = todoDateRepository
                .existsByTodo_User_IdAndDateAndCompleted(user.getId(), day, false);

        boolean success = hasCompletedTodo && !hasIncompleteTodo;

        statistic.increaseStreakIfSuccess(success);

        // 성공한 날에만 해당 요일 count 증가
        if (success) {
            StatWeek statWeek = statistic.getStatWeeks().stream()
                    .filter(week -> week.getWeek().toDayOfWeek() == day.getDayOfWeek())
                    .findFirst()
                    // Statistic 생성 시 7개가 만들어지므로, 없으면 잘못된 도메인 상태다.
                    .orElseThrow(() -> new GeneralException(ErrorCode.INTERNAL_SERVER_ERROR));

            statWeek.increaseCount();
        }

        // 최근 완료율
        LocalDate start = day.minusDays(MONTH_PERCENT_RANGE_DAYS);
        LocalDate end = day;

        long completedCount = todoDateRepository
                .countByTodo_User_IdAndDateBetweenAndCompleted(user.getId(), start, end, true);
        long incompleteCount = todoDateRepository
                .countByTodo_User_IdAndDateBetweenAndCompleted(user.getId(), start, end, false);

        long totalCount = completedCount + incompleteCount;

        int monthPercent;
        if (totalCount == 0) {
            monthPercent = 0;
        } else {
            monthPercent = (int) ((completedCount * 100) / totalCount);
        }

        statistic.updateMonthPercent(monthPercent);
    }
}
