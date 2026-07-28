package likelion14th.lte.statistic.entity;

import jakarta.persistence.*;
import likelion14th.lte.global.entity.BaseEntity;
import likelion14th.lte.todo.entity.WeekEnum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "stat_week")
public class StatWeek extends BaseEntity {

    /** 필드 **/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private WeekEnum week;

    /** 해당 요일의 수행 횟수 (초기값 0) **/
    @Column(nullable = false)
    private int count;

    /** 연관관계의 주인 : statistic_id 외래 키를 관리한다 **/
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "statistic_id", nullable = false)
    private Statistic statistic;

    /** StatWeek 는 Statistic 을 통해서만 생성된다 (패키지 외부에서 단독 생성 불가) **/
    static StatWeek create(WeekEnum week) {
        StatWeek statWeek = new StatWeek();
        statWeek.week = week;
        statWeek.count = 0;
        return statWeek;
    }

    /** 연관관계 편의 메서드에서만 호출된다 **/
    void assignStatistic(Statistic statistic) {
        this.statistic = statistic;
    }

    /** 해당 요일을 성공한 날에 1 증가시킨다 **/
    public void increaseCount() {
        this.count++;
    }
}
