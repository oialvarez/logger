package com.alviel.logger;

import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hashtags")
public class HashtagController {
    private final HashtagRepository hashtagRepository;

    @PutMapping("/{hashtagId}")
    Hashtag update(@PathVariable Long hashtagId,
                   @NotNull @RequestBody Hashtag hashtag) {
        if (!Objects.equals(hashtagId, hashtag.getId())) {
            throw new BadRequestException("Ambiguous hashtagId");
        }
        if (StringUtils.isBlank(hashtag.getDescription())) {
            throw new BadRequestException("Description not provided");
        }
        Optional<Hashtag> byId = hashtagRepository.findById(hashtagId);
        if (!byId.isPresent()) {
            throw new ResourceNotFoundException("Hashtag not found");
        }
        Hashtag foundHashtag = byId.get();
        foundHashtag.setDescription(hashtag.getDescription());
        return hashtagRepository.save(foundHashtag);
    }
}
