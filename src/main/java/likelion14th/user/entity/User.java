package likelion14th.lte.user.domain;

import jakarta.persistence.*;
import likelion14th.lte.Entity.BaseEntity;
import lombok.*;

import java.util.ArrayList;


@Entity
@Getter
// [Q1. @NoArgsConstructor는 매개변수가 없는 기본 생성자를 만듭니다.
// 그런데 왜 누구나 쓸 수 있게 PUBLIC으로 열어두지 않고, 굳이 PROTECTED로 막아두었을까요? (객체 생성의 안전성과 JPA 관점)]
// 답변:public으로 올려두면,만약에 누군가 접근해서 객체를 생성한다고 한다면 객체의 무결성이 깨짐
// 2. 모든 필드의 Getter를 자동 생성합니다.(설명하기)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 3. 매개변수가 없는 기본 생성자를 만드는데, 접근 제어자를 'protected'로 막아둡니다. (JPA 규칙상 기본 생성자가 필요한데, 아무나 함부로 빈 객체를 만드는 건 막기 위함)
@Table(name = "users")
// 4. DB에 생성될 테이블 이름을 "User"가 아니라 "users"로 강제 지정합니다. (User는 DB 예약어라 충돌이 날 수 있음)
public class User extends BaseEntity{
// 5. 자바 상속! 이제 User는 id, name뿐만 아니라 createdAt, updatedAt도 가집니다.

    @Id
    // 6. 이 변수가 이 테이블의 기본키(Primary Key, PK)임을 선언합니다. (주민번호 같은 역할)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // 7. ID 값을 우리가 직접 넣지 않고(!중요), DB가 알아서 1, 2, 3... 순서대로 증가(Auto Increment)시키도록 맡깁니다.
    private Long id;
    // [Q2. @Column(nullable = false) 어노테이션이 DB와 자바 코드 사이에서 하는 역할은 무엇인가요?]
    // 답변:username은 DB에서 필수이기 때문에 없으면 안된다.
    @Column(nullable = false)
    // 8. "이 컬럼은 절대 비어있으면 안 돼(NOT NULL)!"
    private String username;

    @Column(length = 16, nullable = false, unique = true)
    // 9. "길이는 16자 제한, 비어있으면 안 되고, 남들과 중복되어서도 안 됨"
    private String userTag;

    @Column(columnDefinition = "TEXT")
    // 10. 문자열이 길어질 수 있으니 DB의 데이터 타입을 단순 VARCHAR가 아닌 'TEXT'로 지정합니다.
    private String introduction;

    @Column(columnDefinition = "TEXT")
    private String profileImage;

    @Column(columnDefinition = "TEXT")
    private String s3ImageKey;

    @Builder(access = AccessLevel.PUBLIC)
    // 11. 객체를 생성할 때 생성자 대신 쓸 수 있는 '빌더 패턴'을 만들어줍니다. (어떤 변수에 무슨 값을 넣는지 명확히 알 수 있어 실수를 줄여줍니다.)
    private User (String username, String introduction, String userTag){
        this.username = username;
        this.userTag = userTag;
        this.introduction = introduction;
    }
    // [Q3. @Setter를 위 @Getter 처럼 사용하면 모든 맴버들에 setIntruduction() 같은 setter 메서드가 생성됩니다. 하지만 왜 @Setter를 쓰지않고 updateIntroduction() 이라는 명확한 메서드를 만든 객체지향적인 이유는 무엇인가요?]
    // 답변:setter는 값만 바꾸어주기 때문에 외부에서 자유롭게 변경할 수 있음, 하지만 객체지향적인 관점에서 외부에서 바꾸는걸 매우 싫어함. 그래서 setter은 대부분 매서드에서 많이 사용
    // 12. [핵심] Setter를 쓰지 않고 명확한 행동(메서드)으로 객체의 상태를 바꿉니다.
    // 이 메서드를 호출해 값을 바꾸면, JPA가 알아서 DB에 UPDATE 쿼리를 날려줍니다! (더티 체킹)
    public void updateIntroduction(String introduction){
        this.introduction = introduction;
    }
}