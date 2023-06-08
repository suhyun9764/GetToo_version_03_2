package Group2.capstone_project.repository;

import Group2.capstone_project.domain.Board;

import java.util.List;

public interface BoardRepository {

    void saveBoard(Board board);

    List<Board> findAll();

}
