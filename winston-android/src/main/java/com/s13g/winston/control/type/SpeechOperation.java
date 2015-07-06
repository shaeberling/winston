/*
 * Copyright 2015 The Winston Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.s13g.winston.control.type;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import com.s13g.winston.control.operation.IOperationProcess;

import java.util.List;
import java.util.logging.Logger;

/**
 * This class uses the google voice recognition to wrap received spoken words into voice operations to
 * control winston functionality using the passed {@link IOperationProcess}
 */
public class SpeechOperation implements IOperationType, RecognitionListener {
    private static final Logger LOG = Logger.getLogger("SpeechOperation");
    private static final String RECOGNITION_APP_CONTEXT = "com.winston.control.voice";
    private static final String LANGUAGE_PREFERENCE = "en";
    private static final int MAX_SPEECH_RESULTS = 5;
    private final Context mContext;
    private SpeechRecognizer mSpeechRecognizer;
    private final IOperationProcess mOperationProcessor;
    private boolean mIsListening = false;
    private boolean mIsAutomaticActivation;

    /**
     * Constructor
     *
     * @param context               the context to global information about the application environment
     * @param operationProcessor      the processor is responsible ot process / forward the voice operation
     * @param isAutomaticActivation <code>true</code> the speech recognition must be activated once
     *                              (e.g. within a lifecycle) and will restart after processing a result<br>
     *                              <code>false</code> the speech recognition must be activated manual
     *                              (e.g. via a button) and won't restart automatically
     */
    public SpeechOperation(final Context context, final IOperationProcess operationProcessor, final boolean isAutomaticActivation) {
        mContext = context;
        mOperationProcessor = operationProcessor;
        mIsAutomaticActivation = isAutomaticActivation;
    }


    /**
     * Initializes the speech recognition listener
     */
    private Intent initializeRecognizerIntent() {
        final Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false); // TODO: currently as a test
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, RECOGNITION_APP_CONTEXT);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_RESULTS, MAX_SPEECH_RESULTS);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, LANGUAGE_PREFERENCE);
        return recognizerIntent;
    }


    /**
     * Starts to recognize voice operations if voice recognition is available
     * {@link android.speech.SpeechRecognizer#isRecognitionAvailable(android.content.Context)}
     *
     * @return <code>true</code> the recognition of the voice operation was successful started<br>
     * <code>false</code> the recognition of the voice operation wasn't started.
     */
    @Override
    public synchronized boolean startOperationRecognition() {
        boolean wasStarted = false;
        if (mIsListening == false && SpeechRecognizer.isRecognitionAvailable(mContext)) {
            final Intent recognizerIntent = initializeRecognizerIntent();
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext);
            mSpeechRecognizer.setRecognitionListener(this);
            mSpeechRecognizer.startListening(recognizerIntent);
            wasStarted = true;
            mIsListening = true;
            LOG.info("voice recognition started...");
        }
        return wasStarted;
    }


    /**
     * Stops the listening for operations and destroy the {@link SpeechRecognizer}
     *
     * @return <code>true</code>
     */
    @Override
    public synchronized boolean stopOperationRecognition() {
        mSpeechRecognizer.stopListening();
        mSpeechRecognizer.cancel();
        mSpeechRecognizer.destroy();
        mSpeechRecognizer = null;
        mIsListening = false;
        LOG.info("voice recognition stopped...");
        return true;
    }


    /**
     * @return <code>true</code> the operation is listening<br>
     * <code>false</code> the operation  is not listening
     */
    @Override
    public boolean isActive() {
        return mIsListening;
    }


    /**
     * If the passed {@link Bundle} contains {@link SpeechRecognizer#RESULTS_RECOGNITION}, this method
     * uses the passed {@link IOperationProcess} to initiate the processing of the recognized words.<br>
     * Additionally the operation recognition will be started again.
     *
     * @param results the recognition results. To retrieve the results in ArrayList<String>
     *                format use getStringArrayList(String) with RESULTS_RECOGNITION as a parameter.
     *                A float array of confidence values might also be given in CONFIDENCE_SCORES.
     */
    @Override
    public synchronized void onResults(final Bundle results) {
        mIsListening = false;

        if (results != null && results.containsKey(SpeechRecognizer.RESULTS_RECOGNITION)) {
            LOG.info("received results!");
            final List<String> recognizedWords = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            final float[] confidenceScore = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
            mOperationProcessor.processRecognizedOperationResults(recognizedWords, confidenceScore);
        } else {
            LOG.info("no results");
        }
        if (mIsAutomaticActivation) {
            // restarts the recognition of speech operation after receiving results
            startOperationRecognition();
        }
    }


    /**
     * Called when partial recognition results are available. The callback might be called at any
     * time between onBeginningOfSpeech() and onResults(Bundle) when partial results are ready.
     * This method may be called zero, one or multiple times for each call to startListening(Intent),
     * depending on the speech recognition service implementation.<br>
     * Partial results will be seen as real results currently.
     *
     * @param partialResults the returned results. To retrieve the results in ArrayList<String>
     *                       format use getStringArrayList(String) with RESULTS_RECOGNITION as
     *                       a parameter
     */
    @Override
    public void onPartialResults(Bundle partialResults) {
        // also try to process partial received results the same as normal results
        onResults(partialResults);
    }

    /**
     * A network or recognition error occurred.
     *
     * @param errorCode code is defined in {@link SpeechRecognizer}
     */
    @Override
    public synchronized void onError(final int errorCode) {

        switch (errorCode) {
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: // TODO
                LOG.warning("received error code: ERROR_NETWORK_TIMEOUT");
                break;
            case SpeechRecognizer.ERROR_NETWORK:// TODO
                LOG.warning("received error code: ERROR_NETWORK");
                break;
            case SpeechRecognizer.ERROR_AUDIO:// TODO
                LOG.warning("received error code: ERROR_AUDIO");
                break;
            case SpeechRecognizer.ERROR_SERVER:// TODO
                LOG.warning("received error code: ERROR_SERVER");
                break;
            case SpeechRecognizer.ERROR_CLIENT:// TODO
                LOG.warning("received error code: ERROR_CLIENT");
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:// TODO
                LOG.warning("received error code: ERROR_SPEECH_TIMEOUT");
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:// TODO
                LOG.warning("received error code: ERROR_NO_MATCH");
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:// TODO
                LOG.warning("received error code: ERROR_RECOGNIZER_BUSY");
                stopOperationRecognition();
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:// TODO
                LOG.warning("received error code: ERROR_INSUFFICIENT_PERMISSIONS");
                break;
            default:
        }
        mIsListening = false;

        if (mIsAutomaticActivation) {
            startOperationRecognition();
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        LOG.info("onEvent");
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        LOG.info("onReadyForSpeech");
    }

    @Override
    public void onBeginningOfSpeech() {
        LOG.info("onBeginningOfSpeech");
    }

    @Override
    public void onRmsChanged(final float rmsdB) {
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        LOG.info("onBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        LOG.info("onEndOfSpeech");
    }
}
