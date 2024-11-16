package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Rollback;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j

class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    @Rollback
    void crud() throws SQLException {

        //save
        Member member = new Member("memberVo", 10000);
        Member save = repository.save(member);


        //findById
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember = {}", findMember);
        log.info("member == findMember {}", member == findMember); // ==비교는 주소 비교이기 때문에 false
        log.info("member is equals to findMember {}", member.equals(findMember)); //equals는 모든 필드 값이 같으면 true
        assertThat(findMember).isEqualTo(member);


        //update - money: 10000 -> 20000
        repository.update(member.getMemberId(), 20000);
        Member updatedMember = repository.findById(member.getMemberId());
        assertThat(updatedMember.getMoney()).isEqualTo(20000);


        //delete
        repository.delete(member.getMemberId());
        assertThrows(NoSuchElementException.class, () -> {
            repository.findById(member.getMemberId());
        }); //단순히 예외 타입만 검증하고 싶은 경우 사용

        assertThatThrownBy(() -> repository.findById(member.getMemberId()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("member not found: memberId = " + member.getMemberId()); //예외 메시지, 원인, 상태 등에 대한 추가 검증이 필요한 경우
    }

}