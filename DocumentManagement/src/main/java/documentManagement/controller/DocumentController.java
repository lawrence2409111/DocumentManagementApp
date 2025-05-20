package documentManagement.controller;

import documentManagement.model.Document;
import documentManagement.model.Role;
import documentManagement.model.UserEntity;
import documentManagement.repository.DocumentRepository;
import documentManagement.repository.RoleRepository;
import documentManagement.repository.UserRepository;
import documentManagement.request.DocumentRequest;
import documentManagement.request.DocumentSelectionRequest;
import documentManagement.request.QARequest;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @PostMapping(value = "/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')") // Only admin/editor can ingest
    public CompletableFuture<ResponseEntity<String>> ingestDocument(
            @RequestParam("title") String title,
            @RequestParam("author") String author,
            @RequestParam("type") String type,
            @RequestParam("file") MultipartFile file) { // Use MultipartFile

        // Log the start of the request
        log.info("Received document ingestion request for title: {}", title);

        // Immediately return a CompletableFuture to indicate async processing
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validate file type
                if (!isValidFileType(type)) {
                    log.warn("Invalid file type '{}' for document '{}'", type, title);
                    return new ResponseEntity<>("Invalid file type. Must be one of text, pdf, docx", HttpStatus.BAD_REQUEST);
                }

                // Store the file content as a byte array
                byte[] fileContent = file.getBytes();
                String originalFileName = file.getOriginalFilename();

                Document document = new Document(title, fileContent, author, LocalDateTime.now(), type, originalFileName);

                // Save to repository (this is the potentially long-running operation)
                documentRepository.save(document);
                log.info("Document '{}' ingested successfully.", title);
                return new ResponseEntity<>("Document ingested successfully", HttpStatus.CREATED);

            } catch (IOException e) {
                log.error("Error ingesting document '{}': {}", title, e.getMessage(), e);
                return new ResponseEntity<>("Error ingesting document: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (Exception e) {
                log.error("An unexpected error occurred during document ingestion for '{}': {}", title, e.getMessage(), e);
                return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }, taskExecutor); // Use the custom taskExecutor
    }



    private boolean isValidFileType(String type) {
        return "text".equalsIgnoreCase(type) || "pdf".equalsIgnoreCase(type) || "docx".equalsIgnoreCase(type);
    }
    @PostMapping("/qa")
    public ResponseEntity<Page<Document>> qa(@RequestBody QARequest qaRequest) {
        // Basic keyword matching
        Pageable pageable = PageRequest.of(qaRequest.getPage(), qaRequest.getSize());
        Page<Document> results = documentRepository.findByContentContainingKeyword(qaRequest.getQuestion(), pageable);
        // Full-text search
        // Pageable pageable = PageRequest.of(qaRequest.getPage(), qaRequest.getSize());
        // Page<Document> results = documentRepository.fullTextSearch(qaRequest.getQuestion(), pageable);
        return ResponseEntity.ok(results);
    }
    @PostMapping("/select")
    public ResponseEntity<Page<Document>> selectDocuments(@RequestBody DocumentSelectionRequest request) {
        Pageable pageable;
        if (request.getSortBy() != null) {
            Sort.Direction direction = "asc".equalsIgnoreCase(request.getSortOrder()) ? Sort.Direction.ASC : Sort.Direction.DESC;
            pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by(direction, request.getSortBy()));
        } else {
            pageable = PageRequest.of(request.getPage(), request.getSize());
        }
        Page<Document> results;
        if (request.getAuthor() != null) {
            results = documentRepository.findByAuthor(request.getAuthor(), pageable);
        } else if (request.getStartDate() != null && request.getEndDate() != null) {
            results = documentRepository.findByUploadDateBetween(request.getStartDate(), request.getEndDate(), pageable);
        } else if (request.getType() != null){
            results = documentRepository.findByType(request.getType(), pageable);
        }
        else {
            results = documentRepository.findAll(pageable);
        }
        return ResponseEntity.ok(results);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR') or hasRole('viewer')")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        Optional<Document> document = documentRepository.findById(id);
        if (document.isPresent()) {
            return ResponseEntity.ok(document.get());
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}