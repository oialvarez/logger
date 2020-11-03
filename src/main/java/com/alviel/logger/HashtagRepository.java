package com.alviel.logger;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HashtagRepository extends PagingAndSortingRepository<Hashtag, Long> {
}
