package Group2.capstone_project.filter;

import Group2.capstone_project.domain.Client;
import Group2.capstone_project.session.SessionConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.util.PatternMatchUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
public class loginCheckFilter implements Filter {


    private static final String[] blackList = {"/loginClient/*","/board/*"};
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try{
            //log.info("인증 체크 필터 시작{}",requestURI);

            if(isLoginCheckPath(requestURI)){
               // log.info("인증 체크 로직 실행{}",requestURI);
                HttpSession session = httpRequest.getSession(false);
                if(session ==null||session.getAttribute(SessionConst.LOGIN_CLIENT)==null){
                 //   log.info("미인증 사용자 요청{}",requestURI);
                    //로그인으로 redirect
                    httpResponse.sendRedirect("/redirectLogin?redirectURL="+requestURI);
                    return;
                }
            }else if(isAdminLoginCheckPath(requestURI)){
                HttpSession session = httpRequest.getSession(false);
                Client client = (Client) session.getAttribute(SessionConst.LOGIN_CLIENT);
                if(session ==null||session.getAttribute(SessionConst.LOGIN_CLIENT)==null){
                    //   log.info("미인증 사용자 요청{}",requestURI);
                    //로그인으로 redirect
                    httpResponse.sendRedirect("/redirectLogin?redirectURL="+requestURI);
                    return;
                }
                System.out.println(client.getAdminCheck());
                if(!"YES".equals(client.getAdminCheck())){
                    httpResponse.sendRedirect("redirect:/notAdmin");
                    return;
                }

            }
            chain.doFilter(request,response);
        }catch (Exception e){
            throw e;
        }finally {
           // log.info("인증 체크 필터 종료{}",requestURI);
        }
    }


    private boolean isLoginCheckPath(String requestURI){
        return PatternMatchUtils.simpleMatch(blackList,requestURI);
    }

    private boolean isAdminLoginCheckPath(String requestURI){
        return PatternMatchUtils.simpleMatch("/admin/*",requestURI);
    }
}
