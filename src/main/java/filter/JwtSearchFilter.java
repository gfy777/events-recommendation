package filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import db.DBConnection;
import db.DBConnectionFactory;
import db.mysql.MySQLConnection;
import util.JwtUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(urlPatterns = {"/search",})
public class JwtSearchFilter implements Filter {


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        ObjectMapper mapper = new ObjectMapper();
        DBConnection connection = DBConnectionFactory.getConnection();

        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);

            try {
                JwtUtil jwtUtil = new JwtUtil();
                jwtUtil.validateToken(jwt);
                String username = jwtUtil.extractUsername(jwt);
                request.setAttribute("username", username);
                request.setAttribute("name", connection.getFullname(username));
                chain.doFilter(request, response);
            } catch (Exception ex) {
                chain.doFilter(request, response);
            } finally {
                try {
                    if (connection != null){
                        connection.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }

}
