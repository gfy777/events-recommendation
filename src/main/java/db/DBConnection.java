package db;

import entity.Item;

import java.util.List;
import java.util.Set;

public interface DBConnection {

    public void close() throws Exception;

    public void setFavoriteItem(String userID, List<String> itemIds);

    public void unsetFavoriteItem(String userID, List<String> itemIds);

    public List<String> getFavoriteItemIds(String userID);

    public List<Item> getFavoriteItems(String userID);

    public List<Item> getRecommendItems(String userID, double lat, double lon);

    public List<String> getCategories(String itemID);

    public List<Item> searchItems(double lat, double lon, String term);

    public void saveItem(Item item);

    public String getFullname(String userID);

    public boolean verifyLogin(String userID, String password);
}
