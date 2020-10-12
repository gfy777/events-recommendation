package entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Favorite {
    private String userId;

    private List<String> favorites;

    public Favorite() {

    }

    public Favorite(String userId, List<String> favorites) {
        this.userId = userId;
        this.favorites = favorites;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<String> favorites) {
        this.favorites = favorites;
    }
}
