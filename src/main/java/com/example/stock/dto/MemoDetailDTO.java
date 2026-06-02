package com.example.stock.dto;

import java.time.LocalDateTime;
import java.util.List;

public class MemoDetailDTO {
    private Integer id;
    private String title;
    private String content;
    private Integer authorId;
    private String authorName;
    private String authorColor;
    private Integer categoryId;
    private String categoryName;
    private String categoryColor;
    private Integer stars;
    private Boolean pin;
    private List<TagInfo> tags;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static class TagInfo {
        private Integer id;
        private String name;
        private String color;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getAuthorId() { return authorId; }
    public void setAuthorId(Integer authorId) { this.authorId = authorId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getAuthorColor() { return authorColor; }
    public void setAuthorColor(String authorColor) { this.authorColor = authorColor; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getCategoryColor() { return categoryColor; }
    public void setCategoryColor(String categoryColor) { this.categoryColor = categoryColor; }
    public Integer getStars() { return stars; }
    public void setStars(Integer stars) { this.stars = stars; }
    public Boolean getPin() { return pin; }
    public void setPin(Boolean pin) { this.pin = pin; }
    public List<TagInfo> getTags() { return tags; }
    public void setTags(List<TagInfo> tags) { this.tags = tags; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
