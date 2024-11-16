package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.annotation.Rollback;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j

class MemberRepositoryV1Test {

    MemberRepositoryV1 repository;

    @BeforeEach
    void beforeEach(){
        // 1) 기본 DriverManager - 항상 새로운 커넥션을 획득
        //DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);


        // 2) 커넥션 풀링으로 변경

        //DI 측면에서의 장점을 얘기하자면
        //DriverManagerDataSource -> HikariDataSource 로 변경해도 MemberRepositoryV1의 코드는 전혀 변경하지 않아도 된다
        //MemberRepositoryV1은 DataSource 인터페이스에 의존하기 때문 -> DataSource 를 사용하는 장점!!
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        repository = new MemberRepositoryV1(dataSource);
    }

    @Test
    @Rollback
    @DisplayName("커넥션 풀 사용했을 때 커넥션이 재사용되면서 작업 이루어짐")
    void crud() throws SQLException, InterruptedException {

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
        assertThatThrownBy(() -> repository.findById(member.getMemberId()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("member not found: memberId = " + member.getMemberId()); //예외 메시지, 원인, 상태 등에 대한 추가 검증이 필요한 경우

        Thread.sleep(1000);

    }

}