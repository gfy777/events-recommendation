package db.postgresql;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;
import external.TicketMasterAPI;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.*;

public class PostgreSqlConnection implements DBConnection {
    private Connection connection;

    public PostgreSqlConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void close() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setFavoriteItem(String userID, List<String> itemIds) {
        if (connection == null) {
            return;
        }

        try {
            String sql = "INSERT INTO history (user_id, item_id) VALUES (?, ?) ON CONFLICT DO NOTHING;";
            PreparedStatement statement = connection.prepareStatement(sql);

            for (String itemId : itemIds) {
                statement.setString(1, userID);
                statement.setString(2, itemId);
                System.out.println(statement.toString());
                statement.execute();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    @Override
    public void unsetFavoriteItem(String userID, List<String> itemIds) {
        if (connection == null) {
            return;
        }

        try {
            String sql = "DELETE from history WHERE user_id = ? AND item_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);

            for (String itemId : itemIds) {
                statement.setString(1, userID);
                statement.setString(2, itemId);
                statement.execute();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public List<String> getFavoriteItemIds(String userID) {
        List<String> favoriteItemIds = new ArrayList<>();
        String sql = "SELECT item_id FROM history WHERE user_id = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, userID);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String itemId = resultSet.getString("item_id");
                favoriteItemIds.add(itemId);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return favoriteItemIds;
    }

    @Override
    public List<Item> getFavoriteItems(String userID) {
        List<Item> favoriteItems = new ArrayList<>();
        List<String> favoriteItemIds = this.getFavoriteItemIds(userID);

        try {
            String sql = "SELECT * FROM items WHERE item_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);

            for (String id : favoriteItemIds) {
                statement.setString(1, id);

                ResultSet resultSet = statement.executeQuery();
                Item.ItemBuilder itemBuilder = new Item.ItemBuilder();

                while (resultSet.next()) {
                    favoriteItems.add(itemBuilder.setId(resultSet.getString("item_id"))
                            .setName(resultSet.getString("name"))
                            .setAddress(resultSet.getString("address"))
                            .setImageUrl(resultSet.getString("image_url"))
                            .setUrl(resultSet.getString("url"))
                            .setTime(resultSet.getString("time"))
                            .setDistance(resultSet.getDouble("distance"))
                            .setRating((resultSet.getDouble("rating")))
                            .setCategories(this.getCategories(id))
                            .build());
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return favoriteItems;
    }

    @Override
    public List<Item> getRecommendItems(String userID, double lat, double lon) {
        // get user's all favorite items
        List<Item> favorites = this.getFavoriteItems(userID);
        Set<String> favoriteID = new HashSet<>();

        // fetch all favorite items' categories
        Map<String, Integer> sortedFavoriteCategories = new HashMap<>();
        for (Item item : favorites) {
            for (String category : item.getCategories()) {
                if (sortedFavoriteCategories.containsKey(category)) {
                    sortedFavoriteCategories.put(category, sortedFavoriteCategories.get(category) + 1);
                } else {
                    sortedFavoriteCategories.put(category, 1);
                }
            }
            favoriteID.add(item.getId());
        }

        List<Map.Entry<String, Integer>> categoryList = new ArrayList<>(sortedFavoriteCategories.entrySet());
        categoryList.sort(new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                if (o1.getValue().equals(o2.getValue())) {
                    return 0;
                }
                return o1.getValue() > o2.getValue() ? -1 : 1;
            }
        });

        // do search based on categories, filter out user already favorites
        List<Item> recommendItems = new ArrayList<>();
        Set<String> dedupItemID = new HashSet<>();

        for(Map.Entry<String, Integer> category : categoryList) {
            List<Item> items = this.searchItems(lat, lon, category.getKey());
            List<Item> filteredItem = new ArrayList<>();
            for (Item item : items) {
                if (!favoriteID.contains(item.getId()) && !dedupItemID.contains(item.getId())) {
                    filteredItem.add(item);
                }
                dedupItemID.add(item.getId());
            }

            filteredItem.sort(new Comparator<Item>() {
                @Override
                public int compare(Item o1, Item o2) {
                    return Double.compare(o1.getDistance(), o2.getDistance());
                }
            });

            recommendItems.addAll(filteredItem);
        }
        return recommendItems;

    }

    @Override
    public List<String> getCategories(String itemID) {
        Set<String> categories = new HashSet<>();

        try {
            String sql = "SELECT category FROM categories WHERE item_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, itemID);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                categories.add(resultSet.getString("category"));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return new ArrayList<>(categories);
    }

    @Override
    public List<Item> searchItems(double lat, double lon, String term) {
        TicketMasterAPI ticketMasterAPI = new TicketMasterAPI();
        List<Item> items = ticketMasterAPI.search(lat, lon, term);
        // if anyone search any item, will save the results to DB
        // in case if use out of ticket master api daily limits
        for (Item i : items) {
            saveItem(i);
        }

        return items;
    }

    @Override
    public void saveItem(Item item) {
        if (connection == null) {
            return;
        }
        try {
            // save to item database
            String sql = "INSERT INTO items VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, item.getId());
            statement.setString(2, item.getName());
            statement.setDouble(3, item.getRating());
            statement.setString(4, item.getAddress());
            statement.setString(5, item.getImageUrl());
            statement.setString(6, item.getUrl());
            statement.setString(7, item.getTime());
            statement.setDouble(8, item.getDistance());
            statement.execute();

            // then save item-category
            sql = "INSERT INTO categories VALUES (?,?) ON CONFLICT DO NOTHING;";
            statement = connection.prepareStatement(sql);
            for (String category : item.getCategories()) {
                statement.setString(1, item.getId());
                statement.setString(2, category);
                statement.execute();
            }


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    @Override
    public String getFullname(String userID) {
        if (connection == null) {
            return null;
        }
        String name = "";

        try {
            String sql = "SELECT first_name, last_name from users WHERE user_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, userID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                name = String.join(" ",
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return name;
    }

    @Override
    public boolean verifyLogin(String userID, String password) {
        if (connection == null) {
            return false;
        }

        try {
            String sql = "SELECT password FROM users WHERE user_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, userID);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String savedHashedPassword = resultSet.getString("password");
                return BCrypt.checkpw(password, savedHashedPassword);
            }

            return false;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return false;
    }
}
