# Events Recommendation Systems
   A content-based event recommendation system.

## Frontend
- An interactive web page with `HTML`, `CSS`, `JavaScript` and `AJAX`. The website has following main functions
   * **Search** events around
   * Get user current location by built-in Web APIs Navigator, or failover to "http://ipinfo.io".
   * **Login** user and provide user access token.
   * Set or unset **Favorite** events (login user only)
   * Get personalized **Recommendation** around based on the user's favorite history

## Backend
- A web service with `RESTful APIs` using `Java Servlet` to handle HTTP requests and process logic, and details are as below:
   * Get all events data from [TicketMaster Discovery API](https://developer.ticketmaster.com/products-and-docs/apis/discovery-api/v2/).
   * Built with both relational database (`MySQL` or `PostgreSQL`) or NoSQL cloud database (`Cloud Firestore`) to 
   support data storage from users and events searched in TicketMaster API
   * Use `c3p0` connection pool to improve performance of database
   * Perform authorization with `JWT` token
   * Design **content-based recommendation algorithm** for event recommendation
- Deploy website server on `Heroku dyno`: [Event Recommendation System](https://mars-events-recommendation.herokuapp.com/)

## API Design
   * /search
      * search events around by provided location
      * call [Ticketmaster API](https://developer.ticketmaster.com/products-and-docs/apis/discovery-api/v2/) to get events information
      * parse, clean and save events data to database
      * return response
   * /login
     * POST the username and password, verify with the user's information stored in database
     * return generated `JWT` token in response
   * /history
      * get, set, delete favorite events
      * update database
      * return response
   * /recommendation
      * recommend events around based on user's favorite history
      * get user's favorite history from database
      * search similar events, and sort by **content-based recommendation algorithm**
      * return response

## Database Design
- Relational Database
   * **users** - store user information, such as username, full name and password
   * **items** - store event information.
   * **category** - store event-category relationship
   * **history** - store user favorite history

- NoSQL Cloud Database
   * **users** - store user information and favorite history.
   * **items** - store event information and event's categories.

## Design Patterns
   * **Builder pattern**: `entity.Item.java`
      * After collect event data from TicketMaster API, use builder pattern to instantiate event object. 
   * **Factory pattern**: `db.DBConnectionFactory.java`
      * Support multiple database to improve flexibility and reusability.
   * **Singleton pattern**: `db.DBConnectionFactory.java`
      * Only create one instance of database connection pool, and give the global access to outer class

## User Behavior Analysis (Working in progress)
- Online (**ElasticSearch**, **Logstash**, **Kibana**)
   * Use Logstash to fetch log, then store data in ElasticSearch, finally use Kibana to get virtualized graphs like APIs use, request status, geolocation of visitors, etc

