package eu.spod.isislab.spodapp.entities;

/**
 * Created by Utente on 08/09/2017.
 */
public class AgoraRoom {
    String ownerId;
    String subject;
    String body;
    String views;
    String comments;
    String opendata;
    String timestamp;
    String post;
    String id;
    String datalet_graph;


    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getOpendata() {
        return opendata;
    }

    public void setOpendata(String opendata) {
        this.opendata = opendata;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDatalet_graph() {
        return datalet_graph;
    }

    public void setDatalet_graph(String datalet_graph) {
        this.datalet_graph = datalet_graph;
    }

    public AgoraRoom(String ownerId, String subject, String body, String views, String comments, String opendata, String timestamp, String post, String id, String datalet_graph) {

        this.ownerId = ownerId;
        this.subject = subject;
        this.body = body;
        this.views = views;
        this.comments = comments;
        this.opendata = opendata;
        this.timestamp = timestamp;
        this.post = post;
        this.id = id;
        this.datalet_graph = datalet_graph;
    }
}
