
import likelion14th.lte.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface UserRepository extends JpaRepository<User,Long> {
// 1. 클래스가 아니라 '인터페이스'입니다.
// 2. JpaRepository<User, Long>을 상속받습니다. "User 테이블을 관리할 거고, 그 테이블의 PK 타입은 Long이야!"라는 뜻입니다.
// [추가문제] (필수 X) 이 코드는 인터페이스일 뿐이고 구현체(implements) 클래스가 없습니다.
// 그런데 어떻게 프로그램 실행 시 DB와 통신하는 객체로 동작할 수 있나요?
// 답변:상속을 받아 JpaRepoistory<>에서 DB를 연결시켜 갖고와 객체로 동작할 수 있음.
    Optional<User> findById(Long id);
    // 3. ID로 유저를 찾는 메서드입니다.
    // 4. Optional: "유저를 찾았는데 없을 수도 있어! (null 방지용 상자)"라는 자바 문법입니다.
    // * 사실 findById는 JpaRepository가 기본으로 제공해서 안 적어도 작동합니다.
}