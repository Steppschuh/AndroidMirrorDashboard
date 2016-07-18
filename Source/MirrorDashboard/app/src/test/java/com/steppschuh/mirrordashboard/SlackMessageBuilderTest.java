package com.steppschuh.mirrordashboard;

import com.steppschuh.mirrordashboard.request.SlackLog;

import net.steppschuh.slackmessagebuilder.message.MessageBuilder;
import net.steppschuh.slackmessagebuilder.message.MessageLink;
import net.steppschuh.slackmessagebuilder.request.Webhook;

import org.junit.Test;

public class SlackMessageBuilderTest {

    @Test
    public void builder_exampleMessage() throws Exception {
        // create a webhook
        String hookUrl = "https://hooks.slack.com/services/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";
        Webhook webhook = new Webhook(hookUrl);

        // create some content
        MessageLink gitHubLink = new MessageLink("https://github.com/Steppschuh/SlackMessageBuilder", "GitHub repo");

        // create a message
        MessageBuilder messageBuilder = new MessageBuilder()
                .setChannel("#mirror")
                .setUsername("Slack Message Builder")
                .setIconEmoji(":+1:")
                .setText("I'm the message text with a link to a " + gitHubLink + " :octocat:");

        // send message
        webhook.postMessageSynchronous(messageBuilder.build());
    }

    @Test
    public void builder_logMessage() throws Exception {
        SlackLog.d(SlackMessageBuilderTest.class.getSimpleName(), "I'm the log text");
        Thread.sleep(3000); // SlackLogs are asynchronous, don't kill them
    }
}