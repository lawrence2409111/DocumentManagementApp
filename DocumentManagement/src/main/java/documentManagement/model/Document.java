package documentManagement.model;

import javax.persistence.*;
import java.time.LocalDateTime;

// Document Entity
@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Lob // Use @Lob for large text fields
    @Column(columnDefinition = "LONGBLOB") // Use LONGBLOB for file storage
    private byte[] content;
    private String author;
    private LocalDateTime uploadDate;
    private String type; // e.g., "text", "pdf", "docx"
    private String originalFileName; // To store the original file name
    // Constructors, getters, and setters
    public Document() {}
    public Document(String title, byte[] content, String author, LocalDateTime uploadDate, String type, String originalFileName) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.uploadDate = uploadDate;
        this.type = type;
        this.originalFileName = originalFileName;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public byte[] getContent() {
        return content;
    }
    public void setContent(byte[] content) {
        this.content = content;
    }
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public LocalDateTime getUploadDate() {
        return uploadDate;
    }
    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getOriginalFileName() {
        return originalFileName;
    }
    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
}
