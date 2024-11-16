package hello.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
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

    @Test
    void dataSourceConnectionPool() throws SQLException, InterruptedException {
        //커넥션 풀링- hikari
        HikariDataSource dataSource = new HikariDataSource(); //DataSource 인터페이스 구현
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        //커넥션 생성 작업은 애플리케이션 실행 속도에 영향을 주지 않기 위해 별도의 스레드에서 수행된다
        //MyPool connection adder라는 이름의 별도 스레드가 수행함
        dataSource.setMaximumPoolSize(10);
        dataSource.setPoolName("MyPool");

        //userDataSource(dataSource); //아직 Pool에 커넥션이 생성되지 않았는데 가져가려고 시도하면 커넥션이 들어올 때까지 약간 기다리게 된다
        useDataSourceOverMaxPoolSize(dataSource);
        Thread.sleep(1000);
    }

    private void useDataSourceOverMaxPoolSize(DataSource dataSource) throws SQLException{
        //커넥션 풀 max가 10인데 11개의 커넥션을 얻으려고 시도하면 계속 풀 안에서 커넥션을 얻으려고 시도하게 된다
        //이 경우 풀이 확보될 때까지 블록된다 -> MyPool - Connection not added, stats (total=10, active=10, idle=0, waiting=1)
        //다른 커넥션이 반환되지 않으면 타임아웃이 터지면서 커넥션이 끊긴다
        //이 때 예외와 함께 커넥션을 얻으려고 시도한 시간이 같이 기재되는데, 이 타임아웃 시간을 활용해서
        //커넥션 풀이 가득찼을 때 내가 얼마정도 기다릴 것인가, 얼마정도의 시간에서 예외를 터트릴 것인가 등의 설정을 할 수 있다
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        Connection con3 = dataSource.getConnection();
        Connection con4 = dataSource.getConnection();
        Connection con5 = dataSource.getConnection();
        Connection con6 = dataSource.getConnection();
        Connection con7 = dataSource.getConnection();
        Connection con8 = dataSource.getConnection();
        Connection con9 = dataSource.getConnection();
        Connection con10 = dataSource.getConnection();
        Connection con11 = dataSource.getConnection();
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
