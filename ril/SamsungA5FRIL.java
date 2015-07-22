/*
 * Copyright (c) 2012-2013, The Linux Foundation. All rights reserved.
 * Not a Contribution.
 *
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.telephony;

import static com.android.internal.telephony.RILConstants.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.media.AudioManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.PowerManager.WakeLock;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.CellInfo;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.SignalStrength;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.SparseArray;

import com.android.internal.telephony.gsm.SmsBroadcastConfigInfo;
import com.android.internal.telephony.gsm.SsData;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccIoResult;
import com.android.internal.telephony.uicc.IccRefreshResponse;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.cdma.CdmaCallWaitingNotification;
import com.android.internal.telephony.cdma.CdmaInformationRecords;
import com.android.internal.telephony.cdma.CdmaSmsBroadcastConfigInfo;
import com.android.internal.telephony.dataconnection.DcFailCause;
import com.android.internal.telephony.dataconnection.DataCallResponse;
import com.android.internal.telephony.dataconnection.DataProfile;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;


/**
 * RIL customization for Galaxy A5F Duos device
 *
 * {@hide}
 */
public class SamsungA5FRIL extends RIL implements CommandsInterface {

    private static final int RIL_REQUEST_DIAL_EMERGENCY = 10016;
    private static final int RIL_UNSOL_RESPONSE_IMS_NETWORK_STATE_CHANGED = 1036;
    private static final int RIL_UNSOL_DEVICE_READY_NOTI = 11008;
    private static final int RIL_UNSOL_AM = 11010;
    private static final int RIL_UNSOL_WB_AMR_STATE = 11017;
    private static final int RIL_UNSOL_RESPONSE_HANDOVER = 11021;
    private static final int RIL_UNSOL_ON_SS_G7102 = 1040;
    private static final int RIL_UNSOL_STK_CC_ALPHA_NOTIFY_G7102 = 1041;
    private static final int RIL_UNSOL_UICC_SUBSCRIPTION_STATUS_CHANGED_G7102 = 11031;

    private AudioManager mAudioManager;
 private Message mPendingGetSimStatus;

    public SamsungA5FRIL(Context context, int networkMode, int cdmaSubscription,Integer instanceId) {
        super(context, networkMode, cdmaSubscription,  instanceId);
        mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        mQANElements = 6;
    }


