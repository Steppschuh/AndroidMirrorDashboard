package com.steppschuh.mirrordashboard.request;

import android.os.Build;
import android.util.Log;

import net.steppschuh.slackmessagebuilder.message.MessageBuilder;
import net.steppschuh.slackmessagebuilder.message.attachment.Attachment;
import net.steppschuh.slackmessagebuilder.message.attachment.AttachmentBuilder;
import net.steppschuh.slackmessagebuilder.message.attachment.AttachmentField;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public final class SlackLog {

    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;

    private static final MessageBuilder defaultMessageBuilder = getDefaultMessageBuilder();

    public static MessageBuilder getDefaultMessageBuilder() {
        MessageBuilder messageBuilder = new MessageBuilder();
        resetToDefaultMessageBuilder(messageBuilder);
        return messageBuilder;
    }

    public static void resetToDefaultMessageBuilder() {
        resetToDefaultMessageBuilder(defaultMessageBuilder);
    }

    public static void resetToDefaultMessageBuilder(MessageBuilder messageBuilder) {
        messageBuilder
                .setChannel("#mirror")
                .setUsername("Mirror")
                .setText(null)
                .setAttachments(null)
                .setIconEmoji(":frame_with_picture:")
                .setIconUrl(null);
    }

    public static void v(String tag, Object content) {
        Log.v(tag, content.toString());
        Attachment attachment = generateAttachment(VERBOSE, tag, content);
        post(attachment);
    }

    public static void d(String tag, Object content) {
        Log.d(tag, content.toString());
        Attachment attachment = generateAttachment(DEBUG, tag, content);
        post(attachment);
    }

    public static void i(String tag, Object content) {
        Log.i(tag, content.toString());
        Attachment attachment = generateAttachment(INFO, tag, content);
        post(attachment);
    }

    public static void w(String tag, Object content) {
        Log.w(tag, content.toString());
        Attachment attachment = generateAttachment(WARN, tag, content);
        post(attachment);
    }

    public static void e(String tag, Object content) {
        Log.e(tag, content.toString());
        Attachment attachment = generateAttachment(ERROR, tag, content);
        post(attachment);
    }

    public static void e(Exception exception) {
        e(null, exception);
    }

    public static void e(String tag, Exception exception) {
        e(tag, exception.getMessage(), exception);
    }

    public static void e(String tag, String message, Exception exception) {
        Log.e(tag, message + ": " + exception.getMessage());
        Attachment attachment = generateAttachment(ERROR, tag, message);

        List<AttachmentField> fields = new ArrayList<>();
        StackTraceElement[] traces = exception.getStackTrace();
        if (traces != null && traces.length > 0) {
            fields.add(new AttachmentField("Method", traces[0].getMethodName()));
            fields.add(new AttachmentField("Line", String.valueOf(traces[0].getLineNumber())));
            String className = traces[0].getClassName();
            int dotIndex = className.lastIndexOf('.');
            if (dotIndex != -1) {
                className = className.substring(dotIndex + 1);
            }
            fields.add(new AttachmentField("Class", className));
        }
        attachment.setFields(fields);

        post(attachment);
    }

    public static void post(Attachment attachment) {
        resetToDefaultMessageBuilder();
        defaultMessageBuilder.addAttachment(attachment);

        RequestHelper.getSlackWebhook().postMessage(defaultMessageBuilder.build());
    }

    public static Attachment generateAttachment(int logLevel, String tag, Object content) {
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        Date now = Calendar.getInstance().getTime();
        String readableTime = df.format(now);

        StringBuilder sb = new StringBuilder(tag)
                .append(" - ").append(Build.MODEL)
                .append(" - ").append(readableTime)
                .append(" (").append(now.getTime()).append(")");

        return getAttachmentBuilder(logLevel)
                .setText(content.toString())
                .setFooter(sb.toString())
                .build();
    }

    public static AttachmentBuilder getAttachmentBuilder(int logLevel) {
        return new AttachmentBuilder()
                .setColor(getHexCode(logLevel));
    }

    public static String getHexCode(int logLevel) {
        switch (logLevel) {
            case DEBUG: {
                return "#607D8B";
            }
            case INFO: {
                return "#009688";
            }
            case WARN: {
                return "#FFC107";
            }
            case ERROR: {
                return "#f44336";
            }
            default: {
                return "#9E9E9E";
            }
        }
    }

}