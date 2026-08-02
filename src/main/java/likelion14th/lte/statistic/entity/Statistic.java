package likelion14th.lte.statistic.entity;

import jakarta.persistence.*;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.entity.BaseEntity;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.todo.entity.WeekEnum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "statistic")
public class Statistic extends BaseEntity {

    /** 완료율 허용 범위 **/
    private static final int MIN_MONTH_PERCENT = 0;
    private static final int MAX_MONTH_PERCENT = 100;

    /** 필드 **/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statistic_id")
    private Long id;

    /** 연속 수행 일수 (초기값 0) **/
    @Column(nullable = false)
    private int streak;

    /** 이번 달 달성률 (초기값 0) **/
    @Column(name = "month_percent", nullable = false)
    private int monthPercent;

    /** Statistic 삭제 시 해당 Statistic 의 StatWeek 도 함께 정리된다 **/
    @OneToMany(mappedBy = "statistic", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StatWeek> statWeeks = new ArrayList<>();

    /**
     * Statistic 생성
     * streak / monthPercent 를 0 으로 초기화하고,
     * WeekEnum 값 개수만큼 StatWeek 를 함께 생성해 연결한다.
     */
    public static Statistic create() {
        Statistic statistic = new Statistic();
        statistic.streak = 0;
        statistic.monthPercent = 0;
        statistic.statWeeks = new ArrayList<>();
        statistic.initializeWeeks();
        return statistic;
    }

    /** 월~일 StatWeek 7개 생성 **/
    private void initializeWeeks() {
        for (WeekEnum week : WeekEnum.values()) {
            addStatWeek(StatWeek.create(week));
        }
    }

    /** 연관관계 편의 메서드 : 양방향 참조를 한 번에 맞춰 준다 **/
    private void addStatWeek(StatWeek statWeek) {
        this.statWeeks.add(statWeek);
        statWeek.assignStatistic(this);
    }

    /**
     * 최다 완료 요일 비교 기준
     * 1순위 : count 가 큰 요일
     * 2순위 : count 가 같으면 WeekEnum 선언 순서(ordinal)가 앞선 요일
     *         max() 는 가장 큰 값을 고르므로, ordinal 은 뒤집어(reversed) 작은 값이 크게 평가되도록 한다.
     */
    private static final Comparator<StatWeek> MOST_TODO_WEEK_ORDER =
            Comparator.comparingInt(StatWeek::getCount)
                    .thenComparing(
                            Comparator.comparingInt((StatWeek statWeek) -> statWeek.getWeek().ordinal()).reversed()
                    );

    /**
     * 연속 성공일 갱신
     * 성공한 날이면 1 증가, 실패한 날이면 0으로 초기화한다.
     */
    public void increaseStreakIfSuccess(boolean success) {
        if (success) {
            this.streak++;
            return;
        }

        this.streak = 0;
    }

    /** 최근 완료율 갱신 (0~100 범위를 벗어난 값은 저장하지 않는다) **/
    public void updateMonthPercent(int monthPercent) {
        if (monthPercent < MIN_MONTH_PERCENT || monthPercent > MAX_MONTH_PERCENT) {
            throw new GeneralException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        this.monthPercent = monthPercent;
    }

    /** 가장 Todo 를 많이 완료한 요일 (계산 책임은 Statistic 이 가진다) **/
    public WeekEnum getMostTodoWeek() {
        return statWeeks.stream()
                .max(MOST_TODO_WEEK_ORDER)
                .map(StatWeek::getWeek)
                // 요일별 통계가 비어 있는 것은 정상 상태가 아니므로 null 대신 예외로 알린다.
                .orElseThrow(() -> new GeneralException(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
