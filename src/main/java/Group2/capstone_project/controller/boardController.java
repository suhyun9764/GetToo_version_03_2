package Group2.capstone_project.controller;
import Group2.capstone_project.domain.Board;
import Group2.capstone_project.domain.Client;
import Group2.capstone_project.dto.client.BoardDto;
import Group2.capstone_project.session.SessionConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import Group2.capstone_project.service.boardService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.http.HttpRequest;
import java.util.List;

@Controller
public class boardController {

    private final boardService boardService;

    @Autowired
    public boardController(boardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping("/board/create")
    public String saveBoard(BoardDto boardDto, HttpServletRequest request){

        HttpSession session = request.getSession(false);
        Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
        Board board = new Board();
        board.setContent(boardDto.getContent());
        board.setWriter(client.getName());
        board.setTitle(boardDto.getTitle());
        board.setModdate(boardDto.getModdate());
        board.setRegdate(boardDto.getRegdate());
        boardService.saveBoard(board);
        System.out.println(board.getId());
        System.out.println(board.getContent());
        return "redirect:/";
    }
    @GetMapping("/board/showlist")
    public String list(Model model) {
        List<Board> boards = boardService.getBoardList();
        model.addAttribute("boards", boards);
        return "loginClient/boardlist";
    }
}
