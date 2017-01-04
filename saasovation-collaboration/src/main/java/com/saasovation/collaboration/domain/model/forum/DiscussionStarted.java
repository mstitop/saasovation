package com.saasovation.collaboration.domain.model.forum;

import java.util.Date;

import com.abigdreamer.infinity.ddd.domain.model.DomainEvent;

import com.saasovation.collaboration.domain.model.collaborator.Author;
import com.saasovation.collaboration.domain.model.tenant.Tenant;

/**
 * 讨论启动完毕
 * 
 * @author Darkness
 * @date 2014-5-30 下午3:42:16
 * @version V1.0
 */
public class DiscussionStarted implements DomainEvent {

    private Author author;
    private DiscussionId discussionId;
    private int eventVersion;
    private String exclusiveOwner;
    private ForumId forumId;
    private Date occurredOn;
    private String subject;
    private Tenant tenant;

    public DiscussionStarted(
            Tenant aTenant,
            ForumId aForumId,
            DiscussionId aDiscussionId,
            Author anAuthor,
            String aSubject,
            String anExclusiveOwner) {

        super();

        this.author = anAuthor;
        this.discussionId = aDiscussionId;
        this.eventVersion = 1;
        this.exclusiveOwner = anExclusiveOwner;
        this.forumId = aForumId;
        this.occurredOn = new Date();
        this.subject = aSubject;
        this.tenant = aTenant;
    }

    public Author author() {
        return this.author;
    }

    public DiscussionId discussionId() {
        return this.discussionId;
    }

    @Override
    public int eventVersion() {
        return this.eventVersion;
    }

    public String exclusiveOwner() {
        return this.exclusiveOwner;
    }

    public ForumId forumId() {
        return this.forumId;
    }

    @Override
    public Date occurredOn() {
        return this.occurredOn;
    }

    public String subject() {
        return this.subject;
    }

    public Tenant tenant() {
        return this.tenant;
    }
}
