package com.tastyhouse.application.crawling.bbq.port.out;

public interface RemoteImagePort {

    ImageDownloadResult download(String imageUrl);
}
