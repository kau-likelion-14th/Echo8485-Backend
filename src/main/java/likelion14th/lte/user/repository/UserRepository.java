ppackage likelion14th.lte.user.repository;

import likelion14th.lte.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// [추가문제 · 필수 X] 이 코드는 인터페이스일 뿐이고 구현체 클래스가 없습니다.
// 그런데 어떻게 프로그램 실행 시 DB와 통신하는 객체로 동작할 수 있나요?
// 답변: 구현 클래스를 직접 작성하지 않아도, Spring Data JPA가 프로그램 실행 시
//       UserRepository를 읽고 동적 프록시 기반 구현체를 메모리에 만들어
//       빈으로 등록함. 덕분에 save(), findById() 호출 시 실제 SQL이 실행됨.
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(Long id);
}