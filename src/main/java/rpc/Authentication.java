package rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import db.DBConnection;
import db.DBConnectionFactory;
import entity.Auth;
import util.JwtUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/login")
public class Authentication extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder str = new StringBuilder();
        ObjectMapper mapper = new ObjectMapper();

        try {
            BufferedReader in = request.getReader();
            String line = null;
            while ((line = in.readLine()) != null) {
                str.append(line);
            }
            in.close();

            Auth auth = mapper.readValue(str.toString(), Auth.class);

            DBConnection connection = DBConnectionFactory.getConnection();

            if (connection.verifyLogin(auth.getUsername(), auth.getPassword())) {
                JwtUtil jwtUtil = new JwtUtil();
                String jwt = jwtUtil.generateToken(auth.getUsername());
                Map<String, String> res = new HashMap<>();

                res.put("jwt", jwt);

                RpcHelper.writeJson(response, mapper.writeValueAsString(res));
            } else {
                Map<String, String> res = new HashMap<>();
                res.put("error", "Password Invalid");
                RpcHelper.writeJson(response, mapper.writeValueAsString(res));
            }
        } catch (Exception e) {
            Map<String, String> res = new HashMap<>();
            res.put("error", "Authentication failed");
            RpcHelper.writeJson(response, mapper.writeValueAsString(res));
        }
    }
}
