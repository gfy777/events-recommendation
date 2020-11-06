package entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
    private String id;
    private String name;
    private double rating;
    private String address;
    private List<String> categories;
    private String imageUrl;
    private String url;
    private String time;
    private double distance;
    private boolean favorite;

    public Item() {
    }

    private Item(ItemBuilder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.rating = builder.rating;
        this.address = builder.address;
        this.categories = builder.categories;
        this.imageUrl = builder.imageUrl;
        this.url = builder.url;
        this.distance = builder.distance;
        this.time = builder.time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public static class ItemBuilder {
        private String id;
        private String name;
        private double rating;
        private String address;
        private List<String> categories;
        private String imageUrl;
        private String url;
        private String time;
        private double distance;

        public ItemBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public ItemBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public ItemBuilder setRating(double rating) {
            this.rating = rating;
            return this;
        }

        public ItemBuilder setAddress(String address) {
            this.address = address;
            return this;
        }

        public ItemBuilder setCategories(List<String> categories) {
            this.categories = categories;
            return this;
        }

        public ItemBuilder setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public ItemBuilder setUrl(String url) {
            this.url = url;
            return this;
        }

        public ItemBuilder setDistance(double distance) {
            this.distance = distance;
            return this;
        }

        public ItemBuilder setTime(String time) {
            this.time = time;
            return this;
        }

        public Item build() {
            return new Item(this);
        }
    }

    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", rating=" + rating +
                ", address='" + address + '\'' +
                ", categories=" + categories +
                ", imageUrl='" + imageUrl + '\'' +
                ", url='" + url + '\'' +
                ", time='" + time + '\'' +
                ", distance=" + distance +
                '}';
    }
}
