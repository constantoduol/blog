package com.quest.constant;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.*;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class DataServlet extends HttpServlet {

    private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private static BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    private static String REDIRECT_URL = "https://constantoduol.appspot.com";
    private static UserService userService = UserServiceFactory.getUserService();
    private static ArrayList skippables = new ArrayList();

    static {
        skippables.add("fetch_status");
        skippables.add("fetch_about");
        skippables.add("all_content");
        skippables.add("fetch_single_status");
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ensureLogin(req, resp);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ensureLogin(req, resp);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response, User user, String msg, JSONObject requestData) {
        try {
            if (msg.equals("new_status")) {
                saveStatus(requestData, response, user.getEmail());
            } else if (msg.equals("save_admin")) {
                createAdmin(requestData, response);
            } else if (msg.equals("fetch_status")) {
                fetchStatus(requestData, response, user);
            } else if (msg.equals("delete_post")) {
                deletePost(requestData, response);
            } else if (msg.equals("all_images")) {
                allImages(response);
            } else if (msg.equals("delete_image")) {
                deleteImage(requestData, response);
            } else if (msg.equals("save_about")) {
                saveAbout(requestData, response);
            } else if (msg.equals("fetch_about")) {
                fetchAbout(response);
            } else if (msg.equals("all_content")) {
                fetchAllContent(response);
            } else if (msg.equals("fetch_single_status")) {
                fetchSingleStatus(requestData, response, user);
            }
        } catch (Exception e) {
            Logger.getLogger(DataServlet.class.getName()).log(Level.SEVERE, e.toString());
        }
    }

    private void saveAbout(JSONObject data, HttpServletResponse resp) {
        String about = data.optString("about");
        Query query = new Query("About");
        Entity en = datastore.prepare(query).asSingleEntity();
        if (en == null) {
            en = new Entity("About");
            en.setProperty("value", new Text(about));
        } else {
            en.setProperty("value", new Text(about));
        }
        datastore.put(en);
        toClient("success", resp);
    }

    private void noteFetch(String timestamp, String title) {
        Query query = new Query("Traffic");
        Filter filter = new FilterPredicate("timestamp", FilterOperator.EQUAL, timestamp);
        query.setFilter(filter);
        Entity en = datastore.prepare(query).asSingleEntity();
        if (en == null) {
            Entity tr = new Entity("Traffic");
            tr.setProperty("timestamp", timestamp);
            tr.setProperty("count", 1);
            tr.setProperty("title", title);
            datastore.put(tr);
        } else {
            long count = (long) en.getProperty("count");
            count++;
            en.setProperty("count", count);
            datastore.put(en);
        }

    }

    private void fetchAllContent(HttpServletResponse resp) {
        try {
            Query query = new Query("Post").addSort("timestamp", SortDirection.DESCENDING);
            Iterable<Entity> entities = datastore.prepare(query).asIterable();
            JSONArray arr = new JSONArray();
            for (Entity en : entities) {
                JSONObject obj = new JSONObject();
                long timestamp = (long) en.getProperty("timestamp");
                String title = (String) en.getProperty("title");
                String category = (String) en.getProperty("category");
                obj.put("timestamp", timestamp);
                obj.put("title", title);
                obj.put("category", category);
                arr.put(obj);
            }
            toClient(arr, resp);
        } catch (Exception e) {

        }
    }

    private void fetchAbout(HttpServletResponse resp) {
        Query query = new Query("About");
        Entity en = datastore.prepare(query).asSingleEntity();
        String about = "";
        if (en != null) {
            about = ((Text) en.getProperty("value")).getValue();
        }
        toClient(about, resp);
    }

    private void saveStatus(JSONObject data, HttpServletResponse resp, String email) {
        try {
            String post = data.optString("new_status");
            String title = data.optString("title");
            String category = data.optString("category");
            Long timestamp = System.currentTimeMillis();
            Entity en = new Entity("Post");
            en.setProperty("status", new Text(post));
            en.setProperty("title", title);
            en.setProperty("timestamp", timestamp);
            en.setProperty("owner", email);
            en.setProperty("category", category);
            datastore.put(en);
            JSONObject response = new JSONObject();
            response.put("timestamp", timestamp);
            response.put("response", "success");
            toClient(response, resp);
        } catch (Exception e) {

        }
    }

    private void deletePost(JSONObject data, HttpServletResponse resp) {
        String timestamp = data.optString("timestamp");
        Filter filter = new FilterPredicate("timestamp", FilterOperator.EQUAL, Long.parseLong(timestamp));
        Query query = new Query("Post").setFilter(filter);
        Entity en = datastore.prepare(query).asSingleEntity();
        datastore.delete(en.getKey());
        toClient("success", resp);
    }

    private void allImages(HttpServletResponse resp) {
        try {
            Query query = new Query("Image").addSort("timestamp", SortDirection.DESCENDING);
            PreparedQuery pq = datastore.prepare(query);
            JSONArray all = new JSONArray();
            for (Entity en : pq.asIterable()) {
                JSONObject obj = new JSONObject();
                obj.put("blob_key", en.getProperty("blob_key"));
                obj.put("timestamp", en.getProperty("timestamp"));
                obj.put("blob_name", en.getProperty("blob_name"));
                all.put(obj);
            }
            toClient(all, resp);
        } catch (Exception e) {

        }
    }

    private void deleteImage(JSONObject data, HttpServletResponse resp) {
        String blobKey = data.optString("blob_key");
        blobstoreService.delete(new BlobKey(blobKey));
        Filter filter = new FilterPredicate("blob_key", FilterOperator.EQUAL, blobKey);
        Query query = new Query("Image").setFilter(filter);
        Entity en = datastore.prepare(query).asSingleEntity();
        datastore.delete(en.getKey());
        toClient("success", resp);
    }

    private void fetchStatus(JSONObject data, HttpServletResponse resp, User user) {
        try {
            String email = user == null ? "" : user.getEmail();
            String timestamp = data.optString("timestamp");
            List<Entity> statuses = null;
            if (timestamp.equals("")) {
                Query query = new Query("Post").addSort("timestamp", SortDirection.DESCENDING);
                statuses = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(10));
            } else {
                Filter filter = new FilterPredicate("timestamp", FilterOperator.LESS_THAN, Long.parseLong(timestamp));
                Query query = new Query("Post").setFilter(filter).addSort("timestamp", SortDirection.DESCENDING);
                statuses = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(10));
            }

            JSONArray all = new JSONArray();
            for (Entity status : statuses) {
                JSONObject obj = new JSONObject();
                obj.put("status", ((Text) status.getProperty("status")).getValue());
                obj.put("title", status.getProperty("title"));
                obj.put("timestamp", status.getProperty("timestamp"));
                obj.put("owner", status.getProperty("owner"));
                obj.put("category", status.getProperty("category"));
                obj.put("current_user", email);
                all.put(obj);
            }
            toClient(all, resp);
        } catch (Exception e) {

        }
    }

    private void fetchSingleStatus(JSONObject data, HttpServletResponse resp, User user) {
        try {
            String email = user == null ? "" : user.getEmail();
            String timestamp = data.optString("timestamp");
            Filter filter = new FilterPredicate("timestamp", FilterOperator.EQUAL, Long.parseLong(timestamp));
            Query query = new Query("Post").setFilter(filter);
            Entity status = datastore.prepare(query).asSingleEntity();
            String title = status.getProperty("title").toString();
            JSONArray all = new JSONArray();
            JSONObject obj = new JSONObject();
            obj.put("status", ((Text) status.getProperty("status")).getValue());
            obj.put("title", title);
            obj.put("timestamp", status.getProperty("timestamp"));
            obj.put("owner", status.getProperty("owner"));
            obj.put("category", status.getProperty("category"));
            obj.put("current_user", email);
            all.put(obj);
            noteFetch(timestamp, title);
            toClient(all, resp);
        } catch (Exception e) {

        }
    }

    public static void log(Object msg, Level level, Class cls) {
        Logger.getLogger(cls.getName()).log(level, msg.toString());
    }

    private void createAdmin(JSONObject data, HttpServletResponse resp) {
        String email = data.optString("admin_email");
        String option = data.optString("option");
        Filter filter = new FilterPredicate("email", FilterOperator.EQUAL, email);
        Query query = new Query("Admin").setFilter(filter);
        PreparedQuery pq = datastore.prepare(query);
        int count = 0;
        Entity theAdmin = null;
        for (Entity admin : pq.asIterable()) {
            count++;
            theAdmin = admin;
        }
        if (option.equals("create")) {
            if (count == 0) {
                Entity en = new Entity("Admin");
                en.setProperty("email", email);
                en.setProperty("timestamp", System.currentTimeMillis());
                datastore.put(en);
            }
        } else if (option.equals("delete")) {
            datastore.delete(theAdmin.getKey());
        }

        toClient("success", resp);
    }

    private void ensureLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            resp.setContentType("text/html;charset=UTF-8");
            String json = req.getParameter("json");
            if (json == null) {
                return;
            }
            JSONObject obj = new JSONObject(json);
            JSONObject headers = obj.optJSONObject("request_header");
            String msg = headers.optString("request_msg");
            JSONObject requestData = (JSONObject) obj.optJSONObject("request_object");
            User user = userService.getCurrentUser();
            if (skippables.indexOf(msg) > -1) {
                processRequest(req, resp, user, msg, requestData);
                return;
                //no auth
            }
            if (user == null) {
                sendRedirect(resp);
                return;
            }
            String email = user.getEmail();
            Query query = new Query("Admin");
            PreparedQuery pq = datastore.prepare(query);
            boolean auth = false;
            for (Entity admin : pq.asIterable()) {
                String storedEmail = (String) admin.getProperty("email");
                if (storedEmail.equals(email)) {
                    auth = true;
                }
            }
            if (auth) {
                processRequest(req, resp, user, msg, requestData);
            } else {
                sendRedirect(resp);
            }
        } catch (Exception e) {

        }
    }

    private void sendRedirect(HttpServletResponse resp) {
        try {
            String url = userService.createLoginURL(REDIRECT_URL);
            JSONObject toClient = new JSONObject();
            toClient.put("data", url);
            toClient.put("type", "auth_url");
            PrintWriter writer = resp.getWriter();
            writer.println(toClient);
        } catch (Exception e) {

        }
    }

    private void toClient(Object response, HttpServletResponse resp) {
        try {
            JSONObject toClient = new JSONObject();
            toClient.put("data", response);
            PrintWriter writer = resp.getWriter();
            writer.println(toClient);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}
