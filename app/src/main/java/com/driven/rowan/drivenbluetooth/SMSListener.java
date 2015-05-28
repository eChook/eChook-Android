package com.driven.rowan.drivenbluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.Toast;

/**
 * Created by BNAGY4 on 28/05/2015.
 */
public class SMSListener extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
			for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
				String messageBody = smsMessage.getMessageBody();
				MainActivity.SMSZone.append("\n" + messageBody);
				MainActivity.showMessage(MainActivity.getAppContext(), messageBody, Toast.LENGTH_LONG);
			}
		}
	}
}
