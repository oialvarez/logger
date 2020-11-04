package com.alviel.logger;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/logs")
public class LoggerController {
    private final LoggerRepository loggerRepository;
    private final HashtagRepository hashtagRepository;

    @PostMapping
    LogExposure create(@Valid @RequestBody LogExposure request) {
        if (CollectionUtils.isEmpty(request.getHashtags())) {
            throw new BadRequestException("No hashtags provided");
        }

        Logger logger = new Logger();
        logger.setCreated(new Timestamp(Instant.now().toEpochMilli()));
        logger.setHost(request.getHost());
        logger.setOrigin(request.getOrigin());
        logger.setDetails(request.getDetails());
        logger.setStacktrace(request.getStacktrace());

        List<Hashtag> logHashtags = getLogHashtags(request);
        logger.setHashtags(logHashtags);

        return toLogExposure(loggerRepository.save(logger));
    }

    private List<Hashtag> getLogHashtags(LogExposure request) {
        List<Hashtag> logHashtags = new ArrayList<>();
        List<Hashtag> existingHashtags =
                StreamSupport.stream(hashtagRepository.findAll().spliterator(), false)
                        .collect(Collectors.toList());
        request.getHashtags().forEach(requestHashtag -> {
            Optional<Hashtag> found = findExistingHashtag(existingHashtags, requestHashtag);
            if (found.isPresent()) {
                logHashtags.add(found.get());
            } else {
                Hashtag created = createHashtag(requestHashtag);
                logHashtags.add(created);
            }
        });
        return logHashtags;
    }

    private Optional<Hashtag> findExistingHashtag(List<Hashtag> existingHashtags, String requestHashtag) {
        Optional<Hashtag> found = Optional.empty();
        for (Hashtag ht : existingHashtags) {
            if (StringUtils.equalsAnyIgnoreCase(ht.getDescription(), requestHashtag)) {
                found = Optional.of(ht);
                break;
            }
        }
        return found;
    }

    private Hashtag createHashtag(String requestHashtag) {
        Hashtag newHashtag = new Hashtag();
        newHashtag.setDescription(requestHashtag);
        return hashtagRepository.save(newHashtag);
    }

    @GetMapping
    LogList list() {
        return transformLogExposure();
    }

    private LogList transformLogExposure() {
        List<LogExposure> logs = new ArrayList<>();
        loggerRepository.findAll()
                .forEach(logger -> logs.add(toLogExposure(logger)));
        LogList logList = new LogList();
        logList.setLogs(logs);
        return logList;
    }

    private LogExposure toLogExposure(Logger logger) {
        LogExposure logExposure = new LogExposure();
        logExposure.setId(logger.getId());
        logExposure.setHost(logger.getHost());
        logExposure.setOrigin(logger.getOrigin());
        logExposure.setDetails(logger.getDetails());
        logExposure.setStacktrace(logger.getStacktrace());
        List<String> hastags = logger.getHashtags().stream()
                .map(Hashtag::getDescription)
                .collect(Collectors.toList());
        logExposure.setHashtags(hastags);
        return logExposure;
    }

}
