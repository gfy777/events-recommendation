package db.firestore;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import db.DBConnection;
import entity.Item;
import external.TicketMasterAPI;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class FirestoreConnection implements DBConnection {

    //
    private Firestore db;

    public FirestoreConnection() {
        // Use a service account
        try {
            // User the local service account json file
            // InputStream serviceAccount = new FileInputStream("D:\\IdeaProjects\\events-recommendation\\src\\main\\java\\db\\firestore\\mars-fc78f-firebase-adminsdk-r1st5-ec48c610ed.json");
            // GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

            // Use the application default credentials
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();
            // avoid re-create firebase app
            if(FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            db = FirestoreClient.getFirestore();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void close() throws Exception {
        db.close();
    }

    @Override
    public void setFavoriteItem(String userID, List<String> itemIds) {
        if (db == null) {
            return;
        }

        try {
            DocumentReference user = db.collection("user").document(userID);
            // asynchronously retrieve the document
            ApiFuture<DocumentSnapshot> future = user.get();
            // future.get() blocks on response
            DocumentSnapshot document = future.get();

            // if user not exists
            if (!document.exists()) {
                return ;
            }

            List<String> currUserFavorite = new ArrayList<>();
            Set<String> favoriteSet = new HashSet<>();
            if (document.contains("favorites")) {
                currUserFavorite = (List<String>) document.get("favorites");
                favoriteSet = new HashSet<>(currUserFavorite);
            }

            for (String itemId : itemIds) {
                // if user : item pair exist
                if (!favoriteSet.contains(itemId)) {
                    currUserFavorite.add(itemId);
                }
            }

            user.update("favorites", currUserFavorite).get();

        } catch (InterruptedException | ExecutionException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void unsetFavoriteItem(String userID, List<String> itemIds) {
        if (db == null) {
            return;
        }

        try {
            DocumentReference user = db.collection("user").document(userID);
            // asynchronously retrieve the document
            ApiFuture<DocumentSnapshot> future = user.get();
            // future.get() blocks on response
            DocumentSnapshot document = future.get();

            // if user not exists
            if (!document.exists()) {
                return ;
            }

            List<String> currUserFavorite = new ArrayList<>();
            if (document.contains("favorites")) {
                currUserFavorite = (List<String>) document.get("favorites");
                currUserFavorite.removeAll(itemIds);
            }
            user.update("favorites", currUserFavorite);

        } catch (InterruptedException | ExecutionException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public List<String> getFavoriteItemIds(String userID) {
        List<String> favoriteItemIdSet = new ArrayList<>();
        if (db == null) {
            return favoriteItemIdSet;
        }

        try {
            DocumentReference user = db.collection("user").document(userID);
            // asynchronously retrieve the document
            ApiFuture<DocumentSnapshot> future = user.get();
            // future.get() blocks on response
            DocumentSnapshot document = future.get();

            // if user not exists
            if (!document.exists()) {
                return favoriteItemIdSet;
            }

            List<String> currUserFavorite = new ArrayList<>();
            if (document.contains("favorites")) {
                currUserFavorite = (List<String>) document.get("favorites");
            }

            favoriteItemIdSet.addAll(currUserFavorite);
        } catch (InterruptedException | ExecutionException throwables) {
            throwables.printStackTrace();
        }
        return favoriteItemIdSet;
    }

    @Override
    public List<Item> getFavoriteItems(String userID) {

        List<Item> favoriteItems = new ArrayList<>();
        List<String> favoriteItemIds = this.getFavoriteItemIds(userID);

        if (favoriteItemIds.isEmpty()) {
            return favoriteItems;
        }

        if (db == null) {
            return favoriteItems;
        }

        try {
            CollectionReference eventCollection = db.collection("events");
            Query query = eventCollection.whereIn("id", favoriteItemIds);

            ApiFuture<QuerySnapshot> querySnapshot = query.get();

            for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
                favoriteItems.add(document.toObject(Item.class));
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
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
        if (db == null) {
            return new ArrayList<>();
        }
        try {
            DocumentReference event = db.collection("events").document(itemID);
            ApiFuture<DocumentSnapshot> future = event.get();
            // future.get() blocks on response
            DocumentSnapshot document = future.get();
            if (!document.exists()) {
                return null;
            }
            // convert document to POJO
            Item item = document.toObject(Item.class);
            return item.getCategories();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
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
        if (db == null) {
            return;
        }
        try {
            String documentId = item.getId();
            // Async update the events database
            db.collection("events").document(documentId).set(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getFullname(String userID) {
        if (db == null) {
            return null;
        }

        String name = "";

        try {
            DocumentReference documentReference = db.collection("user").document(userID);
            // asynchronously retrieve the document
            ApiFuture<DocumentSnapshot> future = documentReference.get();
            // future.get() blocks on response
            DocumentSnapshot document = future.get();
            if (!document.exists()) {
                return null;
            }

            if (document.contains("first_name") && document.contains("last_name")) {
                name = String.join(" ",
                        document.getString("first_name"),
                        document.getString("last_name"));
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return name;
    }

    @Override
    public boolean verifyLogin(String userID, String password) {
        if (db == null) {
            return false;
        }

        try {
            DocumentReference user = db.collection("user").document(userID);
            // asynchronously retrieve the document
            ApiFuture<DocumentSnapshot> future = user.get();
            // future.get() blocks on response
            DocumentSnapshot document = future.get();
            if (!document.exists()) {
                return false;
            }

            if (document.contains("password") && document.getString("password").equals(password)) {
                return true;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return false;
    }
}
