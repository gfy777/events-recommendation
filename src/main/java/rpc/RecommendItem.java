package rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/recommendation")
public class RecommendItem extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userID = (String) request.getAttribute("username");

        double lat = Double.parseDouble(request.getParameter("lat"));
        double lon = Double.parseDouble(request.getParameter("lon"));

        DBConnection connection = DBConnectionFactory.getConnection();

        List<Item> recommendItems = connection.getRecommendItems(userID, lat, lon);

        ObjectMapper mapper = new ObjectMapper();

        RpcHelper.writeJson(response, mapper.writeValueAsString(recommendItems));
    }
}
