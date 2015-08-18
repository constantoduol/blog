package com.quest.constant;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

public class UploadServlet extends HttpServlet {

    private static BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private static String REDIRECT_URL = "https://constantoduol.appspot.com";

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        ensureLogin(req, res);
    }

    private void uploadImages(HttpServletRequest req, HttpServletResponse res) {
        // Get the image representation
        try {
            Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);

            Map<String, List<BlobInfo>> blobInfos = blobstoreService.getBlobInfos(req);
            List<BlobKey> blobKeys = blobs.get("file");
            List<BlobInfo> blobInfo = blobInfos.get("file");
            for (int x = 0; x < blobKeys.size(); x++) {
                Entity en = new Entity("Image");
                en.setProperty("blob_key", blobKeys.get(x).getKeyString());
                en.setProperty("timestamp", System.currentTimeMillis());
                en.setProperty("blob_name", blobInfo.get(x).getFilename());
                datastore.put(en);
            }

            res.getWriter().write("Images uploaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ensureLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        if (user == null) {
            try {
                String url = userService.createLoginURL(REDIRECT_URL);
                JSONObject toClient = new JSONObject();
                toClient.put("data", url);
                toClient.put("type", "auth_url");
                PrintWriter writer = resp.getWriter();
                writer.println(toClient);
                resp.sendRedirect(url);
                return;
            } catch (Exception e) {

            }
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
            uploadImages(req, resp);
        } else {
            try {
                String url = userService.createLoginURL(REDIRECT_URL);
                JSONObject toClient = new JSONObject();
                toClient.put("data", url);
                toClient.put("type", "auth_url");
                PrintWriter writer = resp.getWriter();
                writer.println(toClient);
                resp.sendRedirect(url);
            } catch (Exception e) {

            }
        }
    }
}
