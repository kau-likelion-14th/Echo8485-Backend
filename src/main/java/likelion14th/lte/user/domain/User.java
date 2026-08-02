package likelion14th.lte.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import likelion14th.lte.global.entity.BaseEntity;
import likelion14th.lte.follow.entity.Follow;
import likelion14th.lte.statistic.entity.Statistic;

import java.util.ArrayList;
import java.util.List;
@Entity
@Getter
// [Q1. @NoArgsConstructor는 매개변수가 없는 기본 생성자를 만듭니다.
// 그런데 왜 누구나 쓸 수 있게 PUBLIC으로 열어두지 않고, 굳이 PROTECTED로 막아두었을까요?]
// 답변: protected로 두면 우리가 new User()로 값 없는 객체를 마음대로 만들지 못함.
//       반면 JPA는 DB에서 데이터를 꺼낼 때 리플렉션으로 기본 생성자를 써야 하므로,
//       생성자 자체는 없앨 수 없음. public이면 username 등이 null인 불완전한 객체가
//       비즈니스 코드 어디서든 만들어질 수 있어 위험함.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // [Q2. @Column(nullable = false) 어노테이션이 DB와 자바 코드 사이에서 하는 역할은 무엇인가요?]
    // 답변: username에 null을 넣어 저장하는 실수를 DB 단계에서 차단함.
    //       테이블 생성 시 NOT NULL 제약이 걸리므로, 애플리케이션 검증을 넘어
    //       물리 DB에서도 빈 값 삽입이 불가능해져 데이터 무결성이 유지됨.
    @Column(nullable = false)
    private String username;

    @Column(length = 16, nullable = false, unique = true)
    private String userTag;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column(columnDefinition = "TEXT")
    private String profileImage;

    @Column(columnDefinition = "TEXT")
    private String s3ImageKey;
    @OneToMany(mappedBy = "toUser",fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followers;

    @OneToMany(mappedBy = "fromUser", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followings;

    /**
     * User 와 Statistic 은 1:1 관계이며, ERD 상 USER 테이블이 statistic_id 외래 키를 가지므로
     * User 가 연관관계의 주인이다.
     * cascade = ALL 로 userRepository.save(user) 한 번에 Statistic 과 StatWeek 까지 함께 저장된다.
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false, orphanRemoval = true)
    @JoinColumn(name = "statistic_id", nullable = false, unique = true)
    private Statistic statistic;


    @Builder(access = AccessLevel.PUBLIC)
    private User(String username, String introduction, String userTag) {
        this.username = username;
        this.userTag = userTag;
        this.introduction = introduction;
        this.followers = new ArrayList<>();
        this.followings = new ArrayList<>();
        // User 가 생성되는 유일한 통로이므로, 여기서 기본 통계 데이터를 함께 만든다.
        this.statistic = Statistic.create();
    }

    // [Q3. @Setter를 위 @Getter 처럼 사용하면 모든 맴버들에 setIntruduction() 같은 setter 메서드가 생성됩니다. 하지만 왜 @Setter를 쓰지않고 updateIntroduction() 이라는 명확한 메서드를 만든 객체지향적인 이유는 무엇인가요?]
    // 답변: @Setter는 id, userTag까지 한꺼번에 외부에서 바꿀 수 있는 통로를 열어 버림.
    //       updateIntroduction()은 소개글 변경이라는 행위만 허용하므로, 어떤 값이 왜 바뀌었는지
    //       파악하기 쉽고 불필요한 필드 수정도 막을 수 있음 → 캡슐화에 맞는 방식임.
    public void updateIntroduction(String introduction) {
        this.introduction = introduction;
    }
}