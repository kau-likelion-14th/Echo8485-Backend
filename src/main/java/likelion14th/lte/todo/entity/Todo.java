package likelion14th.lte.todo.entity;

import jakarta.persistence.*;
import likelion14th.lte.category.entity.Category;
import likelion14th.lte.global.entity.BaseEntity;
import likelion14th.lte.user.domain.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "todo")
public class Todo extends BaseEntity {

    /** 필드 **/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String description;

    /** 루틴 여부 : 프론트에서 루틴 팝업의 [저장]을 누르면 true **/
    @Column(nullable = false)
    private boolean routineEnabled;

    /** 루틴 Todo 일 때만 사용하는 값 **/
    @Column
    private LocalDate startDate;
    @Column
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private WeekEnum week;

    /** Todo 삭제 시 해당 Todo 의 TodoDate 도 함께 정리된다 **/
    @OneToMany(mappedBy = "todo", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TodoDate> todoDates = new ArrayList<>();

    @Builder(access = AccessLevel.PUBLIC)
    private Todo(User user, Category category, String description,
                 boolean routineEnabled, LocalDate startDate, LocalDate endDate, WeekEnum week) {
        this.user = user;
        this.category = category;
        this.description = description;
        this.routineEnabled = routineEnabled;
        this.startDate = startDate;
        this.endDate = endDate;
        this.week = week;
    }

    /** Todo 생성 **/
    public static Todo create(User user, String description, Category category,
                              boolean routineEnabled, LocalDate startDate, LocalDate endDate, WeekEnum week) {
        return Todo.builder()
                .user(user)
                .description(description)
                .category(category)
                .routineEnabled(routineEnabled)
                .startDate(startDate)
                .endDate(endDate)
                .week(week)
                .build();
    }

    /** Todo 수정 **/
    public void update(String description, Category category,
                       boolean routineEnabled, LocalDate startDate, LocalDate endDate, WeekEnum week) {
        this.category = category;
        this.description = description;
        this.routineEnabled = routineEnabled;
        this.startDate = startDate;
        this.endDate = endDate;
        this.week = week;
    }
}
