package dev.countryfair.player.playlazlo.com.countryfair.model;

/**
 * Created by lexap on 09.10.2017.
 */

public class PushMessageItem {
    private long googleSentTime;
    private PushMessageCommon common;
    private PushMessageEntity entity;
    private long from;
    private String messageId;

    public long getGoogleSentTime() {
        return googleSentTime;
    }

    public void setGoogleSentTime(long googleSentTime) {
        this.googleSentTime = googleSentTime;
    }

    public PushMessageCommon getCommon() {
        return common;
    }

    public void setCommon(PushMessageCommon common) {
        this.common = common;
    }

    public PushMessageEntity getEntity() {
        return entity;
    }

    public void setEntity(PushMessageEntity entity) {
        this.entity = entity;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
