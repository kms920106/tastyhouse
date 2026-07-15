package com.tastyhouse.adminapi.event;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.event.domain.model.EventAnnouncement;
import com.tastyhouse.core.domain.event.domain.model.EventStatus;
import com.tastyhouse.core.domain.event.domain.model.EventWinner;
import com.tastyhouse.core.domain.event.domain.vo.EventId;
import com.tastyhouse.core.domain.file.domain.model.UploadedFile;
import com.tastyhouse.core.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.core.domain.event.application.EventCommandService;
import com.tastyhouse.core.domain.event.application.EventQueryService;
import com.tastyhouse.core.domain.event.application.dto.EventAdminDetailDto;
import com.tastyhouse.core.domain.event.application.dto.EventAdminListItemDto;
import com.tastyhouse.core.domain.event.application.dto.EventSearchCondition;
import com.tastyhouse.core.domain.event.application.dto.EventWinnerDto;
import com.tastyhouse.core.domain.event.application.dto.command.EventAnnouncementCreateCommand;
import com.tastyhouse.core.domain.event.application.dto.command.EventAnnouncementUpdateCommand;
import com.tastyhouse.core.domain.event.application.dto.command.EventCreateCommand;
import com.tastyhouse.core.domain.event.application.dto.command.EventUpdateCommand;
import com.tastyhouse.core.domain.event.application.dto.command.EventWinnerCreateCommand;
import com.tastyhouse.core.domain.file.application.FileQueryService;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.adminapi.common.FileResponse;
import com.tastyhouse.adminapi.event.response.EventAnnouncementResponse;
import com.tastyhouse.adminapi.event.response.EventDetailResponse;
import com.tastyhouse.adminapi.event.response.EventListItemResponse;
import com.tastyhouse.adminapi.event.response.EventPageResponse;
import com.tastyhouse.adminapi.event.response.EventWinnerResponse;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventCommandService eventCommandService;
    private final EventQueryService eventQueryService;
    private final FileQueryService fileQueryService;
    private final FileService fileService;

    public EventPageResponse getEvents(String name, String status, int page, int size) {
        EventStatus eventStatus = status == null ? null : EventStatus.from(status);
        EventSearchCondition condition = EventSearchCondition.of(name, eventStatus);
        PageResult<EventListItemResponse> pageResult = eventQueryService.findAllEvents(condition, page, size)
            .map(this::toListItemResponse);
        return EventPageResponse.from(pageResult);
    }

    private EventListItemResponse toListItemResponse(EventAdminListItemDto dto) {
        return EventListItemResponse.from(dto, toFileResponse(dto.thumbnailImageFileId(), dto.thumbnailFileName(), dto.thumbnailFilePath()));
    }

    public Long createEvent(
        String name,
        String description,
        String subtitle,
        Long thumbnailImageFileId,
        Long bannerImageFileId,
        String contentHtml,
        String status,
        LocalDateTime startAt,
        LocalDateTime endAt
    ) {
        EventCreateCommand command = EventCreateCommand.of(
            name, description, subtitle, thumbnailImageFileId, bannerImageFileId,
            contentHtml, EventStatus.from(status), startAt, endAt
        );
        EventId eventId = eventCommandService.createEvent(command);
        return eventId.value();
    }

    public EventDetailResponse getEvent(Long id) {
        EventId eventId = EventId.of(id);
        EventAdminDetailDto detail = eventQueryService.findAdminDetailById(eventId);
        FileResponse thumbnailFile = toFileResponse(detail.thumbnailImageFileId());
        FileResponse bannerFile = toFileResponse(detail.bannerImageFileId());
        return EventDetailResponse.from(detail, thumbnailFile, bannerFile);
    }

    public void updateEvent(
        Long id,
        String name,
        String description,
        String subtitle,
        Long thumbnailImageFileId,
        Long bannerImageFileId,
        String contentHtml,
        String status,
        LocalDateTime startAt,
        LocalDateTime endAt
    ) {
        EventId eventId = EventId.of(id);
        EventUpdateCommand command = EventUpdateCommand.of(
            name, description, subtitle, thumbnailImageFileId, bannerImageFileId,
            contentHtml, EventStatus.from(status), startAt, endAt
        );
        eventCommandService.updateEvent(eventId, command);
    }

    public void deleteEvent(Long id) {
        EventId eventId = EventId.of(id);
        eventCommandService.deleteEvent(eventId);
    }

    public Long createAnnouncement(Long id, String name, String content, LocalDateTime announcedAt) {
        EventId eventId = EventId.of(id);
        EventAnnouncementCreateCommand command = EventAnnouncementCreateCommand.of(name, content, announcedAt);
        return eventCommandService.createAnnouncement(eventId, command);
    }

    public void updateAnnouncement(Long id, String name, String content, LocalDateTime announcedAt) {
        EventId eventId = EventId.of(id);
        EventAnnouncementUpdateCommand command = EventAnnouncementUpdateCommand.of(name, content, announcedAt);
        eventCommandService.updateAnnouncement(eventId, command);
    }

    public EventAnnouncementResponse getAnnouncement(Long id) {
        EventId eventId = EventId.of(id);
        EventAnnouncement announcement = eventQueryService.findAnnouncementByEventId(eventId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.EVENT_ANNOUNCEMENT_NOT_FOUND));
        return EventAnnouncementResponse.from(announcement);
    }

    public Long createWinner(Long id, Integer rankNo, String winnerName, String phoneNumber, LocalDateTime announcedAt) {
        EventId eventId = EventId.of(id);
        EventWinnerCreateCommand command = EventWinnerCreateCommand.of(rankNo, winnerName, phoneNumber, announcedAt);
        return eventCommandService.createWinner(eventId, command);
    }

    public List<EventWinnerResponse> getWinners(Long id) {
        EventId eventId = EventId.of(id);
        List<EventWinner> winners = eventQueryService.findWinnersByEventId(eventId);
        return winners.stream()
            .map(EventWinnerDto::from)
            .map(EventWinnerResponse::from)
            .toList();
    }

    public void deleteWinner(Long winnerId) {
        eventCommandService.deleteWinner(winnerId);
    }

    private FileResponse toFileResponse(Long fileId, String fileName, String filePath) {
        if (fileId == null) {
            return null;
        }
        return FileResponse.of(fileId, fileName, fileService.getUrlByPath(filePath));
    }

    private FileResponse toFileResponse(Long fileId) {
        if (fileId == null) {
            return null;
        }
        UploadedFile uploadedFile = fileQueryService.findById(UploadedFileId.of(fileId))
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.FILE_NOT_FOUND));
        return FileResponse.of(fileId, uploadedFile.getOriginalFilename(), fileService.getUrlByPath(uploadedFile.getFilePath()));
    }
}
