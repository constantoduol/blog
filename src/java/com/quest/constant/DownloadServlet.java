package com.quest.constant;

import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

public class DownloadServlet extends HttpServlet {

    private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private static ImagesService imageService = ImagesServiceFactory.getImagesService();

    public void doPost(HttpServletRequest req, HttpServletResponse res) {
        try {
            res.setContentType("text/html;charset=UTF-8");
            String json = req.getParameter("json");
            if (json == null) {
                return;
            }
            JSONObject obj = new JSONObject(json);
            JSONObject headers = obj.optJSONObject("request_header");
            String msg = headers.optString("request_msg");
            JSONObject requestData = (JSONObject) obj.optJSONObject("request_object");
            if (msg.equals("download_image")) {
                downloadImage(requestData, res);
            } else if (msg.equals("download_all_images")) {
                downloadAllImages(res);
            }
        } catch (Exception e) {

        }
    }

    private void downloadImage(JSONObject requestData, HttpServletResponse res) {
        try {
            String name = requestData.optString("name");
            Filter filter = new FilterPredicate("blob_name", FilterOperator.EQUAL, name);
            Query query = new Query("Image").setFilter(filter);
            PreparedQuery pq = datastore.prepare(query);
            Entity blobInfo = pq.asSingleEntity();
            String blobKey = (String) blobInfo.getProperty("blob_key");
            BlobKey key = new BlobKey(blobKey);
            String url = imageService.getServingUrl(ServingUrlOptions.Builder.withBlobKey(key));
            toClient(url, res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadAllImages(HttpServletResponse res) {
        try {
            Query query = new Query("Image");
            PreparedQuery pq = datastore.prepare(query);
            JSONArray all = new JSONArray();
            for (Entity en : pq.asIterable()) {
                String blobKey = (String) en.getProperty("blob_key");
                BlobKey key = new BlobKey(blobKey);
                String url = imageService.getServingUrl(ServingUrlOptions.Builder.withBlobKey(key));
                all.put(url);
            }

            toClient(all, res);
        } catch (Exception e) {
            e.printStackTrace();
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
