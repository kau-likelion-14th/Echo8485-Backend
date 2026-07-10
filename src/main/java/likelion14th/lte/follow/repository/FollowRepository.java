package likelion14th.lte.follow.repository;

import likelion14th.lte.follow.entity.Follow;
import likelion14th.lte.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFromUserAndToUser(User fromUser, User toUser);

    Optional<Follow> findByFromUserAndToUser(User fromUser, User toUser);
    List<Follow> findByToUser(User toUser);
    List<Follow> findByFromUser(User fromUser);
}