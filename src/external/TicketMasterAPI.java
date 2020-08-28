package external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import entity.Item;
import entity.Item.ItemBuilder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TicketMasterAPI {
    private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
    private static final String DEFAULT_KEYWORD = "";
    private static final String API_KEY = "";

    // currently only get the first address
    public String getAddress(JsonNode event) {
        if (event.hasNonNull("_embedded")) {
            JsonNode embedded = event.get("_embedded");

            if (embedded.hasNonNull("venues") && embedded.get("venues").isArray()) {
                ArrayNode venues = (ArrayNode) embedded.get("venues");

                for (int i = 0; i < venues.size(); i++) {
                    JsonNode venue = venues.get(i);

                    StringBuilder str = new StringBuilder();

                    if (venue.hasNonNull("address")) {
                        JsonNode address = venue.get("address");
                        if (address.hasNonNull("line1")) {
                            str.append(address.get("line1").asText());
                            str.append(" ");
                        }
                        if (address.hasNonNull("line2")) {
                            str.append(address.get("line2").asText());
                            str.append(" ");
                        }
                        if (address.hasNonNull("line3")) {
                            str.append(address.get("line3").asText());
                            str.append(" ");
                        }
                    }

                    if (venue.hasNonNull("city")) {
                        JsonNode city = venue.get("city");

                        if (city.hasNonNull("name")) {
                            str.append(city.get("name").asText());
                            str.append(" ");
                        }
                    }

                    if (str.length() != 0) {
                        return str.toString();
                    }
                }
            }
        }
        return "";
    }

    public String getImageUrl(JsonNode event) {

        if (event.hasNonNull("images") && event.get("images").isArray()) {
            ArrayNode images = (ArrayNode) event.get("images");

            for (int i = 0; i < images.size(); i++) {
                JsonNode image = images.get(i);

                if (image.hasNonNull("url")) {
                    return image.get("url").asText();
                }
            }
        }

        return "";
    }

    public Set<String> getCategories(JsonNode event) {

        Set<String> categories = new HashSet<>();
        if (event.hasNonNull("classifications") && event.get("classifications").isArray()) {
            ArrayNode classifications = (ArrayNode) event.get("classifications");

            for (int i = 0; i < classifications.size(); i++) {
                JsonNode classification = classifications.get(i);

                if (classification.hasNonNull("segment")) {
                    JsonNode segment = classification.get("segment");

                    if (segment.hasNonNull("name")) {
                        categories.add(segment.get("name").asText());
                    }
                }
            }
        }

        return categories;
    }

    public List<Item> getItemList(ArrayNode events) {
        List<Item> itemList = new ArrayList<>();

        for (int i = 0; i < events.size(); i++) {
            JsonNode event = events.get(i);

            ItemBuilder builder = new ItemBuilder();

            if (event.hasNonNull("name")) {
                builder.setName(event.get("name").asText());
            }
            if (event.hasNonNull("id")) {
                builder.setId(event.get("id").asText());
            }
            if (event.hasNonNull("url")) {
                builder.setUrl(event.get("url").asText());
            }
            if (event.hasNonNull("rating")) {
                builder.setRating(event.get("rating").asDouble());
            }
            if (event.hasNonNull("distance")) {
                builder.setDistance(event.get("distance").asDouble());
            }

            builder.setCategories(getCategories(event));
            builder.setAddress(getAddress(event));
            builder.setImageUrl(getImageUrl(event));

            itemList.add(builder.build());

        }

        return itemList;
    }

    public List<Item> search(double lat, double lon, String keyword) {
        if (keyword == null) {
            keyword = DEFAULT_KEYWORD;
        }

        // encode input
        try {
            keyword = java.net.URLEncoder.encode(keyword, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // get geoHash
        String geoHash = GeoHash.encodeGeohash(lat, lon, 8);
        // create ticketmaster api query
        String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s",
                API_KEY, geoHash, keyword, 50);

        ObjectMapper mapper = new ObjectMapper();

        try {
            // build connection. must cast to HttpURLConnection to support http status code
            HttpURLConnection connection = (HttpURLConnection) new URL(URL + "?" + query).openConnection();

            System.out.println("\nSending 'GET' request to URL: " + URL + "?" + query);
            System.out.println("Response Code: " + connection.getResponseCode());

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {

            }

            // Pass connection input stream to a stream reader, then pass to buffered reader
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            StringBuilder response = new StringBuilder();

            // read from buffered reader and write to response
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // convert response string to json
            JsonNode jsonNode = mapper.readTree(response.toString());
            if (!jsonNode.hasNonNull("_embedded")) {
                return new ArrayList<>();
            }
            JsonNode embedded = jsonNode.get("_embedded");
            if (!embedded.hasNonNull("events")) {
                return new ArrayList<>();
            }
            if (!embedded.get("events").isArray()) {
                return new ArrayList<>();
            }


            return getItemList((ArrayNode) embedded.get("events"));

        } catch (IOException e) {
            e.printStackTrace();
        }

        // in case of exception, return empty array node
        return new ArrayList<>();
    }

    // for debug purpose
    private void queryAPI(double lat, double lon) {
        List<Item> events = search(lat, lon, null);
        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println(mapper.writeValueAsString(events));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TicketMasterAPI tmAPI = new TicketMasterAPI();
        tmAPI.queryAPI(29.682684, -95.295410);
    }
}
