package filter;


import com.fasterxml.jackson.databind.ObjectMapper;
import db.DBConnection;
import db.mysql.MySQLConnection;
import util.JwtUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@WebFilter(urlPatterns = {"/history", "/recommendation"})
public class JwtRequestFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        ObjectMapper mapper = new ObjectMapper();
        DBConnection connection = new MySQLConnection();

        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);

            try {
                JwtUtil jwtUtil = new JwtUtil();
                jwtUtil.validateToken(jwt);
                String username = jwtUtil.extractUsername(jwt);
                request.setAttribute("username", username);
                request.setAttribute("name", connection.getFullname(username));
                filterChain.doFilter(request, response);
            } catch (Exception ex) {
                Map<String, Object> errorDetails = new HashMap<>();

                errorDetails.put("message", ex.getMessage());
                errorDetails.put("status", HttpServletResponse.SC_FORBIDDEN);
                errorDetails.put("timestamp", (new Date()).toString());
                errorDetails.put("error", HttpServletResponse.SC_FORBIDDEN);
                errorDetails.put("path", request.getRequestURI());

                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");

                response.getWriter().write(mapper.writeValueAsString(errorDetails));
            }
        } else {
            Map<String, Object> errorDetails = new HashMap<>();

            errorDetails.put("message", "Authentication is required");
            errorDetails.put("status", HttpServletResponse.SC_FORBIDDEN);
            errorDetails.put("timestamp", (new Date()).toString());
            errorDetails.put("error", HttpServletResponse.SC_FORBIDDEN);
            errorDetails.put("path", request.getRequestURI());

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");

            response.getWriter().write(mapper.writeValueAsString(errorDetails));
        }
    }
}
