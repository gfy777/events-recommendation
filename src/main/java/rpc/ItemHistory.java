package rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import db.DBConnection;
import db.DBConnectionFactory;
import entity.Favorite;
import entity.Item;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet("/history")
public class ItemHistory extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder str = new StringBuilder();
        ObjectMapper mapper = new ObjectMapper();

        String userID = (String) request.getAttribute("username");

        try {
            BufferedReader in = request.getReader();
            String line = null;
            while ((line = in.readLine()) != null) {
                str.append(line);
            }
            in.close();
            Favorite favorite = mapper.readValue(str.toString(), Favorite.class);

            DBConnection connection = DBConnectionFactory.getConnection();
            connection.setFavoriteItem(userID, favorite.getFavorites());

            Map<String, String> res = new HashMap<>();
            res.put("status", "SUCCESS");

            System.out.println(mapper.writeValueAsString(favorite));

            RpcHelper.writeJson(response, mapper.writeValueAsString(res));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userID = (String) request.getAttribute("username");

        try {
            DBConnection connection = DBConnectionFactory.getConnection();
            List<Item> favoriteItems = connection.getFavoriteItems(userID);
            ObjectMapper mapper = new ObjectMapper();

            List<String> userFavorites = connection.getFavoriteItemIds(userID);
            for(Item item : favoriteItems) {
                item.setFavorite(userFavorites.contains(item.getId()));
            }

            RpcHelper.writeJson(response, mapper.writeValueAsString(favoriteItems));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder str = new StringBuilder();
        ObjectMapper mapper = new ObjectMapper();
        String userID = (String) request.getAttribute("username");
        try {
            BufferedReader in = request.getReader();
            String line = null;
            while ((line = in.readLine()) != null) {
                str.append(line);
            }
            in.close();
            Favorite favorite = mapper.readValue(str.toString(), Favorite.class);

            DBConnection connection = DBConnectionFactory.getConnection();
            connection.unsetFavoriteItem(userID, favorite.getFavorites());

            Map<String, String> res = new HashMap<>();
            res.put("status", "SUCCESS");

            System.out.println(mapper.writeValueAsString(favorite));

            RpcHelper.writeJson(response, mapper.writeValueAsString(res));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
