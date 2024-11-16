package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/*
* JDBC - DataSource 사용, JdbcUtils 사용
* */
@Slf4j
public class MemberRepositoryV1 {

    private final DataSource dataSource;

    public MemberRepositoryV1(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    //파라미터 바인딩으로 해야 sql injection을 예방할 수 있다
    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection con = null;
        PreparedStatement pstmt=  null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());

            //생성된 쿼리를 db에 전달
            pstmt.executeUpdate();

            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            //리소스는 예외가 발생하든 하지 않은 항상 수행되어야 하므로 곡 finally 구문에서 작성!!
            //리소스를 닫지 않는 것을 리소스 누수라고 하는데,
            //결과적으로 커넥션 부족으로 인한 장애가 발생할 수 있다
            close(con, pstmt, null);
        }
    }

    public Member findById(String memberId) throws SQLException {
        String sql = "select * from Member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery(); //rs에 조회 결과를 담아서 반환

            //rs는 내부에 있는 cursor를 이용해서 다음 데이터를 조회한다
            //최초엔 cursor가 데이터를 가리키고 있지 않기 때문에, rs.next()를 호출해서 커서가 다음으로 이동하게 함으로써
            //조회한 데이터가 존재하는지 확인한다 -> 따라서 최초 한번 rs.next()를 반드시 호출해야 한다!!!
            //이 경우 단건 조회이기 때문에 while문이 아니라 if문으로 커서를 한번 옮긴다
            if(rs.next()){ //true -> 결과 데이터가 있다는 뜻
                Member member = new Member();
                member.setMemberId(rs.getString("member_id")); //현재 cursor가 가리키고 있는 member_id 데이터를 String 타입으로 변환
                member.setMoney(rs.getInt("money"));
                return member;
            } else { //false -> 결과 데이터가 없다는 뜻
                throw new NoSuchElementException("member not found: memberId = " + memberId);
            }

        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    public void update(String memberId, int money) throws SQLException {
        String sql = "update Member set money = ? where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);

            int resultSize = pstmt.executeUpdate(); //쿼리를 수행하고 영향받은 row 수 반환
            log.info("resultSize = {}",resultSize);
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public void delete(String memberId) throws SQLException {
        String sql = "delete from Member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error",e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }



    //JdbcUtils은 커넥션을 편리하게 닫을 수 있는 기능 제공
    private void close(Connection con, Statement stmt, ResultSet rs){
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeConnection(con);
    }

    private Connection getConnection() throws SQLException {
        Connection con = dataSource.getConnection();
        log.info("get connection = {}, class = {}", con, con.getClass());
        return con;
    }


}
