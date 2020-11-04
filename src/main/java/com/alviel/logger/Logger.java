package com.alviel.logger;

import java.sql.Timestamp;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import lombok.Data;

@Entity
@Data
public class Logger {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Timestamp created;
    private String host;
    private String origin;
    private String details;
    private String stacktrace;
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.ALL})
    @JoinTable(name = "logger_hashtag",
            joinColumns = {@JoinColumn(name = "logger_id")},
            inverseJoinColumns = {@JoinColumn(name = "hashtag_id")})
    private List<Hashtag> hashtags;
}
