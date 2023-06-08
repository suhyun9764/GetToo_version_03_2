package Group2.capstone_project.controller;

import Group2.capstone_project.domain.Client;
import Group2.capstone_project.domain.Club;
import Group2.capstone_project.dto.client.ClientDto;
import Group2.capstone_project.service.clientService;
import Group2.capstone_project.session.SessionConst;
import Group2.capstone_project.session.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class clientController {


    private final PasswordEncoder passwordEncoder;
    private final clientService clientserivce;
    private final SessionManager sessionManager = new SessionManager();

    private final ResourceLoader resourceLoader;
    @Value("${upload.directory}")
    private String uploadDirectory;


    @Autowired
    public clientController(clientService clientService,PasswordEncoder passwordEncoder,ResourceLoader resourceLoader){
        this.clientserivce = clientService;
        this.passwordEncoder =passwordEncoder;
        this.resourceLoader = resourceLoader;
    }

    @GetMapping("/")
    public String Home(HttpServletRequest request, Model model){
        HttpSession session = request.getSession(false);
        System.out.println("call");
        if(session == null){
            return "index.html";
        }
        Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
        if(client==null)
            return "index.html";
        if("YES".equals(client.getAdminCheck())){
            return "redirect:/admin/list";
        }

        return "redirect:/loginClient";
    }

    @GetMapping("/loginClient")
    public String loginClient(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if(session == null){
            return "login.html";
        }
        if(session!=null){
            Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
            if(client!=null){
                model.addAttribute("name", client.getName());
                return "loginClient/login_index.html";
            }
        }
        return "login.html";
    }




    @GetMapping("/index.html")
    public String Home2(HttpServletRequest request, Model model){
        HttpSession session = request.getSession(false);

        if(session == null){
            return "/index.html";
        }
        Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
        if(client==null)
            return "/index.html";
        model.addAttribute("errorMessage", "권한이 없습니다");
        System.out.println("모델:"+client.getName());
        model.addAttribute("name", client.getName());
        return "/loginClient/login_index.html";
    }


    @GetMapping("/notAdmin")
    public String notAdmin(Model model){
        model.addAttribute("errorMessage", "권한이 없습니다");
        return "/loginClient/login_index.html";
    }
    @GetMapping("/gotoMembership")
    public String membership(){
        return "membership.html";
    }


    @GetMapping("/gotoJoin")
    public String joinForm(){
        return "join.html";
    }

    @GetMapping("/gotoLogin")
    public String loginHome(HttpServletRequest request, Model model){
        HttpSession session = request.getSession(false);
        if(session != null){
            Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
            if(client != null){
                session.setAttribute(SessionConst.LOGIN_CLIENT,client);
                model.addAttribute("name", client.getName());
                return "redirect:/loginClient/redirectLoginIndex";
            }
        }
        return "login.html";

    }




    @GetMapping("/client/goLogin_index")
    public String main(Model model,HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
        model.addAttribute("errorMessage", "권한이 없습니다");
        model.addAttribute("name", client.getName());

        return "loginClient/login_index.html";
    }



    @PostMapping("/clientlogin")
    public String loginV3(@ModelAttribute ClientDto clientDto,@RequestParam(defaultValue = "/")String redirectURL,
                          HttpServletRequest request,Model model){

        Client client = new Client();
        client.setId(clientDto.getId());
        client.setPwd(clientDto.getPassword());
        Optional<Client> result = clientserivce.login(client);
        if(result!=null) {
            if("NO".equals(result.get().getJoinCheck())){
                model.addAttribute("errorMessage", "가입이 승인되지 않았습니다");
                return "login.html";
            }
            HttpSession session = request.getSession();
            session.setAttribute(SessionConst.LOGIN_CLIENT, result.get() );
            return "redirect:"+redirectURL;
        } else{
            model.addAttribute("errorMessage", "일치하는 계정정보가 없습니다");
            return "login.html";

        }
    }




    @PostMapping("/client/join")
    public String create(ClientDto ClientDto){

        Client client = new Client();
        client.setId(ClientDto.getId());
        client.setName(ClientDto.getName());
        client.setAge(ClientDto.getAge());
        client.setEmail(ClientDto.getEmail());
        client.setStudentNumber(ClientDto.getStudentNumber());
        client.setPwd(passwordEncoder.encode(ClientDto.getPassword()));
        client.setSchool(ClientDto.getSchool());
        client.setDepartment(ClientDto.getDepartment());
        client.setClub(ClientDto.getClub());
        client.setLeader(ClientDto.getLeader());
        MultipartFile imageFile = ClientDto.getImageFile();
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // 이미지 파일을 저장할 경로를 설정합니다.
                String fileName = imageFile.getOriginalFilename();
                String storeFileName = createStoreFileName(fileName);
                Path filePath = Path.of(uploadDirectory, storeFileName);

                // 파일을 지정된 경로로 복사합니다.
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println(fileName);
                // 이미지 파일 경로를 클라이언트 객체에 설정합니다.
                client.setImagePath(filePath.toString());
            } catch (IOException e) {
                e.printStackTrace();
                // 에러 처리 방식에 따라 예외 처리 코드를 작성하세요.
            }
        }
        client.setQuestion(ClientDto.getQuestion());
        client.setAnswer(ClientDto.getAnswer());
        clientserivce.join(client);

        return "redirect:/afterjoin";
    }
    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }
    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

    @GetMapping("/afterjoin")
    public String afterjoin(Model model){
        model.addAttribute("errorMessage", "회원가입이 완료되었습니다. 가입승인까지 2~3일 정도 소요됩니다");
        return "login.html";
    }

 // 아이디 찾기 관련
    @PostMapping("/client/findID")
    public String findID(Model model, @ModelAttribute ClientDto ClientDto){
        System.out.println("come");
        Client client = new Client();
        client.setName(ClientDto.getName());
        client.setStudentNumber(ClientDto.getStudentNumber());
        client.setEmail(ClientDto.getEmail());
        String result = clientserivce.findId(client.getName(), client.getStudentNumber(), client.getEmail());
        if(result=="false"){
            return "redirect:/redirectIdSearch";
        }
        model.addAttribute("result",result);
        System.out.println("result :"+result);
        return "loginclient/checkyourId";
    }

    @GetMapping("/gotoIdSearch")
    public String gotoIdSearch(){
        return "idSearch.html";
    }

    @GetMapping("/gotoPwdSearch")
    public String gotoPwdSearch(){
        return "passwordsearch.html";
    }



    @GetMapping("/redirectIdSearch")
    public String redirectIdSearch(Model model){
        model.addAttribute("errorMessage", "일치하는 계정정보가 없습니다");
        return "idSearch.html";
    }