    @Override
    public void
    dial(String address, int clirMode, UUSInfo uusInfo, Message result) {
        if (PhoneNumberUtils.isEmergencyNumber(address)) {
            dialEmergencyCall(address, clirMode, result);
            return;
        }

        RILRequest rr = RILRequest.obtain(RIL_REQUEST_DIAL, result);

        rr.mParcel.writeString(address);
        rr.mParcel.writeInt(clirMode);
        rr.mParcel.writeInt(0);         // CallDetails.call_type
        rr.mParcel.writeInt(1);         // CallDetails.call_domain
//        rr.mParcel.writeInt(0);         // CallDetails.call_domain

        rr.mParcel.writeString("");     // CallDetails.getCsvFromExtras

        logParcel(rr.mParcel);

        if (uusInfo == null) {
            rr.mParcel.writeInt(0); // UUS information is absent
        } else {
            rr.mParcel.writeInt(1); // UUS information is present
            rr.mParcel.writeInt(uusInfo.getType());
            rr.mParcel.writeInt(uusInfo.getDcs());
            rr.mParcel.writeByteArray(uusInfo.getUserData());
        }

        if (RILJ_LOGD) riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));

        send(rr);
    }

    @Override
    protected Object
    responseIccCardStatus(Parcel p) {
        IccCardApplicationStatus appStatus;

        IccCardStatus cardStatus = new IccCardStatus();
        cardStatus.setCardState(p.readInt());
        cardStatus.setUniversalPinState(p.readInt());
        cardStatus.mGsmUmtsSubscriptionAppIndex = p.readInt();
        cardStatus.mCdmaSubscriptionAppIndex = p.readInt();
        cardStatus.mImsSubscriptionAppIndex = p.readInt();

        int numApplications = p.readInt();

        // limit to maximum allowed applications
        if (numApplications > IccCardStatus.CARD_MAX_APPS) {
            numApplications = IccCardStatus.CARD_MAX_APPS;
        }
        cardStatus.mApplications = new IccCardApplicationStatus[numApplications];

        for (int i = 0 ; i < numApplications ; i++) {
            appStatus = new IccCardApplicationStatus();
            appStatus.app_type       = appStatus.AppTypeFromRILInt(p.readInt());
            appStatus.app_state      = appStatus.AppStateFromRILInt(p.readInt());
            appStatus.perso_substate = appStatus.PersoSubstateFromRILInt(p.readInt());
            appStatus.aid            = p.readString();
            appStatus.app_label      = p.readString();
            appStatus.pin1_replaced  = p.readInt();
            appStatus.pin1           = appStatus.PinStateFromRILInt(p.readInt());
            appStatus.pin2           = appStatus.PinStateFromRILInt(p.readInt());
            // All subsequent readInt()s added for our device
            p.readInt(); // pin1_num_retries
            p.readInt(); // puk1_num_retries
            p.readInt(); // pin2_num_retries
            p.readInt(); // puk2_num_retries
            p.readInt(); // perso_unblock_retries

            cardStatus.mApplications[i] = appStatus;
        }
        return cardStatus;
    }

    @Override
    protected Object
    responseCallList(Parcel p) {
        int num,tm;
        int voiceSettings;
        ArrayList<DriverCall> response;
        DriverCall dc;

        logParcel(p);

        int dataAvail = p.dataAvail();
        int pos = p.dataPosition();
        int size = p.dataSize();

        Rlog.d(RILJ_LOG_TAG, "Parcel size = " + size);
        Rlog.d(RILJ_LOG_TAG, "Parcel pos = " + pos);
        Rlog.d(RILJ_LOG_TAG, "Parcel dataAvail = " + dataAvail);

        //Samsung changes
        num = p.readInt();

        Rlog.d(RILJ_LOG_TAG, "num = " + num);

        response = new ArrayList<DriverCall>(num);

        if (RILJ_LOGV) {
            riljLog("responseCallList: num=" + num +
                    " mEmergencyCallbackModeRegistrant=" + mEmergencyCallbackModeRegistrant +
                    " mTestingEmergencyCall=" + mTestingEmergencyCall.get());
        }
        for (int i = 0 ; i < num ; i++) {


            dc = new DriverCall();

            dc.state = DriverCall.stateFromCLCC(p.readInt());
            // & 0xff to truncate to 1 byte added for us, not in RIL.java
            dc.index = p.readInt() & 0xff;
            dc.TOA = p.readInt();
            dc.isMpty = (0 != p.readInt());
            dc.isMT = (0 != p.readInt());
            dc.als = p.readInt();
            voiceSettings = p.readInt();
            dc.isVoice = (0 != voiceSettings);
            boolean isVideo = (0 != p.readInt());
            int call_type = p.readInt();            // Samsung CallDetails
            int call_domain = p.readInt();          // Samsung CallDetails
	    p.readInt();
//            String csv = p.readString();            // Samsung CallDetails
            dc.isVoicePrivacy = (0 != p.readInt());
            dc.number = p.readString();
            int np = p.readInt();
            dc.numberPresentation = DriverCall.presentationFromCLIP(np);
            dc.name = p.readString();
            dc.namePresentation = p.readInt();
            int uusInfoPresent = p.readInt();

            Rlog.d(RILJ_LOG_TAG, "state = " + dc.state);
            Rlog.d(RILJ_LOG_TAG, "index = " + dc.index);
            Rlog.d(RILJ_LOG_TAG, "state = " + dc.TOA);
            Rlog.d(RILJ_LOG_TAG, "isMpty = " + dc.isMpty);
            Rlog.d(RILJ_LOG_TAG, "isMT = " + dc.isMT);
            Rlog.d(RILJ_LOG_TAG, "als = " + dc.als);
            Rlog.d(RILJ_LOG_TAG, "isVoice = " + dc.isVoice);
            Rlog.d(RILJ_LOG_TAG, "number = " + dc.number);
            Rlog.d(RILJ_LOG_TAG, "np = " + np);
            Rlog.d(RILJ_LOG_TAG, "name = " + dc.name);
            Rlog.d(RILJ_LOG_TAG, "namePresentation = " + dc.namePresentation);
            Rlog.d(RILJ_LOG_TAG, "uusInfoPresent = " + uusInfoPresent);

            if (uusInfoPresent == 1) {
                dc.uusInfo = new UUSInfo();
                dc.uusInfo.setType(p.readInt());
                dc.uusInfo.setDcs(p.readInt());
                byte[] userData = p.createByteArray();
                dc.uusInfo.setUserData(userData);
                riljLogv(String.format("Incoming UUS : type=%d, dcs=%d, length=%d",
                                dc.uusInfo.getType(), dc.uusInfo.getDcs(),
                                dc.uusInfo.getUserData().length));
                riljLogv("Incoming UUS : data (string)="
                        + new String(dc.uusInfo.getUserData()));
                riljLogv("Incoming UUS : data (hex): "
                        + IccUtils.bytesToHexString(dc.uusInfo.getUserData()));
            } else {
                riljLogv("Incoming UUS : NOT present!");
            }

            // Make sure there's a leading + on addresses with a TOA of 145
            dc.number = PhoneNumberUtils.stringFromStringAndTOA(dc.number, dc.TOA);

            response.add(dc);

            if (dc.isVoicePrivacy) {
                mVoicePrivacyOnRegistrants.notifyRegistrants();
                riljLog("InCall VoicePrivacy is enabled");
            } else {
                mVoicePrivacyOffRegistrants.notifyRegistrants();
                riljLog("InCall VoicePrivacy is disabled");
            }
        }

        Collections.sort(response);

        if ((num == 0) && mTestingEmergencyCall.getAndSet(false)) {
            if (mEmergencyCallbackModeRegistrant != null) {
                riljLog("responseCallList: call ended, testing emergency call," +
                            " notify ECM Registrants");
                mEmergencyCallbackModeRegistrant.notifyRegistrant();
            }
        }

        return response;
    }

    @Override
    protected RILRequest
    processSolicited (Parcel p) {
        int serial, error;
        boolean found = false;
        int dataPosition = p.dataPosition(); // save off position within the Parcel
        serial = p.readInt();
        error = p.readInt();

        RILRequest rr = null;

        /* Pre-process the reply before popping it */
        synchronized (mRequestList) {
                RILRequest tr = mRequestList.get(serial);
                if (tr != null && tr.mSerial == serial) {
                    if (error == 0 || p.dataAvail() > 0) {
                        try {switch (tr.mRequest) {
                            /* Get those we're interested in */
                            case RIL_REQUEST_DATA_REGISTRATION_STATE:
                                rr = tr;
                                break;
                        }} catch (Throwable thr) {
                            // Exceptions here usually mean invalid RIL responses
                            if (tr.mResult != null) {
                                AsyncResult.forMessage(tr.mResult, null, thr);
                                tr.mResult.sendToTarget();
                            }
                            return tr;
                        }
                    }
                }
        }

        if (rr == null) {
            /* Nothing we care about, go up */
            p.setDataPosition(dataPosition);

            // Forward responses that we are not overriding to the super class
            return super.processSolicited(p);
        }


        rr = findAndRemoveRequestFromList(serial);

        if (rr == null) {
            return rr;
        }

        Object ret = null;

        if (error == 0 || p.dataAvail() > 0) {
            switch (rr.mRequest) {
                case RIL_REQUEST_DATA_REGISTRATION_STATE: ret =  dataRegState(p); break;
                default:
                    throw new RuntimeException("Unrecognized solicited response: " + rr.mRequest);
            }
            //break;
        }

        if (RILJ_LOGD) riljLog(rr.serialString() + "< " + requestToString(rr.mRequest)
            + " " + retToString(rr.mRequest, ret));

        if (rr.mResult != null) {
            AsyncResult.forMessage(rr.mResult, ret, null);
            rr.mResult.sendToTarget();
        }
        return rr;
    }

    private Object
    dataRegState(Parcel p) {
        int num;
        String response[];

        response = p.readStringArray();

        /* DANGER WILL ROBINSON
         * In some cases from Vodaphone we are receiving a RAT of 102
         * while in tunnels of the metro.  Lets Assume that if we
         * receive 102 we actually want a RAT of 2 for EDGE service */
        if (response.length > 4 &&
            response[0].equals("1") &&
            response[3].equals("102")) {

            response[3] = "2";
        }
        return response;
    }


    @Override
    protected void
    processUnsolicited (Parcel p) {
        Object ret = null;
        int dataPosition = p.dataPosition(); // save off position within the Parcel
        int response = p.readInt();
        int newResponse = response;
        
        switch(response) {
	    case RIL_UNSOL_RIL_CONNECTED: // Fix for NV/RUIM setting on CDMA SIM devices
                // skip getcdmascriptionsource as if qualcomm handles it in the ril binary
                ret = responseInts(p);
                setRadioPower(false, null);
                setPreferredNetworkType(mPreferredNetworkType, null);
                setCdmaSubscriptionSource(mCdmaSubscription, null);
                if(mRilVersion >= 8)
                    setCellInfoListRate(Integer.MAX_VALUE, null);
                notifyRegistrantsRilConnectionChanged(((int[])ret)[0]);
                break;
                
            case RIL_UNSOL_RESPONSE_IMS_NETWORK_STATE_CHANGED:
                ret = responseVoid(p);
                break;
            case RIL_UNSOL_DEVICE_READY_NOTI:
                ret = responseVoid(p);
                break;
            case RIL_UNSOL_AM:
                ret = responseString(p);
                samsungUnsljLogRet(response, ret);
                String amString = (String) ret;
                Rlog.d(RILJ_LOG_TAG, "Executing AM: " + amString);

                try {
                    Runtime.getRuntime().exec("am " + amString);
                } catch (IOException e) {
                    e.printStackTrace();
                    Rlog.e(RILJ_LOG_TAG, "am " + amString + " could not be executed.");
                }
                break;
            case RIL_UNSOL_WB_AMR_STATE:
                ret = responseInts(p);
                setWbAmr(((int[])ret)[0]);
                break;
            case RIL_UNSOL_RESPONSE_HANDOVER:
                ret = responseVoid(p);
                break;
            case 1040:
                newResponse = RIL_UNSOL_ON_SS;
                  p.setDataPosition(dataPosition);
                  p.writeInt(newResponse);
    		super.processUnsolicited(p);
                break;
            case 1041:
                newResponse = RIL_UNSOL_STK_CC_ALPHA_NOTIFY;
                  p.setDataPosition(dataPosition);
                  p.writeInt(newResponse);
    		super.processUnsolicited(p);
                break;
            case 11031:
                newResponse = RIL_UNSOL_UICC_SUBSCRIPTION_STATUS_CHANGED;
                  p.setDataPosition(dataPosition);
                  p.writeInt(newResponse);
    		super.processUnsolicited(p);
            case 11032:
                ret = responseVoid(p);
                break;

	    default:
                p.setDataPosition(dataPosition);
                super.processUnsolicited(p);
    		return;
        }
    }


   //@Override
    public void etUiccSubscription(int slotId, int appIndex, int subId,
            int subStatus, Message result) {
        //Note: This RIL request is also valid for SIM and RUIM (ICC card)
        RILRequest rr = RILRequest.obtain(115, result);

        riljLog(rr.serialString() + "MYRILJ> " + requestToString(rr.mRequest)
                + " slot: " + slotId + " appIndex: " + appIndex
                + " subId: " + subId + " subStatus: " + subStatus);

        rr.mParcel.writeInt(slotId);
        rr.mParcel.writeInt(appIndex);
        rr.mParcel.writeInt(subId);
        rr.mParcel.writeInt(subStatus);

        send(rr);
    }

   @Override
    public void setDataAllowed(boolean allowed, Message result) {
	int req = 123;
        RILRequest rr;
	if (allowed)
        {
            req = 116;
            rr = RILRequest.obtain(req, result);
        }
        else
        {
            rr = RILRequest.obtain(req, result);
            rr.mParcel.writeInt(1);
            rr.mParcel.writeInt(allowed ? 1 : 0);
        }
        send(rr);
    }

