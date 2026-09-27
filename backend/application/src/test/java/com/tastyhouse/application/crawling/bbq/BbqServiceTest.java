package com.tastyhouse.application.crawling.bbq;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import com.tastyhouse.application.crawling.bbq.port.out.BbqMenuPort;
import com.tastyhouse.application.crawling.bbq.port.out.BbqProductCategoryResponse;
import com.tastyhouse.application.crawling.bbq.port.out.BbqProductResponse;
import com.tastyhouse.application.crawling.bbq.port.out.DownloadedImage;
import com.tastyhouse.application.crawling.bbq.port.out.ImageDownloadFailure;
import com.tastyhouse.application.crawling.bbq.port.out.ImageDownloadResult;
import com.tastyhouse.application.crawling.bbq.port.out.RemoteImagePort;
import com.tastyhouse.application.file.port.out.FileDeleteResult;
import com.tastyhouse.application.file.port.out.FileStoragePort;
import com.tastyhouse.application.file.port.out.write.UploadedFileRepository;
import com.tastyhouse.application.file.service.FileUploadService;
import com.tastyhouse.application.shared.exception.BatchJobException;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.file.model.UploadedFile;
import com.tastyhouse.domain.file.vo.UploadedFileId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BbqServiceTest {

    private static final Long SHOP_ID = 1L;
    private static final Long CATEGORY_ID = 30L;
    private static final Long BBQ_CATEGORY_ID = 3L;
    private static final Long BBQ_MENU_ID = 100L;
    private static final Long UPLOADED_FILE_ID = 77L;
    private static final String IMAGE_URL = "https://bbq.example/menu/chicken.png?v=1";
    private static final byte[] PNG_BYTES = {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};

    private final RecordingFileStorage storage = new RecordingFileStorage();
    private BbqMenuPort bbqMenuPort;
    private BbqProductSyncService bbqProductSyncService;
    private RemoteImagePort remoteImagePort;
    private BbqService service;

    @BeforeEach
    void setUp() {
        bbqMenuPort = mock(BbqMenuPort.class);
        bbqProductSyncService = mock(BbqProductSyncService.class);
        remoteImagePort = mock(RemoteImagePort.class);
        FileUploadService fileUploadService = new FileUploadService(new FixedIdRepository(), storage, event -> { });
        service = new BbqService(bbqMenuPort, bbqProductSyncService, remoteImagePort, fileUploadService);

        BbqProductCategoryResponse category = BbqProductCategoryResponse.from(BBQ_CATEGORY_ID, SHOP_ID, "치킨", 0, true);
        when(bbqMenuPort.fetchMenuCategories()).thenReturn(List.of(category));
        when(bbqMenuPort.fetchMenusByCategoryId(BBQ_CATEGORY_ID)).thenReturn(List.of(menu(null)));
        when(bbqProductSyncService.resolveCategoryId(anyLong(), anyString(), anyInt())).thenReturn(CATEGORY_ID);
    }

    @Test
    @DisplayName("이미지 URL이 있으면 다운로드 결과를 파일로 업로드하고, 반환된 파일 id를 상품 등록에 싣는다")
    void uploadsDownloadedImageAndRegistersFileId() {
        when(bbqMenuPort.fetchMenuDetail(BBQ_MENU_ID)).thenReturn(menu(IMAGE_URL));
        when(remoteImagePort.download(IMAGE_URL)).thenReturn(ImageDownloadResult.downloaded(new DownloadedImage(PNG_BYTES, "image/png", "chicken.png")));

        service.crawlAndSaveNewMenu(SHOP_ID);

        assertThat(storage.content).containsExactly(PNG_BYTES);
        assertThat(storage.contentType).isEqualTo("image/png");
        assertThat(storage.storedFilename).endsWith(".png");
        assertThat(registeredProduct().imageFileId()).isEqualTo(UPLOADED_FILE_ID);
    }

    @Test
    @DisplayName("이미지 URL이 비어 있으면 원격 이미지를 받지 않고 이미지 없이 상품을 등록한다")
    void skipsDownloadWhenImageUrlIsEmpty() {
        when(bbqMenuPort.fetchMenuDetail(BBQ_MENU_ID)).thenReturn(menu(""));

        service.crawlAndSaveNewMenu(SHOP_ID);

        verify(remoteImagePort, never()).download(any());
        assertThat(storage.content).isNull();
        assertThat(registeredProduct().imageFileId()).isNull();
    }

    @ParameterizedTest
    @CsvSource({
        "EMPTY, FILE_EMPTY",
        "SIZE_EXCEEDED, FILE_SIZE_EXCEEDED"
    })
    @DisplayName("이미지 다운로드 실패 결과는 기존과 같은 파일 ErrorCode로 번역되어 크롤링을 실패시킨다")
    void translatesDownloadFailureToFileErrorCode(ImageDownloadFailure failure, ErrorCode expected) {
        when(bbqMenuPort.fetchMenuDetail(BBQ_MENU_ID)).thenReturn(menu(IMAGE_URL));
        when(remoteImagePort.download(IMAGE_URL)).thenReturn(ImageDownloadResult.failed(failure));

        assertThatThrownBy(() -> service.crawlAndSaveNewMenu(SHOP_ID))
            .isInstanceOf(BatchJobException.class)
            .cause()
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", expected);
        assertThat(storage.content).isNull();
        verify(bbqProductSyncService, never()).createCrawledProduct(any());
    }

    private BbqProductRegistration registeredProduct() {
        ArgumentCaptor<BbqProductRegistration> captor = ArgumentCaptor.forClass(BbqProductRegistration.class);
        verify(bbqProductSyncService).createCrawledProduct(captor.capture());
        return captor.getValue();
    }

    private static BbqProductResponse menu(String imageUrl) {
        return BbqProductResponse.from(BBQ_MENU_ID, "황금올리브", "설명", imageUrl, 20000, 0, false, false, true, true);
    }

    private static final class RecordingFileStorage implements FileStoragePort {

        private byte[] content;
        private String storedFilename;
        private String contentType;

        @Override
        public String store(byte[] content, String storedFilename, String datePath, String contentType) {
            this.content = content;
            this.storedFilename = storedFilename;
            this.contentType = contentType;
            return datePath + "/" + storedFilename;
        }

        @Override
        public String getFileUrl(String filePath) {
            return filePath;
        }

        @Override
        public FileDeleteResult delete(String filePath) {
            return FileDeleteResult.deleted();
        }
    }

    private static final class FixedIdRepository implements UploadedFileRepository {

        @Override
        public UploadedFile save(UploadedFile file) {
            return UploadedFile.reconstitute(
                UPLOADED_FILE_ID,
                file.getOriginalFilename(),
                file.getStoredFilename(),
                file.getFilePath(),
                file.getFileSize(),
                file.getContentType(),
                null,
                null
            );
        }

        @Override
        public Optional<UploadedFile> findById(UploadedFileId id) {
            return Optional.empty();
        }
    }
}
