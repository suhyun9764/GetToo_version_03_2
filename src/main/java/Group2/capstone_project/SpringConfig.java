package Group2.capstone_project;

import Group2.capstone_project.repository.BoardRepository;
import Group2.capstone_project.repository.ClientRepository;
import Group2.capstone_project.repository.MysqlBoardRepository;
import Group2.capstone_project.repository.MysqlClientRepository;
import Group2.capstone_project.service.boardService;
import Group2.capstone_project.service.clientService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class SpringConfig {

    private DataSource dataSource;

    public SpringConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public clientService clientService(){
        return new clientService(clientRepository());
    }
    

    @Bean
    public ClientRepository clientRepository(){
        return new MysqlClientRepository(dataSource);
    }

    @Bean
    public boardService boardService(){
        return new boardService(boardRepository());
    }

    @Bean
    public BoardRepository boardRepository(){
        return new MysqlBoardRepository(dataSource);
    }
}
