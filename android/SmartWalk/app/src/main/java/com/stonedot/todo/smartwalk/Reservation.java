package com.stonedot.todo.smartwalk;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by komatsu on 2016/10/30.
 */

public class Reservation {
    private SNS sns;
    private String sender;
    private String content;
    private Date time;

    public Reservation(SNS sns, String sender, String content, Date time) {
        this.sns = sns;
        this.sender = sender;
        this.content = content;
        this.time = time;
    }

    public SNS getSns() {
        return sns;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public String getTime(String format) {
        return new SimpleDateFormat(format).format(time);
    }
}