@Override
    public void
    getIccCardStatus(Message result) {
        if (mState != RadioState.RADIO_ON) {
            mPendingGetSimStatus = result;
        } else {
            super.getIccCardStatus(result);
        }
    }

    @Override
    protected void switchToRadioState(RadioState newState) {
        super.switchToRadioState(newState);

        if (newState == RadioState.RADIO_ON && mPendingGetSimStatus != null) {
            super.getIccCardStatus(mPendingGetSimStatus);
            mPendingGetSimStatus = null;
        }
    }


 public void
    getDataCallProfile(int appType, Message result) {
        RILRequest rr = RILRequest.obtain(
                RIL_REQUEST_GET_DATA_CALL_PROFILE, result);

        // count of ints
        rr.mParcel.writeInt(1);
        rr.mParcel.writeInt(appType);

        if (RILJ_LOGD) riljLog(rr.serialString() + "> " + requestToString(rr.mRequest)
                + " : " + appType);

        send(rr);
    }

    private void
    dialEmergencyCall(String address, int clirMode, Message result) {
        RILRequest rr;
        Rlog.v(RILJ_LOG_TAG, "Emergency dial: " + address);

        rr = RILRequest.obtain(RIL_REQUEST_DIAL_EMERGENCY, result);
        rr.mParcel.writeString(address + "/");
        rr.mParcel.writeInt(clirMode);
        rr.mParcel.writeInt(0);        // CallDetails.call_type
        rr.mParcel.writeInt(3);        // CallDetails.call_domain
        rr.mParcel.writeString("");    // CallDetails.getCsvFromExtra
        rr.mParcel.writeInt(0);        // Unknown

        if (RILJ_LOGD) riljLog(rr.serialString() + "> " + requestToString(rr.mRequest));

        send(rr);
    }

    private void logParcel(Parcel p) {
        StringBuffer s = new StringBuffer();
        byte [] bytes = p.marshall();

        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) s.append(" ");
            if (i == p.dataPosition()) s.append("*** ");
            s.append(bytes[i]);
        }
        riljLog("MYRILJ parcel position=" + p.dataPosition() + ": " + s);
    }

    static String
    samsungResponseToString(int request)
    {
        switch(request) {
            // SAMSUNG STATES
            case RIL_UNSOL_AM: return "RIL_UNSOL_AM";
            default: return "<unknown response: "+request+">";
        }
    }
    
    protected void samsungUnsljLogRet(int response, Object ret) {
        riljLog("[UNSL]< " + samsungResponseToString(response) + " " + retToString(response, ret));
    }

    private void setWbAmr(int state) {
        if (state == 1) {
            Rlog.d(RILJ_LOG_TAG, "setWbAmr(): setting audio parameter - wb_amr=on");
            mAudioManager.setParameters("wb_amr=on");
        }else if (state == 0) {
            Rlog.d(RILJ_LOG_TAG, "setWbAmr(): setting audio parameter - wb_amr=off");
            mAudioManager.setParameters("wb_amr=off");
        }
    }
}
