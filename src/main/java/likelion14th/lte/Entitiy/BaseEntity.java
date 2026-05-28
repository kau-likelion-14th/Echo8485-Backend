package likelion14th.lte.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
// 1. "나는 진짜 테이블이 아니라, 내 자식 클래스에게 아래의 변수(컬럼)들을 물려주기만 할 거야"라는 뜻입니다.
@EntityListeners(AuditingEntityListener.class)
// 2. "이 클래스에 시간이 생기거나 변경될 때마다, 스프링이 알아서 시간을 감시하고(Listening) 기록해 줘!"
@Getter
// 3. 자바의 Getter 메서드(getCreatedAt 등)를 안 써도 롬복(Lombok)이 알아서 만들어줍니다.
public abstract class BaseEntity {
// 4. 단독으로 쓰일 수 없도록 추상 클래스(abstract)로 선언했습니다.

    @CreationTimestamp
    // 5. 이 데이터가 DB에 처음 'INSERT(저장)'될 때의 시간을 자동으로 찍어줍니다.
    @Column(updatable = false)
    // 6. "이 컬럼은 처음 저장될 때만 기록되고, 나중에 절대 수정(update)되면 안 돼!"라는 방어막입니다.
    private LocalDateTime createdAt;

    @UpdateTimestamp
    // 7. 데이터가 'UPDATE(수정)'될 때마다 현재 시간으로 계속 갱신해 줍니다.
    @Column(insertable = false)
    // 8. "처음 삽입(insert)될 때는 비워두고, 수정될 때만 작동해!"라는 뜻입니다.
    private LocalDateTime updatedAt;
}