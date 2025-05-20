package documentManagement.request;

import java.time.LocalDateTime;

public class DocumentSelectionRequest {

    private String author;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String type;
    private int page;
    private int size;
    private String sortBy;  // Field to sort by
    private String sortOrder; // "asc" or "desc"
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public LocalDateTime getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    public LocalDateTime getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public int getPage() {
        return page;
    }
    public void setPage(int page) {
        this.page = page;
    }
    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
    public String getSortBy() {
        return sortBy;
    }
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    public String getSortOrder() {
        return sortOrder;
    }
    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}
