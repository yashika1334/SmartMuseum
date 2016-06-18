package it.sapienza.pervasivesystems.smartmuseum.view.slack;

import android.os.AsyncTask;

import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;

import java.io.IOException;
import java.util.List;

import it.sapienza.pervasivesystems.smartmuseum.SmartMuseumApp;
import it.sapienza.pervasivesystems.smartmuseum.business.interlayercommunication.ILCMessage;
import it.sapienza.pervasivesystems.smartmuseum.business.slack.SlackBusiness;
import it.sapienza.pervasivesystems.smartmuseum.model.entity.UserModel;

/**
 * Created by andrearanieri on 14/06/16.
 */
public class ChatAsync extends AsyncTask<Void, Integer, String> {
    private SlackBusiness.SlackCommand command;
    private UserModel userModel;
    private ChatAsyncResponse delegate;
    private ILCMessage message = new ILCMessage();
    private SlackBusiness slackBusiness = new SlackBusiness();
    private String currentChannel;
    private String messageToSend;

    public ChatAsync(ChatAsyncResponse ca, UserModel um, SlackBusiness.SlackCommand c, String cc, String mts) {
        this.delegate = ca;
        this.userModel = um;
        this.command = c;
        this.currentChannel = cc;
        this.messageToSend = mts;
    }

    @Override
    protected String doInBackground(Void... voids) {
        switch (this.command) {
            case OPEN_SESSION:
                //slack session creation;
                try {
                    SmartMuseumApp.slackSession = new SlackBusiness().createSession(SlackBusiness.token);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.message.setMessageType(ILCMessage.MessageType.SUCCESS);
                this.message.setMessageObject(null);
                this.message.setMessageText("Slack session opened");
                break;
            case CLOSE_SESSION:
                try {
                    SmartMuseumApp.slackSession.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.message.setMessageType(ILCMessage.MessageType.SUCCESS);
                this.message.setMessageObject(null);
                this.message.setMessageText("Slack session closed");
                break;
            case DOWNLOAD_MESSAGES:
                List<SlackMessagePosted> messages = this.slackBusiness.getMessagesInChannel(SmartMuseumApp.slackSession, this.currentChannel, 100);
                this.message.setMessageType(ILCMessage.MessageType.SUCCESS);
                this.message.setMessageObject(messages);
                this.message.setMessageText("Messages Downloaded");
                break;
            case SEND_MESSAGE:
                this.slackBusiness.sendMessageToChannel(SmartMuseumApp.slackSession, this.currentChannel, messageToSend, this.userModel);
                List<SlackMessagePosted> updatedMessages = this.slackBusiness.getMessagesInChannel(SmartMuseumApp.slackSession, this.currentChannel, 100);
                this.message.setMessageType(ILCMessage.MessageType.SUCCESS);
                this.message.setMessageObject(updatedMessages);
                this.message.setMessageText("Message written");
                break;
        }

        return null;
    }

    protected void onPostExecute(String result) {
        switch (this.command) {
            case OPEN_SESSION:
                this.delegate.sessionOpened(this.message);
                break;
            case CLOSE_SESSION:
                this.delegate.sessionClosed(this.message);
                break;
            case DOWNLOAD_MESSAGES:
            case SEND_MESSAGE:
                this.delegate.messagesDownloaed(this.message);
                break;
        }
    }
}