// 비밀번호 찾기 관련
    @GetMapping("/client/findPwd")
    public String findPwd(Model model, ClientDto ClientDto){
        Client client = new Client();
        client.setName(ClientDto.getName());
        client.setId(ClientDto.getId());
        client.setStudentNumber(ClientDto.getStudentNumber());
        client.setQuestion(ClientDto.getQuestion());
        client.setAnswer(ClientDto.getAnswer());
        Optional<Client> result = clientserivce.findPwd(client.getName(), client.getId(), client.getStudentNumber(),client.getQuestion(),client.getAnswer());
        if(result.isPresent()){
            model.addAttribute("id",result.get().getId());
            System.out.println(result.get().getName());
            return "changeyourpwd.html";
        }else{
            return "redirect:/returnPwdSearch";
        }

    }
    @GetMapping("/returnPwdSearch")
    public String returnPwdSearch(Model model){
        model.addAttribute("errorMessage","일치하는 계정이 없습니다");
        return "passwordsearch.html";
    }

    @PostMapping("/changePwd")
    public String changePwd(@RequestParam("id") String id, @RequestParam("newPassword") String newPassword){
        String newEncodePwd = passwordEncoder.encode(newPassword);
        clientserivce.changePwd(id,newEncodePwd);
        return "login.html";
    }
    @GetMapping("/client/update")
        public String updateForm(HttpSession session,Model model){
        String id = (String)session.getAttribute("loginId");
        Client client = clientserivce.updateForm(id);
        model.addAttribute("updateClient",client);
        return "client/clientinfoupdate";
    }
    @GetMapping("/admin/list")
    public String adminPage(Model model) {
        List<Client> clients = clientserivce.findAll();
        model.addAttribute("clients", clients); // 클라이언트 리스트를 모델에 추가

        return "admin/adminPage"; // 클라이언트 리스트를 표시할 뷰의 이름 반환
    }


    @PostMapping("/client/update")
    public String updateClinet(Model model,@ModelAttribute ClientDto clientDto){
        Client client = new Client();
        client.setId(clientDto.getId());
        client.setName(clientDto.getName());
        client.setAge(clientDto.getAge());
        client.setStudentNumber(clientDto.getStudentNumber());
        clientserivce.updateInfo(client);
        model.addAttribute("client",client);
        return "/client/updateresult";
    }



     @PostMapping ("/clientlogout")
    public String logOut(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        if(session!=null) {
            session.invalidate();
        }
        return "redirect:/gotoLogin";
    }

    @GetMapping("/loginClient/clientInfo")
    public String clientInfo(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        System.out.println("SUCCESS");
        Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
        model.addAttribute("client", client);
//        return "clientInfo.html";
        return "loginClient/login_profile.html";
    }

    @PostMapping("/loginClient/clientDelete")
    public String clientDelete(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
        System.out.println("delteCOn"+client.getName());
        clientserivce.clientDelete(client);
        return "redirect:/afterDelete";
    }

    @GetMapping("/afterDelete")
    public String afterDelete(Model model){
        model.addAttribute("errorMessage", "회원탈퇴가 정상적으로 완료되었습니다. 감사합니다");
        return "login.html";
    }

    @GetMapping("/loginClient/DeleteInfoPage")
    public String DeleteInfo(HttpServletRequest request,Model model){
        HttpSession session = request.getSession(false);
        System.out.println("SUCCESS");
        Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
        model.addAttribute("client", client);
        return "loginClient/deleteinfo.html";
    }

    @GetMapping("/redirectLogin")
    public String redirectLogin(HttpServletRequest request, Model model) {
        String redirectURL = request.getParameter("redirectURL");
        model.addAttribute("redirectURL", redirectURL);
        model.addAttribute("errorMessage", "로그인 후 이용해주세요");
        return "login.html";
    }

    @GetMapping("loginClient/writeBoard")
    public String onlyLeader(HttpServletRequest request, Model model, RedirectAttributes attributes) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_CLIENT) == null) {
            model.addAttribute("errorMessage", "권한이 없습니다");
            return "redirect:/loginClient/redirectLoginIndex";
        }

        Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
        if (client == null || client.getId() == null) {
            model.addAttribute("errorMessage", "권한이 없습니다");
            return "redirect:/loginClient/redirectLoginIndex";
        }

            Client chkClient = clientserivce.findById(client.getId());
            if (chkClient == null || !"YES".equals(chkClient.getLeader())) {
                return "redirect:/loginClient?error=auth";
        }

        return "loginClient/createclubBoard_1.html";
    }

    @PostMapping("/loginClient/loginPwdChange")
    public String loginPwdChange(@RequestParam("password") String password,HttpServletRequest request){
        HttpSession session = request.getSession();
        Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
        System.out.println("인증"+client.getName());
        boolean chk = clientserivce.checkPwd(client, password);
        if(chk == true){
            return "/loginClient/loginPwdChange";
        }
        else{
            return "redirect:/loginClient/changePwdPage2";
        }
    }

    @PostMapping("/clientlogin/loginChangePassword")
    public String loginChangePassword(Model model,HttpServletRequest request,@RequestParam("newPassword") String password, @RequestParam("confirmPassword") String confirmPassword){
        if(!confirmPassword.equals(password)){
            model.addAttribute("errorMessage", "비밀번호가 일치하지 않습니다.");
            return "loginClient/loginpwdchange.html";

        }

        HttpSession session = request.getSession(false);
        Client client = (Client)session.getAttribute(SessionConst.LOGIN_CLIENT);
        String newEncodePwd = passwordEncoder.encode(password);
        clientserivce.changePwd(client.getId(),newEncodePwd);
            session.invalidate();

        return "redirect:/afterChange";
    }

    @GetMapping("/afterChange")
    public String afterChange(Model model){
        model.addAttribute("errorMessage", "비밀번호가 변경되었습니다. 다시 로그인해주세요");
        return "login.html";
    }

    @GetMapping("/loginClient/loginPwdChange")
    public String loginPwdChange(){
        return "loginClient/loginpwdchange";
    }
    @PostMapping("/client/check-id")
    public ResponseEntity<String> checkId(@RequestParam("id") String id) {
        System.out.println(id);
        boolean isAvailable = clientserivce.checkIdAvailability(id);
        if (isAvailable) {
            return ResponseEntity.ok("available");
        } else {
            return ResponseEntity.ok("not-available");
        }
    }

    @PostMapping("/admin/auth")
    public String authClient(@RequestParam("clientId") String clientId) {
        clientserivce.joinAuth(clientId);
        return "redirect:/admin/list";  // 작업 완료 후 리다이렉션할 페이지를 반환
    }

    @GetMapping("/admin/checkimg")
    public ResponseEntity<Resource> showStudentCardImage(@RequestParam("clientId") String clientId) throws IOException {
        Client client = clientserivce.findById(clientId);
        String imagePath = client.getImagePath() ;
        System.out.println(client.getImagePath());
        Resource imageResource = resourceLoader.getResource("file:" + imagePath);

        if (imageResource.exists()) {
            System.out.println(client.getImagePath()+"ok!!");
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(imageResource);
        } else {
            System.out.println(client.getImagePath()+"NO!!");
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/loginClient/ShowMypage")
    public String gotoMypage(){
        return "loginClient/login_profile.html";
    }
    @GetMapping("/loginClient/changePwdPage")
    public String gotoChangePwd(){
        return "loginClient/loginpwdcheck.html";
    }
    @GetMapping("/loginClient/changePwdPage2")
    public String gotoChangePwd2(Model model)
    {
        model.addAttribute("errorMessage", "비밀번호가 틀렸습니다");
        return "loginClient/loginpwdcheck.html";
    }

    @GetMapping("/loginClient/loginGroup")
    public String loginGroup(Model model,HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        Client client = (Client)session.getAttribute(SessionConst.LOGIN_CLIENT);
        List<Club> clubs = clientserivce.getClubByClient(client.getId());
        Optional<Club> result = clubs.stream().findAny();
        if(result.isPresent()){
            System.out.println("in");
            model.addAttribute("club",result.get().getClubName());
            model.addAttribute("name",client.getName());
        }else{
            model.addAttribute("club","가입된 동아리가 없습니다");
            model.addAttribute("name",client.getName());
        }
        model.addAttribute("errorMessage", "비밀번호가 틀렸습니다");
        return "loginClient/login_group.html";
    }


}
