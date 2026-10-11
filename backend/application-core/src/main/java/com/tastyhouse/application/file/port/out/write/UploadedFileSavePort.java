package com.tastyhouse.application.file.port.out.write;

import com.tastyhouse.domain.file.model.UploadedFile;

public interface UploadedFileSavePort {

    UploadedFile save(UploadedFile uploadedFile);
}
