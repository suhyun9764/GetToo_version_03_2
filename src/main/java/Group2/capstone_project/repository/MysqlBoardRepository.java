package Group2.capstone_project.repository;

import Group2.capstone_project.domain.Board;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MysqlBoardRepository implements BoardRepository{
    private final JdbcTemplate jdbcTemplate;
    private final String SELECT_ALL = "SELECT id, title, content,writer FROM board";
    public MysqlBoardRepository(DataSource dataSource){
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
    @Override
    public void saveBoard(Board board) {
        String sql = "INSERT INTO board(title, content, writer) VALUES (?, ?, ?)";
        Object[] params = new Object[] {board.getTitle(), board.getContent(), board.getWriter()};
        jdbcTemplate.update(sql, params);
    }

    @Override
    public List<Board> findAll() {

        List<Board> boards = jdbcTemplate.query(SELECT_ALL, new RowMapper<Board>() {
            public Board mapRow(ResultSet rs, int rowNum) throws SQLException {
                Board board = new Board();
                board.setId(rs.getString("id"));
                board.setTitle(rs.getString("title"));
                board.setContent(rs.getString("content"));
                board.setWriter(rs.getString("writer"));
                return board;
            }
        });
        return boards;
    }

}
