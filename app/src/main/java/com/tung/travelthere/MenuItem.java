package com.tung.travelthere;

public class MenuItem {
    private String content;
    private String userName;
    private String userImageURL;
    private String contentImageURL;

    public MenuItem(String content, String userName, String userImageURL, String contentImageURL) {
        this.content = content;
        this.userName = userName;
        this.userImageURL = userImageURL;
        this.contentImageURL = contentImageURL;
    }

    public MenuItem() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImageURL() {
        return userImageURL;
    }

    public void setUserImageURL(String userImageURL) {
        this.userImageURL = userImageURL;
    }

    public String getContentImageURL() {
        return contentImageURL;
    }

    public void setContentImageURL(String contentImageURL) {
        this.contentImageURL = contentImageURL;
    }
}
