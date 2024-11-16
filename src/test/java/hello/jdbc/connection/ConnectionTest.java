package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {

    @Test
    @DisplayName("DriverManager를 통해 매번 커넥션을 가져오는 경우 확인")
    void driverManager() throws SQLException {
        //커넥션을 획득할 때마다 매번 URL, USERNAME, PASSWORD를 넘겨야 함
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        log.info("connection = {}, class = {}", con1, con1.getClass());
        log.info("connection = {}, class = {}", con2, con2.getClass());
    }

    @Test
    @DisplayName("DataSource 인터페이스를 통해 커넥션을 가져오는 경우 확인")
    void dataSourceDriverManager() throws SQLException {
        //DriverManagerDataSource - 항상 새로운 커넥션을 획득한다
        DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        userDataSource(dataSource);
    }

    private void userDataSource(DataSource dataSource) throws SQLException {
        //DataSource 사용하는 방식은 처음 객체를 생성할 때만 필요한 파라미터를 넘기고,
        //커넥션을 획득할 때는 단순히 getConnection()만 호출하면 된다
        //이는 설정과 사용의 분리가 일어난것!! 사용 시 더 유연성이 증가됨
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        log.info("connection = {}, class = {}", con1, con1.getClass());
        log.info("connection = {}, class = {}", con2, con2.getClass());
    }
}
