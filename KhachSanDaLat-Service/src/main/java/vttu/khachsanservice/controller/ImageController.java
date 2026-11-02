package vttu.khachsanservice.controller;


import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/images")
public class ImageController {
    private final Path imageDir = Paths.get("/app/images");

    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String fileName) throws IOException {

        Path filePath = imageDir.resolve(fileName).normalize();

        // Không cho phép truy cập ra ngoài /app/images
        if (!filePath.startsWith(imageDir)) {
            return ResponseEntity.badRequest().build();
        }

        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(filePath);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

        if (contentType != null) {
            mediaType = MediaType.parseMediaType(contentType);
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }
}
