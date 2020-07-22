package in.bluebytes.statechanger.repository;

import in.bluebytes.statechanger.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}
