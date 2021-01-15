package nextstep.subway.member.domain;

import lombok.Getter;
import nextstep.subway.BaseEntity;
import nextstep.subway.auth.application.AuthorizationException;
import nextstep.subway.favorite.domain.Favorite;
import nextstep.subway.favorite.exception.NotFoundFavoriteException;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;
    private Integer age;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "member")
    private final List<Favorite> favorites = new ArrayList<>();

    protected Member() {
    }

    public Member(String email, String password, Integer age) {
        this.email = email;
        this.password = password;
        this.age = age;
    }

    public void update(Member member) {
        this.email = member.email;
        this.password = member.password;
        this.age = member.age;
    }

    public void checkPassword(String password) {
        if (!StringUtils.equals(this.password, password)) {
            throw new AuthorizationException();
        }
    }

    public void deleteFavorite(Long favoriteId) {
        Favorite favorite = findFavorite(favoriteId);
        favorites.remove(favorite);
    }

    private Favorite findFavorite(Long favoriteId) {
        return favorites.stream()
                .filter(favorite -> favorite.isEqualId(favoriteId))
                .findFirst()
                .orElseThrow(() -> new NotFoundFavoriteException("해당 Favorite이 없습니다."));
    }
}
