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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@WebServlet("/search")
public class SearchItem extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.addHeader("Access-Control-Allow-Origin", "*");

        List<Item> itemList = new ArrayList<>();



        try {
            double lat = Double.parseDouble(request.getParameter("lat"));
            double lon = Double.parseDouble(request.getParameter("lon"));
            String keyword = request.getParameter("term");

            DBConnection dbConnection = DBConnectionFactory.getConnection();
            itemList = dbConnection.searchItems(lat, lon, keyword);

            String userId = (String) request.getAttribute("username");
            if (userId != null) {
                List<String> userFavorites = dbConnection.getFavoriteItemIds(userId);
                for(Item item : itemList) {
                    item.setFavorite(userFavorites.contains(item.getId()));
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        ObjectMapper mapper = new ObjectMapper();
        out.println(mapper.writeValueAsString(itemList));

        out.close();
    }
}
