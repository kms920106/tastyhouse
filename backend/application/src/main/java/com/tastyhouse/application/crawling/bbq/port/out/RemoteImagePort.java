package com.tastyhouse.application.crawling.bbq.port.out;

public interface RemoteImagePort {

    Long uploadFromUrl(String imageUrl);
}
