package com.tastyhouse.application.crawling.bbq.port.out;

public interface RemoteImagePort {

    DownloadedImage download(String imageUrl);
}
