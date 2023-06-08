package Group2.capstone_project.repository;

import Group2.capstone_project.domain.Client;
import Group2.capstone_project.domain.Club;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class MysqlClientRepository implements ClientRepository{

    private final JdbcTemplate jdbcTemplate;

    public MysqlClientRepository(DataSource dataSource){
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
    @Override
    public void save(Client client) {

        String sql = "INSERT INTO client(id,name,age,studentNumber,email,school,department, pwd,imagepath,question, answer,club,Leader) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql,client.getId(),client.getName(),client.getAge(),client.getStudentNumber(),
                client.getEmail(),client.getSchool(),client.getDepartment(),client.getPwd(),client.getImagePath(),client.getQuestion(),client.getAnswer(),client.getClub(),client.getLeader());
    }

    @Override
    public Optional<Client> findId(String name, String studentNumber, String email) {
        System.out.println(name+studentNumber+email);
        String[] object = {name,studentNumber,email};
        String sql = "SELECT * FROM client where name = ? and studentNumber = ? and email = ?";
        List<Client> result = jdbcTemplate.query(sql,clientRowMapper(),object);
        if(result.isEmpty()){
            System.out.println("repoEmpty");
            return Optional.empty();
        }
        return result.stream().findAny();

    }

    @Override
    public Optional<Client> findPwd(String name, String id, String studentNumber, String question,String answer) {
        String[] object = {name,id,studentNumber, question, answer};
        String sql = "SELECT * FROM client where name = ? and id = ? and studentNumber = ? and question =? and answer=?";
        List<Client> result = jdbcTemplate.query(sql,clientRowMapper(),object);
        if(result.isEmpty()){
            System.out.println("repoEmpty");
            return Optional.empty();
        }else {
            System.out.println("repoNotEmpty");
            return result.stream().findAny();
        }
    }
    @Override
    public List<Client> findAll() {
        String sql = "SELECT * FROM client where joincheck ='NO'";
        return jdbcTemplate.query(sql,clientRowMapper());
    }


    @Override
    public Optional<Client> findById(String id) {
        String sql = "SELECT *FROM client WHERE id =?";
        List<Client> result = jdbcTemplate.query(sql, clientRowMapper(), id);
        return result.stream().findAny();
    }

    private RowMapper<Client> clientRowMapper(){
        return (rs, rowNum) -> {

            Client client = new Client();
            client.setId(rs.getString("id"));
            client.setName(rs.getString("name"));
            client.setStudentNumber(rs.getString("studentNumber"));
            client.setAge(rs.getString("age"));
            client.setPwd(rs.getString("pwd"));
            client.setEmail(rs.getString("email"));
            client.setSchool(rs.getString("school"));
            client.setDepartment(rs.getString("department"));
            client.setLeader(rs.getString("Leader"));
            client.setJoinCheck(rs.getString("joincheck"));
            client.setAdminCheck(rs.getString("admincheck"));
            client.setImagePath(rs.getString("imagepath"));
            client.setClub("club");
            return client;
        };
    }
    @Override
    public Optional<Client> login (Client client){
        String sql = "SELECT * FROM client where id = ?";
        List<Client> result = jdbcTemplate.query(sql,clientRowMapper(),client.getId());
        return result.stream().findAny();
    }



    @Override
    public void authJoin(String id) {
        String sql = "UPDATE client SET joincheck ='YES' WHERE id=?";
        jdbcTemplate.update(sql,id);
    }

    @Override
    public void clientDelete(String id, String studentNumber) {
        String sql = "DELETE FROM client WHERE id = ? AND studentNumber = ?";
       jdbcTemplate.update(sql,id,studentNumber);
    }

    @Override
    public void updateInfo(Client client) {
        String sql = "UPDATE client SET name= ? , studentNumber =? , age=? WHERE id =?  ";
        String[] object = {client.getName(),client.getStudentNumber(),client.getAge(),client.getId()};
        jdbcTemplate.update(sql,client.getName(),client.getStudentNumber(),client.getAge(),client.getId());
    }

    @Override
    public List<Club> getClubByClient(String id) {
        String sql = "SELECT c.* FROM membership m JOIN club c ON m.clubName = c.clubName WHERE m.studentName = ? AND m.joinAuth =?";
        String[] ob = {id,"OK"};
        List<Club> clubs = jdbcTemplate.query(sql,clubRowMapper(),ob);
        return clubs;
    }

    private RowMapper<Club> clubRowMapper(){
        return (rs, rowNum) -> {
            Club club = new Club();
            club.setClubName(rs.getString("clubName"));
            club.setLeader(rs.getString("leaderName"));
            return club;
        };
    }
    @Override
    public void updatePwd(String id, String newEncodePwd) {
        String sql = "UPDATE client SET pwd= ? WHERE id =?  ";
        System.out.println("mysql : "+id+":"+newEncodePwd);
        jdbcTemplate.update(sql,newEncodePwd,id);
    }


}
