package documentManagement.repository;

import documentManagement.model.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    // Basic keyword matching using LIKE and LOWER for case-insensitivity
    @Query("SELECT d FROM Document d WHERE LOWER(d.content) LIKE LOWER(concat('%', :keyword, '%'))")
    Page<Document> findByContentContainingKeyword(@Param("keyword") String keyword, Pageable pageable);
    // Full-text search (MySQL specific, requires FULLTEXT index)
    @Query(value = "SELECT * FROM documents WHERE MATCH(content) AGAINST (?1 IN NATURAL LANGUAGE MODE)", nativeQuery = true)
    Page<Document> fullTextSearch(String keyword, Pageable pageable);
    // Filter documents by metadata
    Page<Document> findByAuthor(String author, Pageable pageable);
    Page<Document> findByUploadDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<Document> findByType(String type, Pageable pageable);
}
