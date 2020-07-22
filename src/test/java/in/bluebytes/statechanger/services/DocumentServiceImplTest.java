package in.bluebytes.statechanger.services;

import in.bluebytes.statechanger.domain.Document;
import in.bluebytes.statechanger.domain.DocumentEvent;
import in.bluebytes.statechanger.domain.DocumentState;
import in.bluebytes.statechanger.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DocumentServiceImplTest {

    @Autowired
    DocumentService documentService;

    @Autowired
    DocumentRepository documentRepository;

    Document document;

    @BeforeEach
    void setUp() {
        document = Document.builder().stateToChange("FAIL").documentState(DocumentState.REVIEW).build();
    }

    @Transactional
    @Test
    void settleDocument() {
        Document savedDocument = documentService.newDocument(document);

        documentService.settleDocument(savedDocument.getId());

        Document settledDocument = documentRepository.getOne(savedDocument.getId());
    }
}